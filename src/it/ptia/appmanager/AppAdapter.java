package it.ptia.appmanager;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppAdapter extends ArrayAdapter<ApplicationInfo> {
	private PackageManager pm=null;
    List<ApplicationInfo> apps;
    Activity context;
    AppAdapter(Activity context, List<ApplicationInfo> apps) {
    	super(context, R.layout.row, apps);
    	this.apps=apps;
    	this.context=context;
    	pm=context.getPackageManager();
    }
    AppAdapter(Activity context) {
    	super(context, R.layout.row);
    	this.context=context;
    	pm=context.getPackageManager();
    }
    public void setData(List<ApplicationInfo> data) {
        clear();
    	apps=data;
        if (data != null) {
            addAll(data);
        }
    }
    @Override
    public View getView(int position, View convertView,ViewGroup parent) {
    	Log.w(context.getPackageName(),"getView");
    	if (convertView==null) {
    		convertView=newView(parent);
    	}  
    	bindView(position, convertView);  
    	return(convertView);
    }
    private View newView(ViewGroup parent) {
		Log.w(context.getPackageName(),"newView");
		return(context.getLayoutInflater().inflate(R.layout.row, parent, false));
    }
    private void bindView(int position, View row) {
    	TextView label=(TextView)row.findViewById(R.id.label);
    	Log.w(context.getPackageName(),"bindView");
    	label.setText(getItem(position).loadLabel(pm));  
    	ImageView icon=(ImageView)row.findViewById(R.id.icon);  
    	icon.setImageDrawable(getItem(position).loadIcon(pm));
    }
}
