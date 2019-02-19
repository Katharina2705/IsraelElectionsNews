package com.example.android.israelelectionsnews;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * A class with helper methods to handle the retrieval of data via the API,
 * including handling the network connection, get and interpret data
 */

public final class QueryUtils {

    private Context context;

    private QueryUtils() {
    }

    /**
     * a method that establishes a network connection from a String URL, reads and extracts JSON
     * data with which it creates NewsItems objects (connects all following methods)
     *
     * @param urlString is a String containing the API URL
     * @return an ArrayList of NewsItems objects
     * @throws IOException if network connection cannot be established
     */
    public static ArrayList<NewsItems> extractJSONData(String urlString) throws IOException {
        if (urlString == null) {
            return null;
        }
        URL urlFromString = createURL(urlString);
        String rawJSONData = makeHttpRequest(urlFromString);
        return interpretJSONData(rawJSONData);
    }

    /**
     * creates an URL object from the original String URL
     *
     * @param urlInput is String representing an URL
     * @return URL object with String input
     */

    private static URL createURL(String urlInput) {
        URL newURLObject = null;
        try {
            newURLObject = new URL(urlInput);
        } catch (MalformedURLException e) {
            Log.e(MainActivity.LOG_TAG, "Invalid URL");
        }
        return newURLObject;
    }

    /**
     * a method that establishes a network connection, fetches JSON data in an InputStream and returns
     * it as a String
     *
     * @param urlObject is the urlObject to make the connection to
     * @return a String of JSON data
     * @throws IOException if InputStream cannot be established
     */
    private static String makeHttpRequest(URL urlObject) throws IOException {

        InputStream newInputStream = null;
        HttpURLConnection makeConnection = null;
        String jsonDataString = "";

        // check if urlObject is empty
        if (urlObject == null) {
            return null;
        }

        try {
            // connect to network with urlObject and send GET request
            makeConnection = (HttpURLConnection) urlObject.openConnection();
            makeConnection.setRequestMethod("GET");
            // set time limit to establish connection
            makeConnection.setConnectTimeout(10000);
            makeConnection.setReadTimeout(15000);
            makeConnection.connect();

            // check the response code from the server and extract JSON data via inputStream
            // if successful
            int responseCode = makeConnection.getResponseCode();
            if (responseCode == 200) {
                newInputStream = makeConnection.getInputStream();
                jsonDataString = readFromJSON(newInputStream);
            } else {
                Log.e(MainActivity.LOG_TAG, "Network connection failed, response code " +
                        responseCode);
            }
        } catch (IOException e) {
            Log.e(MainActivity.LOG_TAG, "IOException occurred.");
        }
        // disconnect / close inputStream and network connection if not null
        finally {
            if (newInputStream != null) {
                newInputStream.close();
            }
            if (makeConnection != null) {
                makeConnection.disconnect();
            }
        }
        return jsonDataString;
    }

    /**
     * a method that reads JSON data from an InputStream and returns it as a String
     *
     * @param myInputStream is an InputStream object containing raw JSON data
     * @return JSON data converted to a String
     * @throws IOException if JSON data cannot be read
     */
    private static String readFromJSON(InputStream myInputStream) throws IOException {
        // check if InputStream is empty
        if (myInputStream == null) {
            return null;
        }

        // create appropriate Readers to read and store JSON data in UTF-8
        InputStreamReader readInputStream = new InputStreamReader(myInputStream, Charset.forName("UTF-8"));
        BufferedReader myBufferedReader = new BufferedReader(readInputStream);

        // store each line of JSON data in a StringBuilder object and return it as a String
        StringBuilder myStringBuilder = new StringBuilder();
        String readLines = myBufferedReader.readLine();
        while (readLines != null) {
            myStringBuilder.append(readLines);
            readLines = myBufferedReader.readLine();
        }
        return myStringBuilder.toString();
    }

    /**
     * a method to extract JSON data values from a String of complete JSON data
     *
     * @param entireData is a String containing the entire requested JSON data
     * @return an ArrayList of NewsItems objects created from fetched JSON data
     */
    private static ArrayList<NewsItems> interpretJSONData(String entireData) {
        ArrayList<NewsItems> newsItems = new ArrayList<NewsItems>();

        try {
            // convert String to a JSON object and extract appropriate data
            JSONObject rootObject = new JSONObject(entireData);
            JSONObject responseObject = rootObject.getJSONObject("response");
            JSONArray articlesArray = responseObject.getJSONArray("results");

            // loop through each article object
            for (int pos = 0; pos <= articlesArray.length(); pos++) {
                JSONObject singleArticle = articlesArray.getJSONObject(pos);
                String sectionName = singleArticle.getString("sectionName");
                String webURL = singleArticle.getString("webUrl");
                String publicationDate = singleArticle.getString("webPublicationDate");
                JSONObject articleFields = singleArticle.getJSONObject("fields");
                String headline = articleFields.getString("headline");
                String thumbnail = articleFields.getString("thumbnail");

                // retrieve tags and store in an ArrayList
                JSONArray tagArray = singleArticle.getJSONArray("tags");
                ArrayList<String> allTags = storeTagItems(tagArray);
                String contributorName = getContributor(tagArray);

                newsItems.add(new NewsItems(headline, contributorName,
                        publicationDate, thumbnail, allTags, sectionName, webURL));
            }
        } catch (JSONException e) {
            Log.e(MainActivity.LOG_TAG, "A JSONException occurred in interpretJSONData()");
        }
        return newsItems;
    }

    /**
     * helper method to retrieve tags from JSON data and store in an ArrayList
     *
     * @param tagArray is a JSONArray containing tag JSONObject
     * @return ArrayList with all tag titles
     * @throws JSONException if title String cannot be accessed
     */
    private static ArrayList<String> storeTagItems(JSONArray tagArray) throws JSONException {
        ArrayList<String> allTags = new ArrayList<String>();
        for (int p = 0; p < tagArray.length(); p++) {
            JSONObject tagItem = tagArray.getJSONObject(p);
            if (tagItem.getString("type").equals("keyword")) {
                String tagTerm = tagItem.getString("webTitle");
                allTags.add(tagTerm);
            }
        }
        return allTags;
    }

    /**
     * a method to retrieve the author's name of the article
     * @param tagArray is a JSONArray containing tag JSONObject
     * @return the author's name as a String
     * @throws JSONException if title String cannot be accessed
     */
    private static String getContributor(JSONArray tagArray) throws JSONException {
        String authorName = "";
        for (int p = 0; p < tagArray.length(); p++) {
            JSONObject tagItem = tagArray.getJSONObject(p);
            String contributorTrue = tagItem.getString("type");
            if (contributorTrue.equals("contributor")) {
                authorName = tagItem.getString("webTitle");
                break;
            }
        }
        return authorName;
    }
}