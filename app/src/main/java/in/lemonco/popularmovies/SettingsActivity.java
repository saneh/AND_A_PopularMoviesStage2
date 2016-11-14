package in.lemonco.popularmovies;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sort_order);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_key)));
        MoviesFragment.setPrefChange(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void bindPreferenceSummaryToValue(Preference pref){
        pref.setOnPreferenceChangeListener(this);

        onPreferenceChange(pref, PreferenceManager.getDefaultSharedPreferences(pref.getContext()).getString(pref.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference,Object value){
        String stringValue = value.toString();
        if(preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if(prefIndex>0){
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }else
            preference.setSummary(stringValue);
        MoviesFragment.setPrefChange(true);

        return true;

    }


}
