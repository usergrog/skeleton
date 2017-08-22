package com.marssoft.skeletonlib.ui;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marssoft.skeletonlib.core.BackStackInterface;

import no.innocode.skeletonlib.R;
import no.innocode.utils.lib.logs.Log;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by alexey on 25-Mar-16.
 */
public abstract class BaseActivity extends AppCompatActivity implements BackStackInterface {

    private static final String TAG = BaseActivity.class.getSimpleName();
    protected CompositeSubscription mCompositeSubscription;
    protected Toolbar mToolbar;
    protected View mViewContainer;
    protected Bundle mSavedInstanceState;
    protected PorterDuffColorFilter mGreyFilter;
    protected RelativeLayout mRlCustomToolbar;
    protected TextView mTvCustomTitle;
    private MenuItem mNoneMenu;
    private Menu mMenu;
    private static Handler mainThreadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompositeSubscription = new CompositeSubscription();
        inflateLayout();
        mSavedInstanceState = savedInstanceState;
        mGreyFilter = new PorterDuffColorFilter(getResources().getColor(R.color.menuIconsColor), PorterDuff.Mode.SRC_ATOP);
        mViewContainer = findViewById(android.R.id.content);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        getSupportFragmentManager().addOnBackStackChangedListener(getBackStackListener());
        mRlCustomToolbar = (RelativeLayout) findViewById(R.id.rlCustomToolbar);
        mTvCustomTitle = (TextView) findViewById(R.id.tvToolbarTitle);
    }

    protected abstract void inflateLayout();

    public void setCustomTitle(String title){
        if (mTvCustomTitle != null) {
            mTvCustomTitle.setText(title);
        }
    }

    public void setTitle(String title){
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void setCustomTitle(int titleResource){
        if (mTvCustomTitle != null) {
            mTvCustomTitle.setText(titleResource);
        }
    }

    public void setTitle(int titleResource){
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleResource);
        }
    }

    private FragmentManager.OnBackStackChangedListener getBackStackListener() {
        return new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                BaseActivity.this.onBackStackChanged();
            }
        };
    }

    public void setHomeIcon(int drawableRes) {
        if (mToolbar != null){
            mToolbar.setNavigationIcon(drawableRes);
        }
    }


    @Override
    public abstract boolean onTryBackPress();

    @Override
    public abstract boolean hasBackStack();

    @Override
    public abstract void onBackStackChanged();

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }

    public void hideToolbar(boolean hide){
        mToolbar.setVisibility(hide? View.GONE :View.VISIBLE);
    }

    public void showCustomToolbar(boolean show) {
        if (mRlCustomToolbar != null) {
            mRlCustomToolbar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void applyColorFilterToMenuItems(Menu menu){
        for (int i = 0; i < menu.size(); i++){
            if (menu.getItem(i).getIcon() != null){
                menu.getItem(i).getIcon().mutate().setColorFilter(mGreyFilter);
            }
        }
    }

    protected void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                Activity.INPUT_METHOD_SERVICE);

        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }

        if (getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void setupUIForHidingFocus(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if (view != null && !(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    View focusedView = mViewContainer.findFocus();
                    if (focusedView != null) {
                        focusedView.clearFocus();
                    }
                    hideSoftKeyboard();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view != null && view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUIForHidingFocus(innerView);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (menu == null){
            getMenuInflater().inflate(R.menu.empty_menu, menu);
        }
        mNoneMenu = menu.add(Menu.NONE, 1001, Menu.NONE, "");
        mNoneMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mNoneMenu.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    public void checkMenuSizeAndShowEmptyItem(){
        if (mNoneMenu != null) {
            mNoneMenu.setVisible(mMenu.size() <= 1);
        }
    }

    @Override
    protected void onDestroy() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
        super.onDestroy();
    }

    public void showToast(final String message) {
        if (mainThreadHandler == null) {
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }, 0);
    }

    public void showToast(final Throwable throwable) {
        Log.w(TAG, throwable.getMessage() != null ? throwable.getMessage() : throwable.toString(), throwable);
        if (throwable.getMessage() == null){
            showToast(throwable.toString());
        } else {
            showToast(throwable.getMessage());
        }
    }

}
