package com.marssoft.skeletonlib.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Alexey Sidorenko on 05-May-16.
 */
public class ClearableEditText extends AppCompatEditText implements View.OnTouchListener, View.OnFocusChangeListener {

    private OnKeyboardEvent mOnKeyboardEvent;

    protected Drawable xD;

    private Listener listener;

    private View.OnTouchListener l;

    private OnFocusChangeListener f;

    public ClearableEditText(Context context) {
        super(context);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.l = l;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener f) {
        this.f = f;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            boolean tappedX = event.getX() > (getWidth() - getPaddingRight() - xD
                    .getIntrinsicWidth());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (getText().length() > 0) {
                        setText("");
                    } else {
                        hideSoftKeyboard();
                        clearFocus();
                    }
                    if (listener != null) {
                        listener.didClearText();
                    }
                }
                return true;
            }
        }
        return l != null && l.onTouch(v, event);
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        setClearIconVisible(hasFocus);
        if (f != null) {
            f.onFocusChange(v, hasFocus);
        }
    }

    private void init() {
        if (isInEditMode()) return;
        if (getCompoundDrawables()[2] instanceof Drawable) {
            xD = getCompoundDrawables()[2];
        }
/*
        if (xD == null) {
            xD = IconsHelper
                    .getSimpleIconC(FishkaIcon.CLOSE, getResources().getColor(R.color.active_red),
                            getResources().getDimensionPixelSize(R.dimen.icon_size), (int) FishkaApp.density * 10);

        }
*/
        xD.setBounds(0, 0, xD.getIntrinsicWidth(), xD.getIntrinsicHeight());
        setClearIconVisible(false);
        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
    }

    protected void setClearIconVisible(boolean visible) {
        Drawable x = visible ? xD : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            clearFocus();
            if (mOnKeyboardEvent != null) {
                mOnKeyboardEvent.onHide();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnKeyboardEvent(OnKeyboardEvent onKeyboardEvent) {
        mOnKeyboardEvent = onKeyboardEvent;
    }

    public interface OnKeyboardEvent {

        void onHide();
    }

    public interface Listener {

        void didClearText();
    }
}
