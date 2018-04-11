package com.bandw.sparks.fragments;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import android.widget.ProgressBar;

import com.bandw.sparks.AppUtils;
import com.bandw.sparks.FlickrFetchr;
import com.bandw.sparks.GalleryItem;
import com.bandw.sparks.QueryPreferences;
import com.bandw.sparks.R;
import com.bandw.sparks.ThumbnailDownloader;
import com.bandw.sparks.activities.PhotoPageActivity;
import com.bandw.sparks.db.Image;
import com.bandw.sparks.db.ImageDao;
import com.bandw.sparks.db.SparksDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mRecyclerView;
    private PhotoAdapter mPhotoAdapter;
    private List<GalleryItem> mGalleryItems = new ArrayList<>();
    private int mCurrentPage;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private Handler mHandler;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mQuery;

    public PhotoGalleryFragment() {
        // Required empty public constructor
    }

    //Enforcing a singleton pattern
    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
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
                });
        // start FetchItems AsyncTAsk
        updateItems();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photogallery, container, false);
        mProgressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setUpAdapter();
        mRecyclerView.setAdapter(mPhotoAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLastItemDisplaying()) {
                    mCurrentPage++;
                    updateItems();
                }
            }
        });
        ViewTreeObserver viewTreeObserver = mRecyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateCellSize();
            }
        });
        mSwipeRefreshLayout = view.findViewById(R.id.photo_gallery_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Refresh items
                refreshItems();
            }

            void refreshItems() {
                //Load items
                if (mQuery==null) {
                    submitQuery(null);
                } else {
                    submitQuery(mQuery);
                }
                onItemsLoadComplete();
            }

            void onItemsLoadComplete() {
                //Stop refresh animation
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem menuItemSearch = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) menuItemSearch.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                mQuery = query;
                AppUtils.hideKeyboard(getActivity(), searchView);
                menuItemSearch.collapseActionView();
                showProgressBar();
                submitQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getSearchQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    private void submitQuery(String query) {
        QueryPreferences.setSearchQuery(getActivity(), query);
        mCurrentPage = 0;
        mGalleryItems = new ArrayList<>();
        mPhotoAdapter.setItems(mGalleryItems);
        updateItems();
    }

    private void updateItems() {
        String query = QueryPreferences.getSearchQuery(getActivity());
        FetchItemsTask fetchItemsTask = new FetchItemsTask(query);
        fetchItemsTask.execute(mCurrentPage);
    }

    private void hideProgressBar() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showProgressBar() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private static final int sColumnWidth = 120;

    private void calculateCellSize() {
        int spanCount = (int) Math.ceil(mRecyclerView.getWidth() / convertDPToPixels(sColumnWidth));
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

    private boolean isLastItemDisplaying() {
        RecyclerView.Adapter galleryAdapter = mRecyclerView.getAdapter();
        if (galleryAdapter != null && galleryAdapter.getItemCount() != 0) {
            int lastVisibleItemPosition = ((GridLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == galleryAdapter.getItemCount() - 1;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        private String mQuery;

        FetchItemsTask(String query) { mQuery = query; }

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            Log.i(TAG, "doInBackground(" +params[0] + ")");

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos(params[0]);
            } else {
                return new FlickrFetchr().searchPhotos(params[0], mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            Log.i(TAG, "onPostExecute()");
            mGalleryItems.addAll(galleryItems);
            hideProgressBar();
            setUpAdapter();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {
        private ImageView mImageView;
        private GalleryItem mGalleryItem;

        PhotoHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }

        public void bindItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            startActivity(PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri()));
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "Long Pressed!");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_save_url)
                    .setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO: Save to database
                            Log.d(TAG, "Save to database");
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //User cancelled the dialog
                        }
                    });
            builder.create().show();
            return true;
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        List<GalleryItem> mItems;

        PhotoAdapter(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_gallery, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mItems.get(position);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getURL());

            //add a placeholder picture
            Drawable placeholder;
            placeholder = getResources().getDrawable(R.drawable.tommy, Objects.requireNonNull(getActivity()).getTheme());
            holder.bindDrawable(placeholder);
            holder.bindItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        void setItems(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            notifyDataSetChanged();
        }
    }


}
