package com.example.android.israelelectionsnews;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A custom Loader to manage the multiple tasks of retrieving and displaying the
 * requested data
 */

public class NewsItemsLoader extends AsyncTaskLoader<ArrayList<NewsItems>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = NewsItemsLoader.class.getName();
    private String strings;

    public NewsItemsLoader(Context context, String strings) {
        super(context);
        this.strings = strings;
    }

    @Override
    public void onStartLoading() {
        forceLoad();
    }

    /**
     * method to execute helper methods within the Loader
     *
     * @return an ArrayList of NewsItems objects (articles)
     */
    @Override
    public ArrayList<NewsItems> loadInBackground() {
        if (strings == null) {
            return null;
        }
        ArrayList<NewsItems> articles = null;
        try {
            articles = QueryUtils.extractJSONData(strings);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Invalid request.");
        }
        return articles;
    }
}
