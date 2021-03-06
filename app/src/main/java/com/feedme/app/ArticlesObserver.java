package com.feedme.app;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.feedme.app.Data.ArticleRepositoryActions;

/**
 * Created by BeTheChange on 7/13/2017.
 */

public class ArticlesObserver extends ContentObserver {
    private final ArticleRepositoryActions mListener;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */

    public ArticlesObserver(Handler handler, ArticleRepositoryActions listener) {
        super(handler);
        mListener= listener;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        mListener.onLocalDataChanged();


    }

}
