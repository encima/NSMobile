package ms.gwillia.sockethead;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import com.neurosky.connection.TgStreamReader;
import com.rvalerio.fgchecker.AppChecker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import net.rehacktive.waspdb.WaspDb;
import net.rehacktive.waspdb.WaspFactory;
import net.rehacktive.waspdb.WaspHash;

import ms.gwillia.sockethead.brain.Reading;

/**
 * This activity demonstrates how to use the constructor:
 * public TgStreamReader(BluetoothDevice mBluetoothDevice,TgStreamHandler tgStreamHandler)
 * and related functions:
 * (1) changeBluetoothDevice
 * (2) Demo of drawing ECG
 * (3) Demo of getting Bluetooth device dynamically
 * (4) setTgStreamHandler
 */
public class BluetoothDeviceDemoActivity extends Activity {
	private static final String TAG = BluetoothDeviceDemoActivity.class.getSimpleName();
	private TgStreamReader tgStreamReader;
	
	// TODO connection sdk
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private String address = null;
	WaspHash logs;

	Intent serviceIntent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.bluetoothdevice_view);

		requestUsageStatsPermission();


		WaspDb db = WaspFactory.openOrCreateDatabase(getFilesDir().getPath(), "log", "pwd");

// now create an WaspHash, it's like a sql table
		logs = db.openOrCreateHash("log");
		List<Reading> allReads = logs.getAllValues();
//		Log.i(TAG, "Readings: " + allReads.size());

		AppChecker appChecker = new AppChecker();
		String packageName = appChecker.getForegroundApp(this);
//		Log.i(TAG, packageName);

		initView();
//		setUpDrawWaveView();

		if(isMyServiceRunning(BGReadService.class)) {
			btn_selectdevice.setText("Stop Service");
		}



		try {
			// TODO	
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				Toast.makeText(
						this,
						"Please enable your Bluetooth and re-run this program !",
						Toast.LENGTH_LONG).show();
				finish();
//				return;
			}  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "error:" + e.getMessage());
			return;
		}
	}

	void requestUsageStatsPermission() {
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
				&& !hasUsageStatsPermission(this)) {
			startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	boolean hasUsageStatsPermission(Context context) {
		AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
		int mode = appOps.checkOpNoThrow("android:get_usage_stats",
				android.os.Process.myUid(), context.getPackageName());
		boolean granted = mode == AppOpsManager.MODE_ALLOWED;
		return granted;
	}

	private TextView tv_ps = null;
	private TextView tv_attention = null;
	private TextView tv_meditation = null;
	private TextView tv_delta = null;
	private TextView tv_theta = null;
	private TextView tv_lowalpha = null;
	
	private TextView  tv_highalpha = null;
	private TextView  tv_lowbeta = null;
	private TextView  tv_highbeta = null;
	
	private TextView  tv_lowgamma = null;
	private TextView  tv_middlegamma  = null;
	private TextView  tv_badpacket = null;
	
	private Button btn_start = null;
	private Button btn_stop = null;
	private Button btn_selectdevice = null;
//	private LinearLayout wave_layout;
	
	private int badPacketCount = 0;

	private void initView() {
		tv_ps = (TextView) findViewById(R.id.tv_ps);
		tv_attention = (TextView) findViewById(R.id.tv_attention);
		tv_meditation = (TextView) findViewById(R.id.tv_meditation);
		tv_delta = (TextView) findViewById(R.id.tv_delta);
		tv_theta = (TextView) findViewById(R.id.tv_theta);
		tv_lowalpha = (TextView) findViewById(R.id.tv_lowalpha);
		
		tv_highalpha = (TextView) findViewById(R.id.tv_highalpha);
		tv_lowbeta= (TextView) findViewById(R.id.tv_lowbeta);
		tv_highbeta= (TextView) findViewById(R.id.tv_highbeta);
		
		tv_lowgamma = (TextView) findViewById(R.id.tv_lowgamma);
		tv_middlegamma= (TextView) findViewById(R.id.tv_middlegamma);
		tv_badpacket = (TextView) findViewById(R.id.tv_badpacket);
		
		
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_stop = (Button) findViewById(R.id.btn_stop);
//		wave_layout = (LinearLayout) findViewById(R.id.wave_layout);
		
		btn_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				badPacketCount = 0;
				showToast("connecting ...",Toast.LENGTH_SHORT);
				start();
			}
		});

		btn_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(tgStreamReader != null){
					tgStreamReader.stop();
				}
			}

		});
		
		btn_selectdevice =  (Button) findViewById(R.id.btn_selectdevice);
		
		btn_selectdevice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(!isMyServiceRunning(BGReadService.class)) {
					scanDevice();
				}else{
					btn_selectdevice.setText("Select Device");
					stopService(new Intent(getApplicationContext(), BGReadService.class));

				}
			}

		});
	}
	
	private void start(){
		if(address != null){
			BluetoothDevice bd = mBluetoothAdapter.getRemoteDevice(address);
			createStreamReader(bd);

			tgStreamReader.connectAndStart();
		}else{
			showToast("Please select device first!", Toast.LENGTH_SHORT);
		}
	}

	public void stop() {
		if(tgStreamReader != null){
			tgStreamReader.stop();
			tgStreamReader.close();//if there is not stop cmd, please call close() or the data will accumulate 
			tgStreamReader = null;
		}
	}

	@Override
	protected void onDestroy() {
		if(tgStreamReader != null){
			tgStreamReader.close();
			tgStreamReader = null;
		}
		super.onDestroy();
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stop();
	}
	
	
	public void showToast(final String msg,final int timeStyle){
		BluetoothDeviceDemoActivity.this.runOnUiThread(new Runnable()    
        {    
            public void run()    
            {    
            	Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }    
    
        });  
	}
	
	//show device list while scanning
	private ListView list_select;
	private BTDeviceListAdapter deviceListApapter = null;
	private Dialog selectDialog;
	
	// (3) Demo of getting Bluetooth device dynamically
    public void scanDevice(){

    	if(mBluetoothAdapter.isDiscovering()){
    		mBluetoothAdapter.cancelDiscovery();
    	}
    	
    	setUpDeviceListView();
    	//register the receiver for scanning
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
    	
    	mBluetoothAdapter.startDiscovery();
    }
    
 private void setUpDeviceListView(){
    	
    	LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.dialog_select_device, null);
		list_select = (ListView) view.findViewById(R.id.list_select);
		selectDialog = new Dialog(this, R.style.dialog1);
		selectDialog.setContentView(view);
    	//List device dialog

    	deviceListApapter = new BTDeviceListAdapter(this);
    	list_select.setAdapter(deviceListApapter);
    	list_select.setOnItemClickListener(selectDeviceItemClickListener);
    	
    	selectDialog.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface arg0) {
				Log.e(TAG,"onCancel called!");
				BluetoothDeviceDemoActivity.this.unregisterReceiver(mReceiver);
			}
    		
    	});
    	
    	selectDialog.show();
    	
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	for(BluetoothDevice device: pairedDevices){
    		deviceListApapter.addDevice(device);
    	}
		deviceListApapter.notifyDataSetChanged();
    }
 
 //Select device operation
 private OnItemClickListener selectDeviceItemClickListener = new OnItemClickListener(){
	 
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
			Log.d(TAG, "Rico ####  list_select onItemClick     ");
	    	if(mBluetoothAdapter.isDiscovering()){
	    		mBluetoothAdapter.cancelDiscovery();
	    	}
	    	//unregister receiver
	    	BluetoothDeviceDemoActivity.this.unregisterReceiver(mReceiver);

	    	mBluetoothDevice =deviceListApapter.getDevice(arg2);
	    	selectDialog.dismiss();
	    	selectDialog = null;
	    	
			Log.d(TAG,"onItemClick name: "+mBluetoothDevice.getName() + " , address: " + mBluetoothDevice.getAddress() );
			address = mBluetoothDevice.getAddress().toString();
			
			//get remote device
			BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothDevice.getAddress().toString());
         
			//bind and connect
			//bindToDevice(remoteDevice); // create bond works unstable on Samsung S5
			//showToast("pairing ...",Toast.LENGTH_SHORT);

			tgStreamReader = createStreamReader(remoteDevice); 
