package com.example.android.israelelectionsnews;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * This app provides news by the Guardian about the Israeli national elections 2019, collected
 * through the Guardian API (https://open-platform.theguardian.com).
 *
 * Users can retrieve a list of news articles according to its publication date and/or a list
 * of predefined topics (candidates, political parties or significant keywords).
 * It allows to share single articles via WhatsApp and redirection to the full article text on the
 * Guardian website.
 *
 * Articles are represented by the NewsItems class, handled by the NewsItemsAdapter and displayed
 * in a RecyclerView
 */

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<ArrayList<NewsItems>> {

    /* String for log messages */
    public static final String LOG_TAG = MainActivity.class.getName();
    /* int representing Loader ID */
    private static final int EARTHQUAKE_LOADER_ID = 1;
    /* base URL String to build URI from for fetching data through API */
    private static final String GUARDIAN_API_URL = "https://content.guardianapis.com/search?api-key=bce90022-27d6-496a-aeb9-0305e2b2b6c5";
    /* TextViews displaying no data or no internet connection */
    private TextView emptyView;
    private TextView internetView;
    /* ProgressBar View to inform user about progress of fetching data */
    private ProgressBar progressBar;
    /* RecyclerViewAdapter instance */
    private NewsItemAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyView = findViewById(R.id.empty);
        internetView = findViewById(R.id.internet);
        progressBar = findViewById(R.id.progressbar);

        // reference and instantiate Loader
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);

        // check whether the device has an internet connection and display TextView with error if not
        if (!checkIfOnline()) {
            internetView.setText(R.string.internet);
        } else {
            internetView.setVisibility(View.GONE);
        }
    }

    /* a method to create a Loader and build an URI according to the user's preferences */
    @NonNull
    @Override
    public Loader<ArrayList<NewsItems>> onCreateLoader(int i, @Nullable Bundle bundle) {
        // get most recent preferences
        SharedPreferences selectedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // get preference for days since publication
        String daysSincePublication = selectedPreferences.getString(getString
                        (R.string.settings_published_since_days),
                getString(R.string.settings_published_since_days_default));
        String preferredTimeSpan = getPublicationStart(daysSincePublication);

        // get preference for topic
        String preferredTopic = selectedPreferences.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_topic_value11));

        // prepare URI to be modified
        Uri baseURI = Uri.parse(GUARDIAN_API_URL);
        Uri.Builder uriBuilder = baseURI.buildUpon();

        // append submitted preference to URI to get the requested data
        uriBuilder.appendQueryParameter("format", "json");
        uriBuilder.appendQueryParameter("q", preferredTopic);
        uriBuilder.appendQueryParameter("lang", "en");
        uriBuilder.appendQueryParameter("page-size", "30");
        uriBuilder.appendQueryParameter("from-date", preferredTimeSpan);
        uriBuilder.appendQueryParameter("show-fields", "headline,thumbnail");
        uriBuilder.appendQueryParameter("show-tags", "keyword,contributor");

        return new NewsItemsLoader(this, uriBuilder.toString());
    }

    /**
     * a method to update the UI with JSON data after the loading process has finished
     *
     * @param loader   is the loader handling newsItems
     * @param articles an ArrayList of NewsItems objects with JSON data
     */
    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<NewsItems>> loader,
                               ArrayList<NewsItems> articles) {
        // hide ProgressBar View
        progressBar.setVisibility(View.GONE);
        updateUI(articles);
    }

    /**
     * method to clear adapter from old data when data finished loading
     *
     * @param loader is the Loader handling the data processing
     */
    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<NewsItems>> loader) {
        loader.reset();
    }

    /**
     * method to create an adapter instance with NewsItems objects and to set it to the RecyclerView
     * show empty View if no data available
     *
     * @param articles is an ArrayList of NewsItems objects that they adapter is handling
     */
    public void updateUI(ArrayList<NewsItems> articles) {
        RecyclerView rootView = findViewById(R.id.list_container);
        if (articles.isEmpty()) {
            emptyView.setText(R.string.nodata);
            rootView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        newsAdapter = new NewsItemAdapter(this, articles);
        rootView.setAdapter(newsAdapter);
        rootView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * method to take in the user input of days since publication and finds earliest date since publication
     *
     * @param preferredDayNumber is the user input of his/her preferred number of days since publication
     * @return the earliest date of publication as a String
     */
    public String getPublicationStart(String preferredDayNumber) {
        int numberofDays = Integer.parseInt(preferredDayNumber);
        LocalDate today = LocalDate.now().minusDays(numberofDays);
        return today.toString();
    }

    /**
     * method to check whether the device is connected to the internet
     *
     * @return true/false
     */
    public boolean checkIfOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo getNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (getNetworkInfo != null && getNetworkInfo.isConnected());
    }

    /* create Settings menu and inflate it with a menu layout file */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * method to execute SettingsActivity if an item in the settings menu is clicked
     *
     * @param item is an item in the menu list
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings_menu) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}