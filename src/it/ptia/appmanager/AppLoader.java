package it.ptia.appmanager;

import java.util.Collections;
import java.util.List;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class AppLoader extends AsyncTaskLoader<List<ApplicationInfo>> {
	Context context;
	PackageManager pm;
	List<ApplicationInfo> apps;
	public AppLoader(Context context) {
		super(context);
		this.context=context;
		pm=context.getPackageManager();
	}
	@Override
	public List<ApplicationInfo> loadInBackground() {
		apps=pm.getInstalledApplications(0);
		int i=0;
		Collections.sort(apps,new ApplicationInfo.DisplayNameComparator(pm));
		return apps;
	}
	@Override
	protected void onStartLoading()  {
		Log.d(context.getPackageName(),"Start Loading Apps");
		forceLoad();
	}
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
		Log.d(context.getPackageName(),"Stop loading apps, loaded "+apps.size());
        cancelLoad();
        
    }
    @Override protected void onReset() {
    	apps=null;
    }
}
