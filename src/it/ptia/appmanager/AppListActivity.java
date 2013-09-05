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

public class AppListActivity extends Activity implements AdapterView.OnItemClickListener {
  public AppAdapter adapter=null;
  public SpinnerAdapter abSpinnerAdapter;
  int pkgType=0;
  public AbsListView list;
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
    adapter=new AppAdapter(getPackageManager(), getApplicationInfoList(0));
    list.setAdapter(adapter);
    list.setOnItemClickListener(this);
	setLongClick();
	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	  //get query
	  Intent intent = getIntent();
	  if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		  String query = intent.getStringExtra(SearchManager.QUERY);
		  //perform search
		  ArrayList<ApplicationInfo> filtered=getFilteredResults(query);
		  adapter=new AppAdapter(getPackageManager(), filtered);
		  list.setAdapter(adapter);
		  list.deferNotifyDataSetChanged();
		  Log.w(getPackageName(),"MainActivity");
		  if(!filtered.isEmpty()) {
			  getActionBar().setTitle(getString(R.string.searchable_results)+query+"\"");
		  }
		  else {
			  getActionBar().setTitle(getString(R.string.searchable_noresults)+query+"\"");
		  }
		 
    	}
	 
	  else if(Intent.ACTION_MAIN.equals(intent.getAction())){
		  setTitle(getString(R.string.app_name));
	  }
	}
	public ArrayList<ApplicationInfo> getFilteredResults(CharSequence constraint) {
		PackageManager pm=getPackageManager();
		ArrayList<ApplicationInfo> result=new ArrayList<ApplicationInfo>();
		for (ApplicationInfo e:getApplicationInfoList(0)) {
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
							return true;
							//selectAll();
							//return true;
						case R.id.menu_uninstall:
						    Uninstaller uninstaller=new Uninstaller(AppListActivity.this,getPackageManager());
							uninstaller.uninstall(cabSelectedItems.get(0));
							/*Intent i=new Intent(MainActivity.this, MainActivity.class);
							startActivity(i);*/
							return true;
						case R.id.menu_selectAll:
		    				cabSelectedItems.clear();
		    				selectAll();
							mode.invalidate();
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
			        //cabSelectedItems=new ArrayList<ApplicationInfo>();
					boolean root=PreferenceManager.getDefaultSharedPreferences(AppListActivity.this).getBoolean("pref_root",false);
					ApplicationInfo firstItem=null;
					//MenuItem uninstall=menu.getItem(R.id.menu_uninstall);
					try {
						firstItem=cabSelectedItems.get(0);
					}
					catch (Exception e) {
						menu.clear();
						MenuInflater inflater = mode.getMenuInflater();
						inflater.inflate(R.menu.cab, menu);
						return true;
					}
					if(cabSelectedItems.size()<2&&((!firstItem.publicSourceDir.startsWith("/system"))||root)) {
						menu.clear();
						MenuInflater inflater = mode.getMenuInflater();
						inflater.inflate(R.menu.cab, menu);
					}
					else {
						menu.removeItem(R.id.menu_uninstall);
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
	public List<ApplicationInfo> getApplicationInfoList(int type) {
		List<ApplicationInfo> launchables;
		PackageManager pm=getPackageManager();
		Intent main=new Intent(Intent.ACTION_MAIN, null);
	    if(type==0) {
	    	main.addCategory(Intent.CATEGORY_LAUNCHER);
	    }
	  launchables=pm.getInstalledApplications(type);
	  int i=0;
	  if(ApplicationInfo.FLAG_SYSTEM==type) {
	    while(i<launchables.size()) {
  		  ApplicationInfo el=launchables.get(i);
  		    if((el.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
          		launchables.remove(el);
   			}
    		if (!((el.flags & ApplicationInfo.FLAG_SYSTEM)==0)) {
    			launchables.remove(el);
  			}
  		  i++;
  		}
      }
	  Collections.sort(launchables,new ApplicationInfo.DisplayNameComparator(pm));
	  return launchables;
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
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		Log.w(getPackageName(),"ROW 270");
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		Log.w(getPackageName(),"SEARCHVIEW (ROW 272)="+searchView);
		Log.w(getPackageName(),"MENU ITEM SEARCH(ROW 273)="+(menu.findItem(R.id.menu_search)).toString());
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
  class AppAdapter extends ArrayAdapter<ApplicationInfo> {
    private PackageManager pm=null;
    List<ApplicationInfo> apps;
    AppAdapter(PackageManager pm, List<ApplicationInfo> apps) {
      super(AppListActivity.this, R.layout.row, apps);
	  this.apps=apps;
      this.pm=pm;
    }
    
    @Override
    public View getView(int position, View convertView,
                          ViewGroup parent) {
		  Log.w(AppListActivity.this.getPackageName(),"getView");
      if (convertView==null) {
        convertView=newView(parent);
      }
      
      bindView(position, convertView);
      
      return(convertView);
    }
    
    private View newView(ViewGroup parent) {
		Log.w(AppListActivity.this.getPackageName(),"newView");
      return(getLayoutInflater().inflate(R.layout.row, parent, false));
    }
	
	
    private void bindView(int position, View row) {
      TextView label=(TextView)row.findViewById(R.id.label);
      Log.w(AppListActivity.this.getPackageName(),"bindView");
      label.setText(getItem(position).loadLabel(pm));
      
      ImageView icon=(ImageView)row.findViewById(R.id.icon);
      
      icon.setImageDrawable(getItem(position).loadIcon(pm));
    }
  }
  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	  list.setItemChecked(position,true);
  }
}
