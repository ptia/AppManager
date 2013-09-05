package it.ptia.appmanager;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.preference.*;
import android.widget.*;
import it.ptia.appmanager.R;
import java.io.*;
//su -c  " mount -o remount,rw -t yafs2 /dev/block/mtdblock3 /system ";su -c " rm /system/app/CMWallpapers.apk "
public class Uninstaller
{
    AlertDialog.Builder askDialog;
    private Activity context;
	private PackageManager pm;
	//String cmd="su -c  \"mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\"; su -c  \"rm ";
	//String cmd="su -c \" mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system \";su -c \" rm ";
	String cmd2="su; mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system; rm ";
	String cmdTest= "su -c \"reboot\"";
	String cmd="su -c  \" mount -o remount,rw -t yafs2 /dev/block/mtdblock3 /system \";su -c \" rm "; ///system/app/CMWallpapers.apk \" ;
    public Uninstaller(Activity context, PackageManager pm) {
		this.pm=pm;
		this.context=context;
	}
	public void rootUninstall(ApplicationInfo app)
    {
        Process process;
        try 
        {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("mount -o remount,rw -t rfs /dev/stl5 /system; \n");          
            os.writeBytes("rm -r "+app.publicSourceDir+";\n");
            os.writeBytes("mount -o remount,ro -t rfs /dev/stl5 /system; \n");
			Toast.makeText(context, R.string.uninstall_done,Toast.LENGTH_LONG).show();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }       
    }
	
	public void uninstall(ApplicationInfo app) {
		boolean root=PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_root",false);
		if(root&&app.publicSourceDir.startsWith("/system")) {
			askForRootUninstall(app);
		}
		else {
			String pkgname=app.packageName;
			Intent i=new Intent(Intent.ACTION_DELETE);
			i.setData(Uri.parse("package:"+pkgname));
			context.startActivity(i);
		}
	}
	private void askForRootUninstall(final ApplicationInfo app) {
		askDialog=new AlertDialog.Builder(context);
		askDialog.setIcon(app.loadIcon(pm));
		askDialog.setCancelable(true);
		askDialog.setTitle(app.loadLabel(pm));
		askDialog.setMessage(R.string.uninstall_message);
		askDialog.setPositiveButton(R.string.yes, new Dialog.OnClickListener() {

				public void onClick(DialogInterface p1, int p2)
				{
					rootUninstall(app);
				}
		});
		askDialog.setNegativeButton(R.string.no, new Dialog.OnClickListener() {

				public void onClick(DialogInterface p1, int p2)
				{
					//Do nothing
				}
			});
		askDialog.show();
	}
}
