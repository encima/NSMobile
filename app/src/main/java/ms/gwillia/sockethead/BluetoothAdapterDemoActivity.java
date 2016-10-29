package ms.gwillia.sockethead;


import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;
import com.rvalerio.fgchecker.AppChecker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import ms.gwillia.sockethead.brain.Reading;
import ms.gwillia.sockethead.brain.Wave;

/**
 * This activity demonstrates how to use the constructor:
 * public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
 * and related functions:
 * (1) Make sure that the device supports Bluetooth and Bluetooth is on 
 * (2) setGetDataTimeOutTime
 * (3) startLog
 * (4) Using connect() and start() to replace connectAndStart()
 * (5) isBTConnected
 * (6) Use close() to release resource 
 * (7) Demo of TgStreamHandler
 * (8) Demo of MindDataType
 * (9) Demo of recording raw data
 *
 */
public class BluetoothAdapterDemoActivity extends Activity {

	private static final String TAG = BluetoothAdapterDemoActivity.class.getSimpleName();
	private TgStreamReader tgStreamReader;

	private BluetoothAdapter mBluetoothAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.first_view);

		initView();
		setUpDrawWaveView();

		try {
			// (1) Make sure that the device supports Bluetooth and Bluetooth is on 
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
			e.printStackTrace();
			Log.i(TAG, "error:" + e.getMessage());
			return;
		}
		
		// Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
		tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);
		// (2) Demo of setGetDataTimeOutTime, the default time is 5s, please call it before connect() of connectAndStart()
		tgStreamReader.setGetDataTimeOutTime(6);
		// (3) Demo of startLog, you will get more sdk log by logcat if you call this function
		tgStreamReader.startLog();
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
	private LinearLayout wave_layout;
	
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
		wave_layout = (LinearLayout) findViewById(R.id.wave_layout);
		
		btn_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				badPacketCount = 0;
				
				// (5) demo of isBTConnected 
				if(tgStreamReader != null && tgStreamReader.isBTConnected()){
					
					// Prepare for connecting
					tgStreamReader.stop();
					tgStreamReader.close();
				}
				
				// (4) Demo of  using connect() and start() to replace connectAndStart(),
				// please call start() when the state is changed to STATE_CONNECTED
				tgStreamReader.connect();
