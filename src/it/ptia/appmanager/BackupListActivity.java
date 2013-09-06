package it.ptia.appmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BackupListActivity extends Activity implements ListView.OnItemClickListener
{	
	public ListView backupsListView;
	public ArrayList<File> backups;
    public File backupDir;
	public BackupAdapter adapter;
    public SpinnerAdapter sortAdapter;
	ArrayList<File> cabSelectedItems;
    public ActionBar.OnNavigationListener abOnNavigationListener;
    public int SORT_TYPE=0;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        setContentView(it.ptia.appmanager.R.layout.backup_list);
        sortAdapter=ArrayAdapter.createFromResource(this, R.array.sort_types,android.R.layout.simple_spinner_dropdown_item);
        abOnNavigationListener=new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int position, long id) {
                SORT_TYPE=position;
                backups=getBackupList();
                adapter=new BackupAdapter(backups);
                backupsListView.setAdapter(adapter);
                return true;
            }
        };
        actionBar.setListNavigationCallbacks(sortAdapter, abOnNavigationListener);
        try {
			backupDir=new File(Environment.getExternalStorageDirectory(),PreferenceManager.getDefaultSharedPreferences(this).getString("pref_bckDir","AppBackups"));
			backupsListView=(ListView)findViewById(R.id.backup_list);
			//Toast.makeText(this,getFileNames(getBackups()).get(0),Toast.LENGTH_LONG)
			backups=getBackupList();
        	adapter=new BackupAdapter(backups);
        	backupsListView.setAdapter(adapter);
			backupsListView.setOnItemClickListener(this);
			setLongClick();
		}
		catch (Exception e) {
    		Toast.makeText(this,"No backups",Toast.LENGTH_SHORT).show();
			onDestroy();
		}
    	//setContentView(R.layout.main);
    	adapter=new BackupAdapter(getBackupList());
    	backupsListView.setAdapter(adapter);
		setLongClick();
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//get query
    }
	public void onItemClick(AdapterView<?> p1, View p2, int position, long id) {
		File file=backups.get(position);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
		startActivity(intent);
	}


	public ArrayList<File> getBackupList() {
  		//File folder = new File(Environment.getExternalStorageDirectory(),backupDir.toString());
  		File[] listOfFiles = backupDir.listFiles(); 
		ArrayList<File> listOfBck=new ArrayList<File>();
		for (int i = 0; i < listOfFiles.length; i++) 
		{
			if (!listOfFiles[i].isDirectory()) 
   			{
   				listOfBck.add(listOfFiles[i]);
      		}
  		}
		if(SORT_TYPE==0) {
		    Collections.sort(listOfBck, new FileComparator.FileNameComparator());
        }
        if (SORT_TYPE==1) {
            Collections.sort(listOfBck,new FileComparator.FileDateComparator());
        }
  		return listOfBck;
	}
	public ArrayList<String> getFileNames(List<File> listOfFiles) {
		ArrayList<String> fileNames=new ArrayList<String>();
		for(File f:listOfFiles) {
			fileNames.add(f.getName());
		}
		return fileNames;
	}
	public void setLongClick() {
		backupsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		backupsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,long id, boolean checked) {
			//list.setItemChecked(position+1,true);
    		    File info=backups.get(position);
    		    if(checked) {
    			    cabSelectedItems.add(info);
    			}
    			else {
    				File el;
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
    				mode.setTitle(getString(R.string.selected_one)+ cabSelectedItems.size()+" backup");
    			}
    			else {
    				mode.setTitle(getString(R.string.selected_multi)+ cabSelectedItems.size()+" backups");
    			}
					mode.invalidate();
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// Respond to clicks on the actions in the CAB
				//ArrayList<File> files;
				switch (item.getItemId()) {
				    case R.id.menu_share:
			                shareMultipleApps(cabSelectedItems);
			                mode.finish();
			                return true;
					case R.id.menu_remove_backup:
						for(File apk:cabSelectedItems) {
							apk.delete();
						}
						BackupListActivity.this.recreate();
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
			    cabSelectedItems=new ArrayList<File>();
			    MenuInflater inflater = mode.getMenuInflater();
			    inflater.inflate(R.menu.backup_cab, menu);
			    return true;
			}
		    @Override
		    public void onDestroyActionMode(ActionMode mode) {
				//VOID
			}
		    @Override
		    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		        // Here you can perform updates to the CAB due to
		        // an invalidate() request
				//VOID
				return true;
			}
		});
	}
	public void selectAll() {
		ListView l = backupsListView;
    	int count = l.getCount();
    	for(int i=0; i<count; ++i) 
    	{
			l.setItemChecked(i, true);
		}
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
	public ArrayList<File> getFilteredResults(CharSequence constraint) {
		ArrayList<File> result=new ArrayList<File>();
		for (File e:getBackupList()) {
			Log.w(getPackageName(),"Searching for "+constraint+" in "+e.getName());
			if(e.getName().toLowerCase().contains(constraint)) {
				result.add(e);
				Log.w(getPackageName(),"Approved "+e.getName());
			}
		}
		return result;
	}
	class BackupAdapter extends ArrayAdapter<File> {
	    List<File> apps;
	    BackupAdapter(List<File> apps) {
	      super(BackupListActivity.this, R.layout.backup_list_row, apps);
		  this.apps=apps;
	    }
	    
	    @Override
	    public View getView(int position, View convertView,ViewGroup parent) {
			Log.w(getPackageName(),"getView");
	    	if (convertView==null) {
	    		convertView=newView(parent);
	    	}
	    	bindView(position, convertView);
	      	return(convertView);
	    }
	    
	    private View newView(ViewGroup parent) {
			Log.w(getPackageName(),"newView");
	      return(getLayoutInflater().inflate(R.layout.backup_list_row, parent, false));
	    }
		
		
		private void bindView(int position, View row) {
			TextView label=(TextView)row.findViewById(R.id.backup_name);
 	    	Log.w(getPackageName(),"bindView");
 	    	label.setText(getItem(position).getName());
 		}
	}
}
