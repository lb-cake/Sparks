package com.bandw.sparks.activities;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.bandw.sparks.BottomNavigationBehavior;
import com.bandw.sparks.SparksBaseHelper;
import com.bandw.sparks.fragments.LocateFragment;
import com.bandw.sparks.fragments.PhotoGalleryFragment;
import com.bandw.sparks.R;
import com.bandw.sparks.fragments.SavedFragment;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    fragment = new PhotoGalleryFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_local:
                    mTextMessage.setText(R.string.title_local);
                    fragment = new LocateFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_saved:
                    mTextMessage.setText(R.string.title_saved);
                    fragment = new SavedFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //attaching bottom sheet behavior - hide / show on scroll
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        //load the home fragment by default
        loadFragment(new PhotoGalleryFragment());
    }

    private void loadFragment(Fragment fragment) {
        //load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //create the database
    public void createSparksDb() {
        final SQLiteDatabase[] sparksDb = new SQLiteDatabase[1];
        SparksBaseHelper.getInstance(this).getWritableDatabase(new SparksBaseHelper.onDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                sparksDb[0] = db;
            }
        });
    }

}
