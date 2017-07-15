package com.example.bethechange.feedme.Data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.SparseArray;

import com.example.bethechange.feedme.ArticlesObserver;
import com.example.bethechange.feedme.FeedMeApp;
import com.example.bethechange.feedme.MainScreen.Models.ArticlesList;
import com.example.bethechange.feedme.MainScreen.Models.FeedMeArticle;
import com.example.bethechange.feedme.MainScreen.Models.Site;
import com.example.bethechange.feedme.MainScreen.ViewContracts.ArticleListContract;
import com.example.bethechange.feedme.Services.ArticlesDownloaderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BeTheChange on 7/11/2017.
 */

public class ArticlesRepository extends AsyncQueryHandler implements ArticleRepositoryActions {
    private int queryToken=1;
    private Site[]mSites=null;
    private SparseArray<Pair<ArticlesRepositoryObserver,Site[]>> mListeners=new SparseArray<>();
    private SparseArray<FeedMeArticle> fullArticles;
    private String sortCriteria=Contracts.ArticleEntry.COLUMN_DATE+" DESC";
    private final int INTIALIZE_TOKEN=0;
    private static ArticlesRepository repoInstance;
    private static ArticlesObserver observer;
    private boolean freshData;

    private ArticlesRepository(ContentResolver cr) {
        super(cr);
        refreshData();
        queryArticles(INTIALIZE_TOKEN);
        observer=new ArticlesObserver(new android.os.Handler(Looper.getMainLooper()),this);
        cr.registerContentObserver(Contracts.ArticleEntry.CONTENT_URI,false,observer);
    }
    public static void destroyInstance(Context context) {
        context.getContentResolver()
                .unregisterContentObserver(observer);
        repoInstance = null;
    }

    public static ArticlesRepository getInstance(Context context){
        if(repoInstance==null){
            repoInstance=new ArticlesRepository(context.getContentResolver());
            context.getContentResolver()
                    .registerContentObserver(Contracts.ArticleEntry.CONTENT_URI,false,observer);
        }

        return repoInstance;
    }
    public void setListener(ArticlesRepositoryObserver mListener,Site[]sites) {
       mListeners.put(mListener.hashCode(), new Pair<>(mListener, sites));
    }

    public void unsetListener(ArticlesRepositoryObserver mListener) {
       mListeners.remove(mListener.hashCode());
    }

    @Override
    public void onLocalDataChanged() {
        if(freshData){
            queryArticles(queryToken);
            freshData=false;
        }
    }


    private int insertValues(Uri uri, ContentValues[]cvs){
        for (ContentValues cv:cvs) {
            super.startInsert(0,null,uri,cv);
        }

        return FeedMeApp.getContext().getContentResolver().bulkInsert(uri,cvs);
    }
    private void queryArticles(int token){

        if(mSites==null){
            super.startQuery(token,null, Contracts.ArticleEntry.CONTENT_URI,null,null,null,sortCriteria);
        }
        else {
            String []selections=new String[mSites.length];
            for(int i=0;i<selections.length;i++){
                selections[i]=mSites[i].getID()+"";
            }
            super.startQuery(token,null,Contracts.ArticleEntry.CONTENT_URI,null,
                    Contracts.ArticleEntry._ID+"=?",selections,sortCriteria);
        }
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(token==INTIALIZE_TOKEN||mSites==null||mSites.length==0){
            fullArticles=DBUtils.cursorToArticles(cursor);

        }
        if(token==queryToken){
            queryToken++;
        }
        //Query Listener with it's registered sites
        deliverToListeners(false);
        cursor.close();
        super.onQueryComplete(token, cookie, cursor);
    }

    private void deliverToListeners(boolean fresh) {
        if(mListeners.size()!=0){
            for(int i=0;i<mListeners.size();i++){
                Pair<ArticlesRepositoryObserver, Site[]> listener=
                        mListeners.get(mListeners.keyAt(i));
                listener.first.onDataChanged(articlesFromSites(listener.second));
            }
        }
    }

    private void refreshData() {
        if(!PrefUtils.updateNow(FeedMeApp.getContext()))
            return;
        Intent intent=new Intent(FeedMeApp.getContext(),ArticlesDownloaderService.class);
        intent.setAction(ArticlesDownloaderService.ACTION_FETCH_LATEST);
        FeedMeApp.getContext().startService(intent);
        freshData=true;
    }
    public void getLatestArticles() {
        Intent intent=new Intent(FeedMeApp.getContext(),ArticlesDownloaderService.class);
        intent.setAction(ArticlesDownloaderService.ACTION_FETCH_LATEST);
        intent.putExtra(ArticlesDownloaderService.REFRESH_NOW,true);
        FeedMeApp.getContext().startService(intent);
        freshData=true;
    }

    private void removeValues(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs){
         super.startDelete(0,null,uri,selection ,selectionArgs);
    }
    public  void removeArticle(@NonNull FeedMeArticle feedMeArticle){
         fullArticles.remove(feedMeArticle.getArticleID());
        // removeValues(ContentUris.withAppendedId(Contracts.ArticleEntry.CONTENT_URI,feedMeArticle.getArticleID()),null,null);
        removeValues(Contracts.ArticleEntry.CONTENT_URI,Contracts.ArticleEntry._ID+" = ? ",new String[]{feedMeArticle.getArticleID()+""});
        deliverToListeners(false);
    }
    public void insertArticle(final FeedMeArticle article){
        fullArticles.put(article.getArticleID(),article);
        ArticlesList articlesList=new ArticlesList(Collections.singletonList(article));
        insertValues(Contracts.ArticleEntry.CONTENT_URI,DBUtils.articlesToCV(articlesList));
    }
    public void insertArticles( List<FeedMeArticle>articles){
        ArticlesList articlesList=new ArticlesList(articles);
        insertValues(Contracts.ArticleEntry.CONTENT_URI,DBUtils.articlesToCV(articlesList));
    }

    @Override
    public void editArticle(FeedMeArticle article) {
        ArticlesList articlesList=new ArticlesList(Collections.singletonList(article));
        super.startUpdate(0,null, ContentUris.withAppendedId(Contracts.ArticleEntry.CONTENT_URI,
                article.getArticleID()),
                            DBUtils.articlesToCV(articlesList)[0],null,null);
    }
    @Override
    public ArticlesList getArticles(@Nullable Site[]sites) {
        if(fullArticles==null)
            queryArticles(queryToken);
        if(mSites==null)
            return new ArticlesList(fullArticles);
        else{

            return articlesFromSites(sites);
        }
    }

    private ArticlesList articlesFromSites(@Nullable Site[] sites) {
        ArrayList<FeedMeArticle> filteredArticles=new ArrayList<>();
        if(sites==null)
            return new ArticlesList(fullArticles);
        for(Site st:sites){
            for (int i=0;i<fullArticles.size();i++){
                if(fullArticles.valueAt(i).getSiteID()==st.getID()){
                    filteredArticles.add(fullArticles.valueAt(i));
                }
            }
        }
        return new ArticlesList(filteredArticles);
    }

    public interface ArticlesRepositoryObserver {
        void onDataChanged(ArticlesList data);
    }
}
