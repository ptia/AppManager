package it.ptia.appmanager;
import android.net.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;
import it.ptia.appmanager.R;
import java.io.*;
import java.nio.channels.*;
import java.util.*;

import android.app.*;
import android.view.*;
import android.widget.*;
import android.preference.*;
import android.provider.Settings;

public class AppListActivity extends Activity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<List<ApplicationInfo>>, SearchView.OnQueryTextListener {
  public AppAdapter adapter=null;
  public SpinnerAdapter abSpinnerAdapter;
  int pkgType=0;
  public AbsListView list;
  public String query;
  ProgressBar progressBar;
  public void selectAll() {
    int count = list.getCount();
    for(int i=0; i<count; ++i) 
    {
		list.setItemChecked(i, true);
	}
  }
	//ArrayList<ApplicationInfo> cabSelectedItems;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		list=(AbsListView)findViewById(R.id.app_list);
		progressBar=(ProgressBar)findViewById(R.id.main_progressbar);
		list.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		adapter=new AppAdapter(this);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		setLongClick();
		getLoaderManager().initLoader(0, null, this);
		setTitle(getString(R.string.app_name));
	}
	public ArrayList<ApplicationInfo> getFilteredResults(CharSequence constraint,List<ApplicationInfo> list) {
		PackageManager pm=getPackageManager();
		ArrayList<ApplicationInfo> result=new ArrayList<ApplicationInfo>();
		for (ApplicationInfo e:list) {
			Log.w(getPackageName(),"Controllando la presenza di "+constraint+" in "+e.loadLabel(pm).toString().toLowerCase());
			if(e.loadLabel(pm).toString().toLowerCase().contains(constraint)) {
				result.add(e);
				Log.w(getPackageName(),"Approvato "+e.loadLabel(pm));
			}
		}
		return result;
	}
	public void setLongClick() {
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
			//ArrayList<int> checkedItems;
			ArrayList<ApplicationInfo>cabSelectedItems;
		    @Override
		    public void onItemCheckedStateChanged(ActionMode mode, int position,long id, boolean checked) {
				//list.setItemChecked(position+1,true);
    		    ApplicationInfo info=adapter.getItem(position);
    		    if(checked) {
    			    cabSelectedItems.add(info);
    			}
    			else {
    			ApplicationInfo el;
    			int i=0;
    				while(i<cabSelectedItems.size()) {
    					el=cabSelectedItems.get(i);
    					if (info.equals(el)) {
    						cabSelectedItems.remove(el);
  						}
  						i++;
  					}
    			}
    			if(cabSelectedItems.size()<=1) {
    				mode.setTitle(getString(R.string.selected_one)+ cabSelectedItems.size()+" app");
    			}
    			else {
    				mode.setTitle(getString(R.string.selected_multi)+ cabSelectedItems.size()+" apps");
    			}
				mode.invalidate();
		    }
			    @Override
		    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		        // Respond to clicks on the actions in the CAB
				ArrayList<File> files;
		        switch (item.getItemId()) {
		            case R.id.menu_share:
		            	files=new ArrayList<File>();
		            	for(ApplicationInfo info:cabSelectedItems) {
		            		files.add(new File(info.publicSourceDir));
		            	}
		                shareMultipleApps(files);
		                //cabSelectedItems=null;
		                mode.finish(); // Action picked, so close the CAB
		                return true;
					case R.id.menu_backup:
						Log.w(getPackageName(),"CAB SELECTED ITEMS="+cabSelectedItems.toString());
					    /*new BackupTask().execute(cabSelectedItems);
					    mode.finish();*/
						ArrayList<String> filesPath=new ArrayList<String>();
		            	for(ApplicationInfo info:cabSelectedItems) {
		            		filesPath.add(info.loadLabel(getPackageManager())+"&&!!"+new File(info.publicSourceDir).getAbsolutePath());
		            	}
						Log.w(getPackageName(),"Files di backup"+filesPath);
						Intent backup= new Intent(AppListActivity.this, BackupService.class);
						backup.putStringArrayListExtra("backupFiles",filesPath);
						startService(backup);
						mode.finish();
						return true;
						//selectAll();
						//return true;
					case R.id.menu_uninstall:
					    Uninstaller uninstaller=new Uninstaller(AppListActivity.this,getPackageManager());
						uninstaller.uninstall(cabSelectedItems.get(0));
						mode.finish();
						return true;
					case R.id.menu_selectAll:
	    				cabSelectedItems.clear();
	    				selectAll();
						mode.invalidate();
						return true;
					case R.id.menu_app_info:
						Intent info=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						info.setData(Uri.parse("package:"+cabSelectedItems.get(0).packageName));
						startActivity(info);
						return true;
					case R.id.menu_open_app:
						try {
							Intent start=getPackageManager().getLaunchIntentForPackage(cabSelectedItems.get(0).packageName);
							startActivity(start);
						}
						catch (NullPointerException e) {
							Log.e(getPackageName(),e.toString());
						}
						return true;
		            default:
           			    return false;
       			}
 			}
  			@Override
		    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		        // Inflate the menu for the CAB
		        cabSelectedItems=new ArrayList<ApplicationInfo>();
		        MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.cab, menu);
		        return true;
		    }
		    @Override
		    public void onDestroyActionMode(ActionMode mode) {
		    	//cabSelectedItems.clear();
		        // Here you can make any necessary updates to the activity when
		        // the CAB is removed. By default, selected items are deselected/unchecked.
		    }
		    @Override
		    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		        // Here you can perform updates to the CAB due to
		        // an invalidate() request
		    	PackageManager pm=getPackageManager();
				boolean root=PreferenceManager.getDefaultSharedPreferences(AppListActivity.this).getBoolean("pref_root",false);
				ApplicationInfo firstItem=null;
				try {
					firstItem=cabSelectedItems.get(0);
				}
				catch (Exception e) {
					//If no items are selected, we cannot control them, so inflate the normal menu and exit
					menu.clear();
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.cab, menu);
					return true;
				}
				//Reload menu
				menu.clear();
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.cab, menu);
				//If more then 1 app is selected, then remove info, uninstall and launch from menu and stop controls
		        if(cabSelectedItems.size()>=2) {
		        	Log.d(getPackageName(), "Removing menu elements info, uninstall and launch because more than 1 element is selected");
					menu.removeItem(R.id.menu_app_info);
					menu.removeItem(R.id.menu_uninstall);
					menu.removeItem(R.id.menu_open_app);
					return true;
				}
				//If NOT(Only 1 app is selected AND(it is NOT system app OR root is enabled)), then remove uninstall from menu 
				if(!((!firstItem.publicSourceDir.startsWith("/system"))||root)){
					Log.d(getPackageName(), "Removing element uninstall");
					menu.removeItem(R.id.menu_uninstall);
				}
				//If NOT(Only 1 app is selected AND it is a launchable app), then remove 
		        if(!(pm.getLaunchIntentForPackage(firstItem.packageName)!=null)) {
		        	Log.d(getPackageName(), "Removing element open app");
		        	menu.removeItem(R.id.menu_open_app);
		        }
		        return true;
		    }
		});
	}
  	public void shareMultipleApps(ArrayList<File> apps) {
		ArrayList<Uri> uris=new ArrayList<Uri>();
		Intent i=new Intent();
		i.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
		i.setType("*/*");
		for(File file:apps)
		{
			uris.add(Uri.fromFile(file));
		}
		i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		startActivity(Intent.createChooser(i, getString(R.string.share)+uris.size()+" apps"));
	}
  	public void showHelp() {
  		AlertDialog.Builder dialog=new AlertDialog.Builder(this);
  		dialog.setTitle(R.string.guide_title);
  		dialog.setMessage(R.string.guide);
  		dialog.show();
  	}
  	public void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
    	}
	    FileChannel source = null;
	    FileChannel destination = null;
	    try {
    	    source = new FileInputStream(sourceFile).getChannel();
    	    destination = new FileOutputStream(destFile).getChannel();
    	    destination.transferFrom(source, 0, source.size());
    	}
    	
    	finally {
    	    if(source != null) {
    	        source.close();
    	    }
    	    if(destination != null) {
    	        destination.close();
    	    }
    	}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		// Get the SearchView and set the searchable configuration
        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    searchView.setOnQueryTextListener(this);
    	searchView.setIconifiedByDefault(true);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent i=new Intent(this, SettingsActivity.class);
				startActivity(i);
				return true;
			case R.id.menu_backup_list:
	    		Intent ibck=new Intent(this, BackupListActivity.class);
				startActivity(ibck);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	ProgressDialog pd;
	public void startLoading(){
		pd = ProgressDialog.show(AppListActivity.this, "",getString(R.string.loading),true,false,null);
	}
	public void stopLoading(){
		pd.dismiss();
		//showToast(R.string.backup_done, Toast.LENGTH_SHORT);
	}
	public void showToast(CharSequence text,int duration) {
		Toast toast = Toast.makeText(getApplicationContext(), text, duration);
		toast.show();
	}
	public void showToast(int text,int duration) {
		Toast toast = Toast.makeText(getApplicationContext(), text, duration);
		toast.show();
	}
  	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		list.setItemChecked(position,true);
	}
  	@Override
	public android.content.Loader<List<ApplicationInfo>> onCreateLoader(int id,Bundle args) {
		return new AppLoader(this);
	}
	@Override
	public void onLoadFinished(android.content.Loader<List<ApplicationInfo>> loader,List<ApplicationInfo> data) {
		Log.d(getPackageName(),"Load finished, loaded "+data.size());
		if(query!=null) {
			data=getFilteredResults(query,data);
		}
		adapter.setData(data);
		list.setAdapter(adapter);
		progressBar.setVisibility(View.GONE);
		list.setVisibility(View.VISIBLE);
	}
	@Override
	public void onLoaderReset(android.content.Loader<List<ApplicationInfo>> arg0) {
		adapter.setData(null);
		list.setAdapter(adapter);
	}
	@Override
	public boolean onQueryTextChange(String text) {
		this.query=text;
		if(query.equals("")) {
			this.query=null;
		}
		list.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		getLoaderManager().initLoader(0, null, this);
		return true;
	}
	@Override
	public boolean onQueryTextSubmit(String query) {
		//Do nothing
		return false;
	}
}
