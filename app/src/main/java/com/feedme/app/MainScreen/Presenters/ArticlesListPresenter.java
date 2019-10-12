package com.feedme.app.MainScreen.Presenters;

import android.os.CountDownTimer;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.feedme.app.ArticleType;
import com.feedme.app.CustomScreen.SearchModel;
import com.feedme.app.Data.ArticleRepositoryActions;
import com.feedme.app.Data.ArticlesRepository;
import com.feedme.app.Data.CategoriesRepository;
import com.feedme.app.Data.ContentFetcher;
import com.feedme.app.Data.SitesRepository;
import com.feedme.app.FeedMeApp;
import com.feedme.app.MainScreen.Models.ArticlesList;
import com.feedme.app.MainScreen.Models.Category;
import com.feedme.app.MainScreen.Models.FeedMeArticle;
import com.feedme.app.MainScreen.Models.Site;
import com.feedme.app.MainScreen.ViewContracts.ArticleListContract;
import com.feedme.app.R;
import com.feedme.app.Utils.NetworkUtils;
import com.feedme.mvpframeworkedited.BasePresenter;

import java.util.ArrayList;

/**
 * Created by BeTheChange on 7/10/2017.
 */

public class ArticlesListPresenter extends BasePresenter<ArticlesList,ArticleListContract.View>
    implements ArticleListContract.Presenter, ArticlesRepository.ArticlesRepositoryObserver,
        NetworkUtils.InternetWatcher, CategoriesRepository.CategoriesListener {
    private final @ArticleType int type;
    private final SitesRepository siteRepo;
    private CategoriesRepository catRepo;
    private ContentFetcher mFetcher;
    private ArticleRepositoryActions articlesRepo;
    private FeedMeArticle requestedArticle;
    private Category mCategory;
    private int startPage=0;
    private Site[]mSites=null;
    private int pageSizes=1;
    private boolean openArticle;
    CountDownTimer countDownTimer;
    public ArticlesListPresenter(@NonNull ArticleRepositoryActions repo,SitesRepository siteRepo, CategoriesRepository catRepo, ContentFetcher fetcher,@ArticleType int type){
        this.articlesRepo=repo;
        this.catRepo=catRepo;
        this.mFetcher=fetcher;
        this.type=type;
        this.siteRepo=siteRepo;

        if(type==ArticleType.CATEGORY) {
            articlesRepo.setListener(this,null);
            catRepo.getCategories(this);
            setModel(articlesRepo.getArticles(null));
        }
        else if(type==ArticleType.BOOKMARKED){
            setModel(articlesRepo.getBookmarkedArticles());
        }
        else if(type==ArticleType.SAVED){
            setModel(articlesRepo.getSavedArticles());
        }
        else
            setModel(new ArticlesList());
    }
    public ArticlesListPresenter(@NonNull ArticleRepositoryActions repo, SitesRepository siteRepo, CategoriesRepository catRepo, ContentFetcher fetcher, @ArticleType int type, SearchModel model) {
        this(repo,siteRepo,catRepo,fetcher,type);
        if(type==ArticleType.SEARCH)
            articlesRepo.getArticlesFromSearchQuery(model,this);

    }
    @Override
    public void setModel(ArticlesList model) {
        for(FeedMeArticle ar:model.getArticles())
            ar.setSite(siteRepo.getSite(ar.getSiteID()));
        super.setModel(model);
    }

    public ArticlesListPresenter(@NonNull ArticleRepositoryActions repo, SitesRepository siteRepo, CategoriesRepository catRepo, ContentFetcher fetcher, @ArticleType int type, Category category){
        this(repo,siteRepo,catRepo,fetcher,type);
        mCategory=category;

    }
    public ArticlesListPresenter(@NonNull ArticleRepositoryActions repo, SitesRepository siteRepo,CategoriesRepository catRepo, ContentFetcher fetcher,@ArticleType int type,Site[]sites){
        this(repo,siteRepo,catRepo,fetcher,type);
        mSites=sites;
        if(type==ArticleType.SITE) {
            articlesRepo.setListener(this,mSites);
            setModel(articlesRepo.getArticles(mSites));
        }


    }
    @Override
    protected void updateView() {

        view().updateList(model);
    }

    @Override
    public void onPerformDelete(FeedMeArticle feedMeArticle) {

        articlesRepo.removeArticle(feedMeArticle);
        view().showMessage(FeedMeApp.getContext().getString(R.string.article_deleted_msg), null);
    }

    @Override
    public void onPerformSave(FeedMeArticle feedMeArticle) {

        if(!feedMeArticle.isSaved()){
            feedMeArticle.setSaved(true);
            articlesRepo.getFullArticle(feedMeArticle,this);
            view().saveArticleAsWebArchive(feedMeArticle);
            view().showMessage(FeedMeApp.getContext().getString(R.string.article_saved_msg), null);
        }
        else{
            feedMeArticle.setSaved(false);
            view().deleteWebArchive(feedMeArticle);
        }
        if(!feedMeArticle.isSaved()&&type==ArticleType.SAVED){
            model.getArticles().remove(feedMeArticle);
            updateView();
        }
    }

    @Override
    public void onPerformFav(FeedMeArticle feedMeArticle) {

        feedMeArticle.setFav(!feedMeArticle.isFav());
        articlesRepo.editArticle(feedMeArticle);
        if(!feedMeArticle.isFav()&&type==ArticleType.BOOKMARKED){
            model.getArticles().remove(feedMeArticle);
            updateView();
        }
        //view().showMessage("Article has been bookmarked", null);
    }

    @Override
    public void onWebArchiveSaved(FeedMeArticle feedMeArticle, String path) {
        feedMeArticle.setWebArchivePath(path);
        articlesRepo.editArticle(feedMeArticle);
    }

    private CountDownTimer getCountDownTimer() {
        return new CountDownTimer(20000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                System.out.println("ToFinish "+millisUntilFinished);

            }

            @Override
            public void onFinish() {
                new Handler(FeedMeApp.getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        internetAvailable(false);
                    }
                });
            }
        };
    }
    public void onFetchFail(){

    }
    @Override
    public void onOpenArticle(final FeedMeArticle article) {

        if ((article.isSaved() || article.isContentFetched())
                && view() != null) {
            view().showArticle(article, article.getArticle().getContent().isEmpty());
        } else {
            countDownTimer=getCountDownTimer();
            countDownTimer.start();
            view().showProgress();
            openArticle = true;
            requestedArticle = article;
            NetworkUtils.isInternetAccessible(this);
            articlesRepo.getFullArticle(requestedArticle,this);

        }
    }

    @Override
    public void onCategorySelected(Category item) {
        if(item==null)
            articlesRepo.setListener(this,null);
        else
            articlesRepo.setListener(this,siteRepo.getSites(item).toArray(new Site[]{}));
        setModel(articlesRepo.getArticles(siteRepo.getSites(item).toArray(new Site[]{})));
    }

    @Override
    public void onViewVisible() {
        catRepo.getCategories(this);
    }

    @Override
    public ArrayList<Integer> getArticlesIds() {
        ArrayList<Integer>ids=new ArrayList<>();
        if(model!=null&&model.getArticles().size()>0){
            for(int i=0;i<model.getArticles().size();i++)
                ids.add(model.getArticles().get(i).getArticleID());
        }
        return ids;
    }

    @Override
    public void internetAvailable(boolean isAvailable) {
        if (!isAvailable)
        {
            if(view()!=null) {
                view().endProgress();
                view().showMessage(FeedMeApp.getContext().getString(R.string.no_internet),
                        requestedArticle.getArticle().getSource());

            }
         openArticle=false;
            //onFullArticleFetched(requestedArticle);
        }
    }

    @Override
    public void onDataChanged(ArticlesList data) {


        if(view()!=null){
            view().endProgress();
            if(data.getArticles().size()>model.getArticles().size())
                view().showMessage(FeedMeApp.getContext().getString(R.string.update_made_msg), null);
        }
        setModel(data);
    }

    @Override
    public void bindView(@NonNull ArticleListContract.View view) {
        super.bindView(view);
        view().setInteractor(this);
    }

    @Override
    public void onFullArticleFetched(final FeedMeArticle fetchedArticle) {
        if(openArticle&&view()!=null){
            if(countDownTimer!=null)
                countDownTimer.cancel();
           // view().imageUpdated(fetchedArticle);
            view().endProgress();
            //if getcontent is empty then show the article on web
            view().showArticle(fetchedArticle,!fetchedArticle.isContentFetched()&&
                    (fetchedArticle.getArticle().getContent()==null||fetchedArticle.getArticle().getContent().isEmpty()));


        }
        openArticle=false;
    }


    @Override
    public void categoriesFetched(ArrayList<Category> cats) {
        if(view()!=null)
            view().updateCategoriesSpinner(cats);
    }
}

