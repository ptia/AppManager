package it.ptia.appmanager.help;

import android.os.*;
import android.preference.*;
import android.app.AlertDialog;
import android.content.*;

import it.ptia.appmanager.R;

/**
 * Created by edo on 05/08/13.
 */
public class Help extends PreferenceActivity {
    Preference shareSingle, shareMulti, backup, restore, root, rootFeatures;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.help);
        shareSingle=(Preference)findPreference("help_share_single");
        shareSingle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//new AlertDialog.Builder(PreferenceActivity.this).setMessage(R.string.root_what_is);
				return false;
			}
		});
        shareMulti=(Preference)findPreference("help_share_multi");
        backup=(Preference)findPreference("help_backup");
        restore=(Preference)findPreference("help_restore");
        root=(Preference)findPreference("help_what_is_root");
        rootFeatures=(Preference)findPreference("help_root_features");
    }
}
