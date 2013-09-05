package it.ptia.appmanager;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.util.*;

import it.ptia.appmanager.R;
import java.io.*;
import java.nio.channels.*;
import java.util.*;
import android.widget.Toast;

public class BackupService extends IntentService
{
    ArrayList<String> apks;
	Notification notification;
	NotificationManager mNotificationManager;
	Notification.Builder builder;
    public BackupService() {
		super("BackupService");
	}
	private String getFileName(String fileName) {
		Log.w(getPackageName(),"Asking for filename");
		int idx = fileName.lastIndexOf("/");
		return idx >= 0 ? fileName.substring(idx + 1) : fileName;
	}
	protected void onHandleIntent(Intent intent)
	{
		Log.w(getPackageName(),"Backup Service Started");
		apks=intent.getStringArrayListExtra("backupFiles");
		startNotification(apks.size());
		Log.w(getPackageName(),"Backup files recived from Service: "+apks);
		int i=0;
		Intent notificationIntent = new Intent(this, AppListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		for(String app:apks) {
			try {
				builder.setContentTitle("Backup");
				builder.setContentText(getString(R.string.backup_of)+(i+1)+getString(R.string.of)+apks.size()+" app(s)");
				builder.setSmallIcon(android.R.drawable.stat_sys_download);
				builder.setProgress(apks.size(),i+1,false);
				//notification.setLatestEventInfo(this, "Backup",getString(R.string.backup_of)+(i+1)+getString(R.string.of)+apks.size()+" app(s)", pendingIntent);
				mNotificationManager.notify(6317, builder.getNotification());
				String[] appSplit=app.split("&&!!");
				String apk=appSplit[1];
				String appName=appSplit[0];
				File backupDir=new File(Environment.getExternalStorageDirectory(),PreferenceManager.getDefaultSharedPreferences(this).getString("pref_bckDir","AppBackups"));
				if(!(backupDir.exists())) {
					backupDir.mkdir();
					Log.w(getPackageName(),"Backup directory is: "+backupDir.getAbsolutePath());
				}
				File source=new File(apk);
				File dest=new File(backupDir,appName+".apk");
				copyFile(source, dest);
				Log.w(getPackageName(),"Copy of 1 file done");
				i++;
			}
			catch(Exception e) {
				Log.w(getPackageName(),"An error occurred:\n"+e);
			}
			finally {
			builder.setProgress(0,0,false);
			//notification.setLatestEventInfo(this, "Backup",getString(R.string.backup_of)+(i+1)+getString(R.string.of)+apks.size()+" app(s)", pendingIntent);
			mNotificationManager.notify(6317, builder.getNotification());
			}
		}
	}
	public void copyFile(File sourceFile, File destFile) throws IOException {
		Log.w(getPackageName(),"Backup Service is copying file: "+sourceFile.getAbsolutePath()+"to: "+destFile.getAbsolutePath());
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
	public void onDestroy() {
		Log.w(getPackageName(),"Service destroying");
		Toast.makeText(this, R.string.backup_finished, Toast.LENGTH_SHORT).show();
	}
	private void startNotification(int backupNumber) {
		builder=new Notification.Builder(this);
		builder.setContentTitle("Backup");
		builder.setContentText(getString(R.string.backup_of)+backupNumber+"app(s)");
		builder.setSmallIcon(android.R.drawable.stat_sys_download);
		Intent notificationIntent = new Intent(this, AppListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		builder.setContentIntent(pendingIntent);
		mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		/*notification = new Notification(android.R.drawable.stat_sys_download, getString(R.string.backup_of)+backupNumber+"app(s)",System.currentTimeMillis());
		notification.setLatestEventInfo(this, "Backup",getString(R.string.backup_of)+1+" app"+getString(R.string.of)+backupNumber, pendingIntent);*/
		startForeground(6317, builder.getNotification());
	}
}





