package ms.gwillia.sockethead;

import java.io.InputStream;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.sockethead.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity demonstrates how to use the constructor:
 * TgStreamReader(InputStream is, TgStreamHandler tgStreamHandler)
 * and related functions:
 * (1) setReadFileBlockSize
 * (2) setReadFileDelay
 * (3) How to destroy a TgStreamReader object
 * (4) ConnectionStates.STATE_COMPLETE is state that indicates read file to the end
 *
 */
public class FileDemoActivity extends  Activity{
	private static final String TAG = FileDemoActivity.class.getSimpleName();
	
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

	private TgStreamReader tgStreamReader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.first_view);

		initView();
		setUpDrawWaveView();
	}

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
				
				// (3) How to destroy a TgStreamReader object
				if(tgStreamReader != null){
					tgStreamReader.stop();
					tgStreamReader.close();
					tgStreamReader = null;
				}
				InputStream is = getApplicationContext().getResources().openRawResource(R.raw.tgam_capture);
				// Example of TgStreamReader(InputStream is, TgStreamHandler tgStreamHandler)
		        tgStreamReader = new TgStreamReader(is, callback);
		        
		        // (1) Example of setReadFileBlockSize(int), the default block size is 8, call it before connectAndStart() or connect()
		        tgStreamReader.setReadFileBlockSize(16);
		        // (2) Example of setReadFileDelay(int), the default delay time is 2ms, call it before connectAndStart() or connect()
		        tgStreamReader.setReadFileDelay(2);
		        
		        tgStreamReader.connectAndStart();
			}
		});

		btn_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				stop();
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		super.onStop();
		stop();
	}

	// TODO view
	DrawWaveView waveView = null;
	
	public void setUpDrawWaveView() {
		// TODO use self view to drawing ECG
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

	private TgStreamHandler callback = new TgStreamHandler() {

		@Override
		public void onStatesChanged(int connectionStates) {
			// TODO Auto-generated method stub
			Log.d(TAG, "connectionStates change to: " + connectionStates);
			switch (connectionStates) {
			case ConnectionStates.STATE_CONNECTED:
				//sensor.start();
				showToast("Connected", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_WORKING:
				
				break;
			case ConnectionStates.STATE_GET_DATA_TIME_OUT:
				//  get data time out
				break;
			case ConnectionStates.STATE_COMPLETE:
				//read file complete
				showToast("STATE_COMPLETE",Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_STOPPED:
				break;
			case ConnectionStates.STATE_DISCONNECTED:
				break;
			case ConnectionStates.STATE_ERROR:
				break;
			}
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_STATE;
			msg.arg1 = connectionStates;
			LinkDetectedHandler.sendMessage(msg);

		}

		@Override
		public void onRecordFail(int a) {
			// TODO Auto-generated method stub
			Log.e(TAG,"onRecordFail: " +a);

		}

		@Override
		public void onChecksumFail(byte[] payload, int length, int checksum) {
			// TODO Auto-generated method stub
			
			badPacketCount ++;
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_BAD_PACKET;
			msg.arg1 = badPacketCount;
			LinkDetectedHandler.sendMessage(msg);

		}

		@Override
		public void onDataReceived(int datatype, int data, Object obj) {
			// TODO Auto-generated method stub
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = datatype;
			msg.arg1 = data;
			msg.obj = obj;
			LinkDetectedHandler.sendMessage(msg);
			//Log.i(TAG,"onDataReceived");
		}

	};

	private static final int MSG_UPDATE_BAD_PACKET = 1001;
	private static final int MSG_UPDATE_STATE = 1002;

	int raw;
	private Handler LinkDetectedHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MindDataType.CODE_RAW:
					updateWaveView(msg.arg1);
					//You can put the raw data into Algo SDKs here
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
				if(power.isValidate()){
					tv_delta.setText("" +power.delta);
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
		FileDemoActivity.this.runOnUiThread(new Runnable()    
        {    
            public void run()    
            {    
            	Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }    
    
        });  
	}
}
