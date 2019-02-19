package com.example.android.israelelectionsnews;

import java.util.ArrayList;

/**
 * this class defines all attributes of those articles that should be fetched
 * from the API and presented in the app
 *
 * @param headline    is the title of the article
 * @param author      is the author of the article
 * @param date        is the day on which the article was first published
 * @param thumbnailID is an integer referring to the resource ID of the article's thumbnail
 * @param keyword     is an ArrayList of keywords/tags the article is marked with
 * @param section     is the section the article belongs to
 * @param webURL      is the URL linking to the entire article on guardian.com
 */

public class NewsItems {
    private String headline;
    private String author;
    private String date;
    private String thumbnailId;
    private ArrayList<String> keywords;
    private String section;
    private String webURL;

    public NewsItems(String headline, String author, String date, String thumbnailId, ArrayList<String> keywords,
                     String section, String webURL) {
        this.headline = headline;
        this.author = author;
        this.date = date;
        this.thumbnailId = thumbnailId;
        this.keywords = keywords;
        this.section = section;
        this.webURL = webURL;
    }

    public String getHeadline() {
        return headline;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }

    public ArrayList<String> getKeyword() {
        return keywords;
    }

    public String getSection() {
        return section;
    }

    public String getWebURL() {
        return webURL;
    }
}
