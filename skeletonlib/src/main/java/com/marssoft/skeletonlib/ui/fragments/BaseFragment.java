package com.marssoft.skeletonlib.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.marssoft.skeletonlib.core.BackStackInterface;
import com.marssoft.skeletonlib.ui.BaseActivity;
import no.innocode.utils.lib.logs.Log;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by alexey on 25-Mar-16.
 */
public abstract class BaseFragment extends Fragment implements BackStackInterface {

    private static final String TAG = BaseFragment.class.getSimpleName();
    protected CompositeSubscription mCompositeSubscription;
    private boolean mDataWasLoaded;
    private String mCurrentFragmentTitle;
    private static Handler mainThreadHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setDataWasLoaded(false);
        mCompositeSubscription = new CompositeSubscription();
        if (getActivity() != null
                && getActivity() instanceof BaseActivity) {
            //noinspection ConstantConditions
            ((BaseActivity) getActivity()).showCustomToolbar(false);
        }
        return inflateLayout(inflater, container);
    }

    protected abstract View inflateLayout(LayoutInflater inflater, ViewGroup container);

    public void setCustomTitle(int titleRes) {
        if (getActivity() != null) {
            setCustomTitle(getActivity().getString(titleRes));
        }
    }

    public void setTitle(int titleRes) {
        if (getActivity() != null) {
            setTitle(getActivity().getString(titleRes));
        }
    }

    public void setCustomTitle(String title) {
        mCurrentFragmentTitle = title;
        // if fragment is visible, set title in host activity
        if (getUserVisibleHint()) {
            if (getActivity() != null
                    && getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).setCustomTitle(title);
            }
        }
    }

    public void setTitle(String title) {
        mCurrentFragmentTitle = title;
        // if fragment is visible, set title in host activity
        if (getUserVisibleHint()) {
            if (getActivity() != null
                    && getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).setTitle(title);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
//        } else {
//            onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }
        if (!TextUtils.isEmpty(mCurrentFragmentTitle)) {
            setCustomTitle(mCurrentFragmentTitle);
        }
    }

    public void updateArgs(Bundle args) {
        if (getArguments() != null) {
            getArguments().clear();
            getArguments().putAll(args);
        } else {
            setArguments(args);
        }
        if (isResumed()) {
            updateModel(true); // otherwise will update model in onCreateView callback
        }
    }

    public abstract void updateModel(boolean forceUpdate);

    @Override
    public abstract boolean onTryBackPress();

    @Override
    public abstract boolean hasBackStack();

    @Override
    public abstract void onBackStackChanged();

    public String getCurrentFragmentTitle() {
        return mCurrentFragmentTitle;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((BaseActivity) getActivity()).checkMenuSizeAndShowEmptyItem();
    }

    @Override
    public void onDestroyView() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
        super.onDestroyView();
    }

    public boolean isDataWasLoaded() {
        return mDataWasLoaded;
    }

    public void setDataWasLoaded(boolean dataWasLoaded) {
        mDataWasLoaded = dataWasLoaded;
    }

    public void showToast(final String message) {
        if (mainThreadHandler == null) {
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
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
