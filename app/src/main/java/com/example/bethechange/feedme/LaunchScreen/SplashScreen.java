package com.example.bethechange.feedme.LaunchScreen;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.bethechange.feedme.Data.ArticlesRepository;
import com.example.bethechange.feedme.Data.Contracts;
import com.example.bethechange.feedme.Data.SuggestRepository;
import com.example.bethechange.feedme.MainScreen.Models.Site;
import com.example.bethechange.feedme.MainScreen.Views.MainScreenActivity;
import com.example.bethechange.feedme.R;
import com.example.bethechange.feedme.Utils.CollectionUtils;
import com.example.bethechange.feedme.Utils.DBUtils;
import com.example.bethechange.feedme.Utils.FirebaseUtils;
import com.example.mvpframeworkedited.BasePresenterActivity;
import com.example.mvpframeworkedited.PresenterFactory;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Created by BeTheChange on 7/29/2017.
 */

public class SplashScreen extends BasePresenterActivity<LaunchPresenter,LaunchContracts.View>
        implements LaunchContracts.View, FirebaseUtils.FirebaseSitesListener {
    private FirebaseAuth mAuth;
    private LaunchPresenter presenter;
    private boolean logedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!FirebaseUtils.isGooglePlayServicesAvailable(this)){
           showError(getString(R.string.common_google_play_services_install_text_phone));
        }
        else{

        //TODO :: CHECK IF SIGNED IN OR NOT ... If Sites to be loaded Choosed or not
            if (getContentResolver().query(Contracts.SiteSuggestEntry.CONTENT_URI, null, null, null, null).getCount()==0) {
                FirebaseUtils.getSuggestionsSites(this);
            } else {
                load();
            }
        }
    }


    private void load() {
        SuggestRepository.getInstance(getContentResolver());
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            logedIn=true;
            if(presenter!=null)
                presenter.loginSuccessful();
        }
        else
            openLoginActivity();

    }



    @Override
    protected void onPresenterPrepared(@NonNull LaunchPresenter presenter) {
        super.onPresenterPrepared(presenter);
        this.presenter=presenter;
        presenter.bindView(this);
        if(logedIn)
            presenter.loginSuccessful();
    }
    @Override
    public void updateProgressMsg(String str) {

    }
    @Override
    public void openMainScreen() {
        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
        presenter.unbindView();
        finish();
    }

    @Override
    public void showError(String str) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        AlertDialog err = builder.setMessage(str).setNegativeButton(getString(R.string.close_btn_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        }).create();
        err.show();

    }

    @Override
    public void showSitesList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Cursor cur = getContentResolver().query(Contracts.SiteSuggestEntry.CONTENT_URI, null, null, null, null);
        final ArrayList<Site>sites=DBUtils.cursorToSuggestSites(cur);

        final boolean[] checks = new boolean[cur != null ? cur.getCount() : 0];
        final int[] count = {0};
        builder.setTitle("Pick Sites")
                .setMultiChoiceItems(CollectionUtils.objectsToStrings(sites),null,new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checks[which] = isChecked;
                        count[0] += isChecked ? 1 : -1;
                    }

                });
        builder.setPositiveButton(getString(R.string.add_btn_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (count[0] > 0){
                    int removed=0;
                    for(int i=0;i<checks.length;i++){
                        if(!checks[i])
                            sites.remove(i-removed++);
                    }
                    presenter.onSitesFetched(sites, false);
                }
                else
                    openMainScreen();
            }
        });
        AlertDialog addSitesDialog = builder.create();
        addSitesDialog.setCancelable(false);
        addSitesDialog.show();


    }
    @Override
    protected void onStop() {
        ArticlesRepository.destroyInstance(this);
        Log.d("OnStop Splash","OnStop");
        if(presenter!=null)
            presenter.unbindView();
        super.onStop();
    }

    @NonNull
    @Override
    protected String tag() {
        return null;
    }

    @NonNull
    @Override
    protected PresenterFactory<LaunchPresenter> getPresenterFactory() {
        return new LaunchPresenterFactory(ArticlesRepository.getInstance(this));
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        if(presenter!=null)
            presenter.unbindView();
        finish();
    }

    @Override
    public void onSitesFetched(ArrayList<Site> sites, boolean error) {
        if (!error) {
            getContentResolver().bulkInsert(Contracts.SiteSuggestEntry.CONTENT_URI, DBUtils.suggestSitesToCV(sites));
            load();
        }
        else
           openLoginActivity();

    }

    private void openErrorActivity() {
        Intent intent = new Intent(this, ErrorActivity.class);
        startActivity(intent);
        if(presenter!=null)
            presenter.unbindView();
        finish();
    }


}

