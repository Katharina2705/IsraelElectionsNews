package com.example.android.israelelectionsnews;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * a custom RecyclerView Adapter to handle NewsItems objects and properly set them
 * to their corresponding Views
 */

public class NewsItemAdapter extends RecyclerView.Adapter<NewsItemAdapter.ViewHolder> {

    /* an ArrayList of articles */
    private ArrayList<NewsItems> newsItems;
    private Context context;

    public NewsItemAdapter(Context context, ArrayList<NewsItems> newsItems) {
        this.newsItems = newsItems;
        this.context = context;
    }

    /**
     * create new ViewHolder and inflate the news_items.xml layout file with data
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.news_items, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * retrieve the data for the article at a particular list position and bind it to their views
     * create two onClickListeners for
     *  1) redirecting to the article's WebURL
     *  2) sharing the article's webURL via WhatsApp
     */
    @Override
    public void onBindViewHolder(@NonNull NewsItemAdapter.ViewHolder viewHolder, int position) {
        final NewsItems newItem = newsItems.get(position);

        viewHolder.titleView.setText(newItem.getHeadline());
        viewHolder.shareView.setText(R.string.share);
        viewHolder.sectionView.setText(context.getString(R.string.placeholder, newItem.getSection()));

        // check if attribute author is not null
        if (newItem.getAuthor() != null) {
            viewHolder.authorView.setText(newItem.getAuthor());
        } else {
            viewHolder.authorView.setVisibility(View.GONE);
        }

        // retrieve keywords and limit their number to max. three
        ArrayList<String> keywordArray = newItem.getKeyword();
        if (keywordArray.size() >= 3) {
            viewHolder.keyword1View.setText(context.getString(R.string.hashtag, keywordArray.get(0)));
            viewHolder.keyword2View.setText(context.getString(R.string.hashtag, keywordArray.get(1)));
            viewHolder.keyword3View.setText(context.getString(R.string.hashtag, keywordArray.get(2)));
        } else if (keywordArray.size() == 2) {
            viewHolder.keyword1View.setText(context.getString(R.string.placeholder, keywordArray.get(0)));
            viewHolder.keyword2View.setText(keywordArray.get(1));
            viewHolder.keyword3View.setVisibility(View.GONE);
        } else if (keywordArray.size() == 1) {
            viewHolder.keyword1View.setText(keywordArray.get(0));
            viewHolder.keyword2View.setVisibility(View.GONE);
            viewHolder.keyword3View.setVisibility(View.GONE);
        } else {
            viewHolder.keyword1View.setVisibility(View.GONE);
            viewHolder.keyword2View.setVisibility(View.GONE);
            viewHolder.keyword3View.setVisibility(View.GONE);
        }

        // create a substring of the requested DateTime String only including the date
        // and display in local format
        String justDate = newItem.getDate().substring(0, 10);
        String formattedDate = formatDate(justDate);
        viewHolder.dateView.setText(formattedDate);

        // use the Picasso library to load the article's thumbnail from an URL String to ImageView
        Picasso.get().load(newItem.getThumbnailId()).into(viewHolder.thumbnailView);
        viewHolder.whatsappIconView.setImageResource(R.drawable.whatsappicon);

        // create an onClickListener for opening the full article text via its webURL
        viewHolder.titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openURL = new Intent(Intent.ACTION_VIEW, Uri.parse(newItem.getWebURL()));
                try {
                    context.startActivity(openURL);
                } catch (ActivityNotFoundException notFound) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                }
            }
        });

        // create an onClickListener for sharing the article's webURL via WhatsApp
        viewHolder.shareViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendWhatsAppMessage = new Intent(Intent.ACTION_SEND);
                sendWhatsAppMessage.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.forward_message) + newItem.getWebURL());
                sendWhatsAppMessage.setType("text/plain");
                sendWhatsAppMessage.setPackage("com.whatsapp");
                try {
                    context.startActivity(sendWhatsAppMessage);
                } catch (ActivityNotFoundException notFound) {
                    Toast.makeText(context, R.string.error_whatsapp, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    /**
     * method to convert date to a local format
     *
     * @param dateObject is the JSON String in ISO 8601
     */
    @SuppressLint("NewApi")
    private String formatDate(String dateObject) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateFormatter = dateFormatter.withLocale(Locale.getDefault());
        LocalDate localDate = LocalDate.parse(dateObject, dateFormatter);
        return localDate.toString();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sectionView;
        TextView titleView;
        TextView dateView;
        ImageView thumbnailView;
        TextView authorView;
        TextView keyword1View;
        TextView keyword2View;
        TextView keyword3View;
        TextView shareView;
        ImageView whatsappIconView;
        LinearLayout shareViewContainer;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // find views that should be inflated with data
            sectionView = itemView.findViewById(R.id.section_view);
            titleView = itemView.findViewById(R.id.title_view);
            dateView = itemView.findViewById(R.id.date_view);
            thumbnailView = itemView.findViewById(R.id.thumbnail_view);
            authorView = itemView.findViewById(R.id.author_view);
            keyword1View = itemView.findViewById(R.id.keyword1_view);
            keyword2View = itemView.findViewById(R.id.keyword2_view);
            keyword3View = itemView.findViewById(R.id.keyword3_view);
            shareView = itemView.findViewById(R.id.share_view);
            whatsappIconView = itemView.findViewById(R.id.whatsapp_icon);
            shareViewContainer = itemView.findViewById(R.id.share_view_container);
        }
    }
}