//				tgStreamReader.connectAndStart();
			}
		});

		btn_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				tgStreamReader.stop();
				tgStreamReader.close();
			}

		});
	}

	public void stop() {
		if(tgStreamReader != null){
			tgStreamReader.stop();
			tgStreamReader.close();
		}
	}

	@Override
	protected void onDestroy() {
		//(6) use close() to release resource 
		if(tgStreamReader != null){
			tgStreamReader.close();
			tgStreamReader = null;
		}
		super.onDestroy();
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

	DrawWaveView waveView = null;

	public void setUpDrawWaveView() {
		waveView = new DrawWaveView(getApplicationContext());
		wave_layout.addView(waveView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		waveView.setValue(2048, 2048, -2048);
	}

	public void updateWaveView(int data) {
		if (waveView != null) {
			waveView.updateData(data);
		}
	}
	
    // (7) demo of TgStreamHandler
	private TgStreamHandler callback = new TgStreamHandler() {

		@Override
		public void onStatesChanged(int connectionStates) {
			// TODO Auto-generated method stub
			Log.d(TAG, "connectionStates change to: " + connectionStates);
			switch (connectionStates) {
			case ConnectionStates.STATE_CONNECTING:
				// Do something when connecting
				break;
			case ConnectionStates.STATE_CONNECTED:
				// Do something when connected
				tgStreamReader.start();
				showToast("Connected", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_WORKING:
				// Do something when working
				
				//(9) demo of recording raw data , stop() will call stopRecordRawData,
				//or you can add a button to control it. 
				//You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
				tgStreamReader.startRecordRawData();
				
				break;
			case ConnectionStates.STATE_GET_DATA_TIME_OUT:
				// Do something when getting data timeout
				
				//(9) demo of recording raw data, exception handling
				tgStreamReader.stopRecordRawData();
				
				showToast("Get data time out!", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_STOPPED:
				// Do something when stopped
				// We have to call tgStreamReader.stop() and tgStreamReader.close() much more than 
				// tgStreamReader.connectAndstart(), because we have to prepare for that.
				
				break;
			case ConnectionStates.STATE_DISCONNECTED:
				// Do something when disconnected
				break;
			case ConnectionStates.STATE_ERROR:
				// Do something when you get error message
				break;
			case ConnectionStates.STATE_FAILED:
				// Do something when you get failed message
				// It always happens when open the BluetoothSocket error or timeout
				// Maybe the device is not working normal.
				// Maybe you have to try again
				break;
			}
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_STATE;
			msg.arg1 = connectionStates;
			LinkDetectedHandler.sendMessage(msg);
		}

		@Override
		public void onRecordFail(int flag) {
			// You can handle the record error message here
			Log.e(TAG,"onRecordFail: " +flag);

		}

		@Override
		public void onChecksumFail(byte[] payload, int length, int checksum) {
			// You can handle the bad packets here.
			badPacketCount ++;
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_BAD_PACKET;
			msg.arg1 = badPacketCount;
			LinkDetectedHandler.sendMessage(msg);

		}

		@Override
		public void onDataReceived(int datatype, int data, Object obj) {
			// You can handle the received data here
			// You can feed the raw data to algo sdk here if necessary.
			
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = datatype;
			msg.arg1 = data;
			msg.obj = obj;
			LinkDetectedHandler.sendMessage(msg);
			
			//Log.i(TAG,"onDataReceived");
		}

	};

	private boolean isPressing = false;
	private static final int MSG_UPDATE_BAD_PACKET = 1001;
	private static final int MSG_UPDATE_STATE = 1002;

	int raw;
	private Handler LinkDetectedHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// (8) demo of MindDataType
			switch (msg.what) {
			case MindDataType.CODE_RAW:
					updateWaveView(msg.arg1);
				break;
			case MindDataType.CODE_MEDITATION:
				Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
				tv_meditation.setText("" +msg.arg1 );
				break;
			case MindDataType.CODE_ATTENTION:
				Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
				tv_attention.setText("" +msg.arg1 );
				break;
			case MindDataType.CODE_EEGPOWER:
				EEGPower power = (EEGPower)msg.obj;
				Log.d(TAG, "updating readings");
				AppChecker appChecker = new AppChecker();
				String packageName = appChecker.getForegroundApp(getApplicationContext());
				long time =  Calendar.getInstance().getTimeInMillis();
				Reading r = new Reading(new Wave(power.delta), new Wave(power.theta),
						new Wave(power.lowAlpha, power.highAlpha), new Wave(power.lowBeta, power.highBeta),
						new Wave(power.lowGamma, power.middleGamma), time,
						"ANDROID", packageName);


				Log.d(TAG, r.toString());
				if(power.isValidate()){
					tv_delta.setText("DEL!" +power.delta);
					tv_theta.setText("" +power.theta);
					tv_lowalpha.setText("" +power.lowAlpha);
					tv_highalpha.setText("" +power.highAlpha);
					tv_lowbeta.setText("" +power.lowBeta);
					tv_highbeta.setText("" +power.highBeta);
					tv_lowgamma.setText("" +power.lowGamma);
					tv_middlegamma.setText("" +power.middleGamma);
				}
				break;
			case MindDataType.CODE_POOR_SIGNAL://
				int poorSignal = msg.arg1;
				Log.d(TAG, "poorSignal:" + poorSignal);
				tv_ps.setText(""+msg.arg1);

				break;
			case MSG_UPDATE_BAD_PACKET:
				tv_badpacket.setText("" + msg.arg1);
				
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
	public void showToast(final String msg,final int timeStyle){
		BluetoothAdapterDemoActivity.this.runOnUiThread(new Runnable()    
        {    
            public void run()    
            {    
            	Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }    
    
        });  
	}
}
