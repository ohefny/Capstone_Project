package com.feedme.app.MainScreen.Views.Adapters;

import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;

import com.feedme.app.ArticleType;
import com.feedme.app.FeedMeApp;
import com.feedme.app.MainScreen.Views.MainScreenActivity;
import com.feedme.app.MainScreen.Views.MySitesFragment;
import com.feedme.app.MainScreen.Views.ArticleListFragment;
import com.feedme.app.R;

/**
 * Created by BeTheChange on 7/10/2017.
 */
public class MainPagesAdapter extends FragmentPagerAdapter {

    private static final String LOG_TAG = "MainPagesAdapter";
    private FragmentActivity mActivity;
    private Fragment mCurrentFragment;

    public MainPagesAdapter(FragmentManager manager, MainScreenActivity activity) {
        super(manager);
        mActivity=activity;
    }

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return 2;
    }


    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return  addImageToTitle(FeedMeApp.getContext().getString(R.string.timeline),R.drawable.ic_launcher);
            case 1:
                return  addImageToTitle(FeedMeApp.getContext().getString(R.string.mysites),R.drawable.ic_launcher);


        }
        return "Item " + (position + 1);
    }

    private CharSequence addImageToTitle(String titleStr,@DrawableRes int id) {

        SpannableStringBuilder sb = new SpannableStringBuilder(" "+titleStr+" "); // space added before text for convenience

        Drawable drawable = mActivity.getBaseContext().getResources().getDrawable( id);

        drawable.setBounds(0, 0, 50, 50);
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
    // END_INCLUDE (pageradapter_getpagetitle)

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return ArticleListFragment.newInstance(1,(MainScreenActivity)mActivity, ArticleType.CATEGORY);
            case 1:
                return MySitesFragment.newInstance();
        }
       return null;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            mCurrentFragment = ((Fragment) object);
        }

        super.setPrimaryItem(container, position, object);
    }
    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

/**
     * Instantiate the {@link View} which should be displayed at {@code position}. Here we
     * inflate a layout from the apps resources and then change the text view to signify the position.
     */




}

