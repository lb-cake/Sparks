package com.bandw.sparks.fragments;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bandw.sparks.QueryPreferences;
import com.bandw.sparks.ThumbnailDownloader;
import com.bandw.sparks.db.GalleryLab;
import com.bandw.sparks.db.GalleryItem;
import com.bandw.sparks.R;
import com.bandw.sparks.activities.PhotoPageActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedFragment extends Fragment {
    private static final String TAG = "SavedFragment";
    private RecyclerView mRecyclerView;
    private PhotoAdapter mPhotoAdapter;
    private List<GalleryItem> mGalleryItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private Handler mHandler;


    public SavedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Orientation change handling
        setRetainInstance(true);
        // Toolbar Menu
        setHasOptionsMenu(true);
        // init UI Thread Handler
        mHandler = new Handler();
        // init HandlerThread (Message Loop)
        mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(final PhotoHolder target, final Bitmap bitmap) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                                target.bindDrawable(drawable);
                            }
                        });
                    }
                }
        );
        //start FetchItems AsyncTask
        updateItems();
    }

    //FIXME
    private void updateItems() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved, container, false);
        mRecyclerView = view.findViewById(R.id.saved_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setUpAdapter();
        mRecyclerView.setAdapter(mPhotoAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        ViewTreeObserver viewTreeObserver = mRecyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateSize();
            }
        });
        updateUI();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_saved, menu);

        //If someone clicks the Settings icon on the Menu Bar
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

    private static final int sColumnWidth = 120;

    private void calculateSize() {
        int spanCount = (int) Math.ceil(mRecyclerView.getWidth() / convertDPToPixels(sColumnWidth));
        //if user did not save any images
        if (spanCount ==0)
            spanCount = 1;
        ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(spanCount);
    }

    private float convertDPToPixels(int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return dp * logicalDensity;
    }

    private void setUpAdapter() {
        if (isAdded()) {
            if (mPhotoAdapter == null) {
                mPhotoAdapter = new PhotoAdapter(mGalleryItems);
            } else {
                mPhotoAdapter.setItems(mGalleryItems);
            }
        }
    }

    private void updateUI() {
        GalleryLab galleryLab = GalleryLab.get(getActivity());
        List<GalleryItem> items = galleryLab.getGalleryItems();
        mPhotoAdapter = new PhotoAdapter(items);
        mRecyclerView.setAdapter(mPhotoAdapter);
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mImageView;
        private GalleryItem mGalleryItem;

        PhotoHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }

        void bindDrawable(Drawable drawable) { mImageView.setImageDrawable(drawable); }
        void bindItem(GalleryItem galleryItem) { mGalleryItem = galleryItem; }

        @Override
        public void onClick(View view) {
            Log.i(TAG, mGalleryItem.toString());
            startActivity(PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri()));
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        List<GalleryItem> mItems;

        PhotoAdapter(List<GalleryItem> galleryItems) { mItems = galleryItems; }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_gallery, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mItems.get(position);

            //add a placeholder picture
            Drawable placeholder;
            placeholder = getResources().getDrawable(R.drawable.tommy, Objects.requireNonNull(getActivity()).getTheme());
            holder.bindDrawable(placeholder);
            holder.bindItem(galleryItem);
        }

        @Override
        public int getItemCount() { return mItems.size(); }

        void setItems(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            notifyDataSetChanged();
        }
    }

}
