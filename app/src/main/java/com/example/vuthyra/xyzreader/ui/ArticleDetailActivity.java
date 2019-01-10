package com.example.vuthyra.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;

import android.support.v4.content.Loader;


import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;


import com.example.vuthyra.xyzreader.R;
import com.example.vuthyra.xyzreader.data.ArticleLoader;
import com.example.vuthyra.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;


/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */


public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    private View mSelectedView;
    private Cursor mCursor;
    private long mStartId;
    private OnPageChangeListener mListener;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private int mSelectedPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        //Get current selected position of ViewPager object.
        mSelectedPosition = mPager.getCurrentItem();


        final FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                startActivity(Intent.createChooser(
                        ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                                .setType("text/plain").setText("Send this article to: ")
                                .getIntent(), getString(R.string.action_share)));


            }

        });

        mListener = new OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mCursor.moveToPosition(position);


                String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
                ImageView backdrop = findViewById(R.id.backdrop);

                Picasso.get().load(photoUrl).into(backdrop);


                //implementing View from current position of ViewPager,
                //then make it populate into the Snackbar as article title.

                mSelectedView = mPager.getChildAt(mSelectedPosition);
                String titleArticle = mCursor.getString(ArticleLoader.Query.TITLE);
                Snackbar snackbar = Snackbar
                        .make(mSelectedView, titleArticle, Snackbar.LENGTH_LONG);
                snackbar.show();


            }

            @Override
            public void onPageScrollStateChanged(int state) {

                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        fab.hide();
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        fab.show();
                        break;
                }
            }
        };


        mPager.addOnPageChangeListener(mListener);

        if (savedInstanceState == null) {

            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }

    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {

        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();
        mPager.post(new Runnable() {

            @Override
            public void run() {
                mListener.onPageSelected(mPager.getCurrentItem());
            }

        });

        // Select the start ID
        if (mStartId > 0) {

            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {

                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {

                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;

                }
                mCursor.moveToNext();

            }
            mStartId = 0;

        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {

        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();

    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

    }

}