//			tgStreamReader.connectAndStart();
		
		}
	
 };
 
 /**
	 * If the TgStreamReader is created, just change the bluetooth
	 * else create TgStreamReader, set data receiver, TgStreamHandler and parser
	 * @param bd
	 * @return TgStreamReader
	 */
	public TgStreamReader createStreamReader(BluetoothDevice bd){
		BGReadService bg = new BGReadService();
		Intent i = new Intent(this,  BGReadService.class);
		i.putExtra("BD", bd);
		startService(i);
		btn_selectdevice.setText("Stop Service");

		Log.i(TAG, "Started service");
		return tgStreamReader;
	}
 
 /**
  * Check whether the given device is bonded, if not, bond it 
  * @param bd
  */
 public void bindToDevice(BluetoothDevice bd){
 	    int ispaired = 0;
		if(bd.getBondState() != BluetoothDevice.BOND_BONDED){
			//ispaired = remoteDevice.createBond();
			try {
				//Set pin
				if(Utils.autoBond(bd.getClass(), bd, "0000")){
					ispaired += 1;
				}
				//bind to device
				if(Utils.createBond(bd.getClass(), bd)){
					ispaired += 2;
				}
				Method createCancelMethod=BluetoothDevice.class.getMethod("cancelBondProcess");
                boolean bool=(Boolean)createCancelMethod.invoke(bd);
                Log.d(TAG,"bool="+bool);
					
			} catch (Exception e) {
				Log.d(TAG, " paire device Exception:    " + e.toString());	
			}
		}
		Log.d(TAG, " ispaired:    " + ispaired);	

 }
 
//The BroadcastReceiver that listens for discovered devices 
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
				Log.d(TAG, "mReceiver()");
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.d(TAG,"mReceiver found device: " + device.getName());
				
				// update to UI
				deviceListApapter.addDevice(device);
				deviceListApapter.notifyDataSetChanged();

			} 
		}
	};
    

}
