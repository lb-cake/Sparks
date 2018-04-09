package com.bandw.sparks.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bandw.sparks.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedFragment extends Fragment {
    private static final String TAG = "SavedFragment";

    public SavedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        // Orientation change handling
        setRetainInstance(true);
        // Toolbar Menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_saved, menu);

        MenuItem settingButton = menu.findItem(R.id.menu_settings);
        settingButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Display the settings fragment
                assert getFragmentManager() != null;
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });
    }

}
