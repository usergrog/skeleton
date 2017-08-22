package com.marssoft.skeletonlib.ui;

import android.os.Bundle;
import android.view.MenuItem;

import no.innocode.skeletonlib.R;

/**
 * Created by alexey on 25-Mar-16.
 */
public class ModalWebActivity extends WebActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHomeIcon(R.drawable.ic_clear_24dp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
