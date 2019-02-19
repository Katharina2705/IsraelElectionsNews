package com.example.android.israelelectionsnews;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity to handle the app's settings so the user can request NewsItems objects according to
 * their date of publication and/or a predefined list of topics
 * <p>
 * The layout is handled in
 * a) res/menu/main.xml (including the menu bar + items)
 * b) res/layout/settings_activity.xml (including the settings fragment)
 * c) res/xml/settings_main.xml (including the Preference Screen)
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }


    public static class NewsItemsPreferenceFragment extends PreferenceFragmentCompat implements
            Preference.OnPreferenceChangeListener {

        /**
         * method to update the screen if preferences have changed
         *
         * @param preference is the reference to the preference
         * @param myObject   is the changed preference object
         * @return boolean to indicate whether change has occurred
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object myObject) {
            // update changed preference in the View
            String changedString = myObject.toString();

            // if preference is an item of ListPreference, find its label via its index and display
            // on the screen
            if (preference instanceof ListPreference) {
                ListPreference myListPreference = (ListPreference) preference;
                int preferenceIndex = myListPreference.findIndexOfValue(changedString);
                if (preferenceIndex >= 0) {
                    CharSequence[] labels = myListPreference.getEntries();
                    preference.setSummary(labels[preferenceIndex]);
                }
                // check if input of days since publication is valid - if not, set default value
            } else if (preference instanceof EditTextPreference) {
                if (!changedString.equals(" ") && TextUtils.isDigitsOnly(changedString)) {
                    preference.setSummary(changedString);
                } else {
                    Toast.makeText(getContext(), "Only numbers allowed.", Toast.LENGTH_LONG).show();
                    preference.setSummary(getString(R.string.settings_published_since_days_default));
                }
            } else {
                preference.setSummary(changedString);
            }
            return true;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            setPreferencesFromResource(R.xml.settings_main, s);

            // find preference of publication date (EditTextView) and display to View
            Preference publishedSinceDays = findPreference(getString(R.string.settings_published_since_days));
            bindPreferenceSummaryToValue(publishedSinceDays);

            //find preference of topic (ListPreference) and display to View
            Preference orderByTopic = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderByTopic);
        }

        /**
         * method to bind the selected preference to the OnPreferenceChangeListener
         *
         * @param preference is the selected preference
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String getPreference = myPreference.getString(preference.getKey(), "");
            onPreferenceChange(preference, getPreference);
        }
    }
}
