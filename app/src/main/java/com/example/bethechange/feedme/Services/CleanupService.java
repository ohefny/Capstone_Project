package com.example.bethechange.feedme.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.bethechange.feedme.Data.Contracts;
import com.example.bethechange.feedme.FeedMeApp;
import com.example.bethechange.feedme.Utils.PrefUtils;

/**
 * Created by BeTheChange on 7/31/2017.
 */

public class CleanupService extends IntentService {
    private static final String ACTION_CLEAN_OLD="com.example.bethechange.feedme.Services.action.CleanOld";

    public CleanupService() {
        super("CleanupService");
    }
    public static void startActionCleanOld(Context context) {
        Intent intent = new Intent(context, CleanupService.class);
        intent.setAction(ACTION_CLEAN_OLD);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CLEAN_OLD.equals(action)) {
                //final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionCleanOld();
            }
        }
    }

    private void handleActionCleanOld() {
        Log.d("CleanUpService","Cleaning Up...");
        long deleteBefore=PrefUtils.deleteBefore(FeedMeApp.getContext());
        int n=getContentResolver().delete(Contracts.ArticleEntry.CONTENT_URI,Contracts.ArticleEntry.COLUMN_FETCHED_DATE+" < "+ deleteBefore+" And "+
        Contracts.ArticleEntry.COLUMN_SAVED+" = 0 And "+ Contracts.ArticleEntry.COLUMN_FAVORITE+" = 0 ",null);
        Log.d("CleanUpService",n+" Items Cleaned");
    }
}
