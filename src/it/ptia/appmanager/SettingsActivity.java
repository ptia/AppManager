package it.ptia.appmanager;
import android.os.Bundle;
import android.util.*;
import it.ptia.appmanager.R;
import java.io.*;
import java.nio.channels.*;
import java.util.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.preference.*;
public class SettingsActivity extends PreferenceActivity {
  CheckBoxPreference myPref;
	EditTextPreference bckPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
		setRootSettings();
		/*getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);*/
		//bckPref=(EditTextPreference)findPreference("pref_bckDir");
    }
	public void setRootSettings() {
		myPref= (CheckBoxPreference)findPreference("pref_root");
		myPref.setOnPreferenceClickListener(new CheckBoxPreference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					if(myPref.isChecked()){
						String[] cmd={"su","-c","ls /system"};
						String rv="";
						try
						{
							Process p=Runtime.getRuntime().exec(cmd);
							DataOutputStream dOs=new DataOutputStream(p.getOutputStream());
							DataInputStream dIs=new DataInputStream(p.getInputStream());
							dOs.writeBytes(cmd + "\nexit\n");
							dOs.flush();
							p.waitFor();
							while(dIs.available()>0) {
								rv+=dIs.readLine()+"\n";
							}
							//Toast.makeText(this,rv,Toast.LENGTH_SHORT).show();
						}
						catch (Exception e)
						{}
						if(!rv.equals("")) {
							myPref.setChecked(true);
							return true;
						}
						else{
							myPref.setChecked(false);
							Toast.makeText(SettingsActivity.this,"No root", Toast.LENGTH_SHORT);
						}
						return false;}
					return true;
				}
			});
	}
}
