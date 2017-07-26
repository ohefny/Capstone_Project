package com.example.bethechange.feedme.MainScreen.Views;

import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bethechange.feedme.Data.ArticlesRepository;
import com.example.bethechange.feedme.Data.Contracts;
import com.example.bethechange.feedme.Data.FeedMeDBHelper;
import com.example.bethechange.feedme.Data.SitesRepository;
import com.example.bethechange.feedme.MainScreen.Models.Site;
import com.example.bethechange.feedme.MainScreen.Views.Adapters.MainPagesAdapter;
import com.example.bethechange.feedme.R;
import com.example.bethechange.feedme.Utils.DBUtils;

import java.net.URL;

public class MainScreenActivity extends AppCompatActivity implements
        TimelineFragment.FragmentActivityInteractor,MySitesFragment.FragmentActivityInteractor{
    private NavigationView mNavigationView;
    private DrawerLayout mDrawer;
    private View mNavHeader;
    private FloatingActionButton mFab;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private boolean mSearchActivity=false;
    private ProgressBar progressBar;
    private ObjectAnimator animation;
    private WebView webView;
    private MainPagesAdapter mAdapter;
    private int currentPage;

    //TODO if seprate search activity remove this boolean and it's usage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        mAdapter=new MainPagesAdapter(getSupportFragmentManager(),this);
        setupViews();

        Intent intent = getIntent();

        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            Toast.makeText(this,"Search Search",Toast.LENGTH_SHORT).show();
            mSearchActivity=true;
        }

        prepareAnimation();
        //FeedMeDBHelper dbHelper=new FeedMeDBHelper(this);
        //SQLiteDatabase wdb = dbHelper.getWritableDatabase();
        // wdb.execSQL("ALTER TABLE Article_Table ADD PUBLISHED_DATE TEXT;");
        // wdb.execSQL("ALTER TABLE Article_Table ADD webarchive_path TEXT;");
        // getContentResolver().bulkInsert(Contracts.SiteEntry.CONTENT_URI, DBUtils.sitesToCV(getSites()));
        // getContentResolver().delete(Contracts.SiteEntry.CONTENT_URI,null,null);
      //  getContentResolver().bulkInsert(Contracts.SiteEntry.CONTENT_URI,DBUtils.sitesToCV(getSites()));
        //insertSites();
    }

    private void prepareAnimation() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        animation = ObjectAnimator.ofInt (progressBar, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        animation.setDuration (5000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
    }


    public static  Site[] getSites() {
        //http://feeds.feedburner.com/TheAtlantic
        //https://www.polygon.com/rss/index.xml
        //http://www.coolhunting.com/atom.xml
        //http://www.betterlivingthroughdesign.com/feed
        //http://rss.cnn.com/rss/cnn_topstories.rss
        //http://www.washingtonpost.com/rss/
        //http://feeds.reuters.com/reuters/topNews
        //http://newsrss.bbc.co.uk/rss/newsonline_world_edition/americas/rss.xml
        String url = "http://stackoverflow.com/feeds/tag?tagnames=rome";

        Site site1=new Site();
        site1.setTitle("BBC Top Stories");
        site1.setUrl("bbc.com");
        site1.setRssUrl("http://newsrss.bbc.co.uk/rss/newsonline_world_edition/americas/rss.xml");
        site1.setCategoryID(1);
        site1.setmImgSrc("http://m.files.bbci.co.uk/modules/bbc-morph-news-waf-page-meta/1.2.0/bbc_news_logo.png?cb=1");
        Site site2=new Site();
        site2.setTitle("Washington Post: Today's Highlights");
        site2.setUrl("washingtonpost.com");
        site2.setRssUrl("http://feeds.washingtonpost.com/rss/politics");
        site2.setmImgSrc("http://www.jayheinz.com/wp-content/themes/synthetik/functions/timthumb.php?src=http://jayheinz.com/wp-content/uploads/2010/08/WashPost.jpg&h=290&w=580&zc=1");
        site2.setCategoryID(1);
        Site site3=new Site();
        site3.setTitle("Cool Hunting");
        site3.setUrl("coolhunting.com");
        site3.setRssUrl("http://www.coolhunting.com/atom.xml");
        site3.setmImgSrc("http://www.flat33.com/upload/CoolHuntingLogo_c_400.jpg");
        site3.setCategoryID(1);
        Site site4=new Site();
        site4.setTitle("Better Living Through Design");
        site4.setUrl("betterlivingthroughdesign.com");
        site4.setRssUrl("http://www.betterlivingthroughdesign.com/feed");
        site4.setmImgSrc("https://cdn.shopify.com/s/files/1/0156/3912/files/Better_Living_Through_Design.jpg");
        site4.setCategoryID(1);
        Site[]sites={site1,site3,site2,site4};//,site2};//,site4};
        return sites;
    }

    private void setupViews() {
        TabLayout mSlidingTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        final ViewPager mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage=position;
                if(position==1)
                    mFab.setImageResource(R.drawable.ic_fab_add);
                else {
                    mFab.setImageResource(R.drawable.ic_arrow_up);
                    ((TimelineFragment)mAdapter.getCurrentFragment()).fragmentVisible();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSlidingTabLayout.setupWithViewPager(mViewPager);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.closeDrawers();
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentPage){
                    case 0:
                        ((TimelineFragment)mAdapter.getCurrentFragment()).fabClicked();
                        break;
                    case 1:
                        ((MySitesFragment)mAdapter.getCurrentFragment()).fabClicked();
                        break;
                    case 2:
                        ((CategoriesFragment)mAdapter.getCurrentFragment()).fabClicked();
                        break;

                }


            }
        });
//        mNavHeader = mNavigationView.getHeaderView(0);
  //      mNavHeader.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        //ToDo:: implement my own header and inflate it using mNavigationView.inflateHeaderView()
        setUpNavigationView();
    }
    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        //TODO :: MAYBE ADD ADAPTER TO NAVIGATION VIEW
        //Todo :: replace action in this listener for navigationview
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;

                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);



                return true;
            }
        });


         mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };


        //Setting the actionbarToggle to drawer layout
        mDrawer.setDrawerListener(mActionBarDrawerToggle);
        mDrawer.closeDrawers();
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        //calling sync state is necessary or else your hamburger icon wont show up
        mActionBarDrawerToggle.syncState();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.fetch_news){
            ArticlesRepository.getInstance(this).getLatestArticles();
            return true;
        }
        return mActionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if(mSearchActivity){
            getMenuInflater().inflate(R.menu.nosearch,menu);
        }
        else {
            getMenuInflater().inflate(R.menu.main, menu);
            SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
            // Get the SearchView and set the searchable configuration
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            // Assumes current activity is the searchable activity
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        }

        return true;
    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
            return;
        }


        super.onBackPressed();
    }
    public FloatingActionButton getFab() {
        return mFab;
    }

    public void onSavedPostsClicked(View view) {
    }

    @Override
    protected void onStart() {
        ArticlesRepository.getInstance(this);
        SitesRepository.getInstance(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        ArticlesRepository.destroyInstance(this);
        SitesRepository.destroyInstance(this);
        super.onStop();
    }

    @Override
    public void openWebViewFragment(String link) {
        if(!(link.contains("https://")||link.contains("http://"))){
            link="http://"+link;
        }
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }
}
