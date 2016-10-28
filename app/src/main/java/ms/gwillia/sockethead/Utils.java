package ms.gwillia.sockethead;

import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class Utils {

    public static boolean autoBond(Class btClass,BluetoothDevice device,String strPin) throws Exception {
    	Method autoBondMethod = btClass.getMethod("setPin",new Class[]{byte[].class});
    	Boolean result = (Boolean)autoBondMethod.invoke(device,new Object[]{strPin.getBytes()}); 
    	return result;
    }
    public static boolean createBond(Class btClass,BluetoothDevice device) throws Exception {
    	Method createBondMethod = btClass.getMethod("createBond"); 
    	Boolean returnValue = (Boolean) createBondMethod.invoke(device);
    	return returnValue.booleanValue();
    }
	public static  int getRawWaveValue(byte highOrderByte, byte lowOrderByte) {
		   int hi = ((int)highOrderByte)& 0xFF;
		   int lo = ((int)lowOrderByte) & 0xFF;
		   return( (hi<<8) | lo );
	 }
    
	public static String byte2String( byte[] b) {  
		StringBuffer sb = new StringBuffer();
		   for (int i = 0; i < b.length; i++) { 
		     String hex = Integer.toHexString(b[i] & 0xFF); 
		     if (hex.length() == 1) { 
		       hex = '0' + hex; 
		     } 
		     sb.append(hex);
		   } 
		   return sb.toString().toLowerCase();
		}

	public static void requestUsageStatsPermission(Context ctx) {
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
				&& !hasUsageStatsPermission(ctx)) {
			ctx.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
		}
	}

	public static boolean isMyServiceRunning(Class<?> serviceClass, Context ctx) {
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static boolean hasUsageStatsPermission(Context ctx) {
		AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
		int mode = appOps.checkOpNoThrow("android:get_usage_stats",
				android.os.Process.myUid(), ctx.getPackageName());
		boolean granted = mode == AppOpsManager.MODE_ALLOWED;
		return granted;
	}
}
