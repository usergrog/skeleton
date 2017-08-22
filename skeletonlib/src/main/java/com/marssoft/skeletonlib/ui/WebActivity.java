package com.marssoft.skeletonlib.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.marssoft.skeletonlib.ui.fragments.WebFragment;

import no.innocode.skeletonlib.R;

/**
 * Created by alexey on 25-Mar-16.
 */
public class WebActivity extends BaseActivity {

    protected WebFragment mAppWebFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWebFragment();
        handleIntent(getIntent());
    }

    protected void inflateLayout() {
        setContentView(R.layout.web_activity);
    }

    protected void initWebFragment() {
        //mAppWebFragment = (WebFragment) getSupportFragmentManager().findFragmentById(R.id.web_fragment);
        mAppWebFragment = new WebFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.web_fragment, mAppWebFragment).commit();
    }

    private void handleIntent(Intent intent){
        Bundle args = intent.getExtras();
        mAppWebFragment.updateArgs(args);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            showShareDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showShareDialog() {
        String title = mAppWebFragment.getWebView().getTitle();
        String url = mAppWebFragment.getWebView().getUrl();
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(url)) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + url);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, mWebView.getStartUrl());
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
        }
    }


    @Override
    public boolean onTryBackPress() {
        return false;
    }

    @Override
    public boolean hasBackStack() {
        return false;
    }

    @Override
    public void onBackStackChanged() {
    }

    @Override
    public void onBackPressed() {
        if (!mAppWebFragment.isVisible() || !mAppWebFragment.onTryBackPress()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
}
