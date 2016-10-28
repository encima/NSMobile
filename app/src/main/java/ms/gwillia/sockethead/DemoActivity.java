package ms.gwillia.sockethead;


import com.neurosky.connection.TgStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * This activity is the man entry of this app. It demonstrates the usage of 
 * (1) TgStreamReader.redirectConsoleLogToDocumentFolder()
 * (2) TgStreamReader.stopConsoleLog()
 * (3) demo of getVersion
 */
public class DemoActivity extends Activity {
	private static final String TAG = DemoActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main_view);

		initView();
		// (1) Example of redirectConsoleLogToDocumentFolder()
		// Call redirectConsoleLogToDocumentFolder at the beginning of the app, it will record all the log.
		// Don't forget to call stopConsoleLog() in onDestroy() if it is the end point of this app.
		// If you can't find the end point of the app , you don't have to call stopConsoleLog()
		TgStreamReader.redirectConsoleLogToDocumentFolder();
		// (3) demo of getVersion
		Log.d(TAG,"lib version: " + TgStreamReader.getVersion());
	}

	private TextView tv_filedemo = null;
	private TextView  tv_adapter = null;
	private TextView  tv_device = null;
	//private TextView  tv_uart = null;
	
	private Button btn_filedemo = null;
	private Button btn_adapter = null;
	private Button btn_device = null;
	//private Button btn_uart = null;


	private void initView() {
		tv_filedemo = (TextView) findViewById(R.id.tv_filedemo);
		tv_adapter = (TextView) findViewById(R.id.tv_adapter);
		tv_device = (TextView) findViewById(R.id.tv_device);
		//tv_uart = (TextView) findViewById(R.id.tv_uart);

		btn_filedemo = (Button) findViewById(R.id.btn_filedemo);
		btn_adapter = (Button) findViewById(R.id.btn_adapter);
		btn_device = (Button) findViewById(R.id.btn_device);
		//btn_uart = (Button) findViewById(R.id.btn_uart);
		
		btn_filedemo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(DemoActivity.this,FileDemoActivity.class);
				Log.d(TAG,"Start the FileDemoActivity");
				startActivity(intent);
			}
		});

		
		btn_adapter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(DemoActivity.this,BluetoothAdapterDemoActivity.class);
				Log.d(TAG,"Start the BluetoothAdapterDemoActivity");
				startActivity(intent);
			}
		});
		btn_device.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(DemoActivity.this,BluetoothDeviceDemoActivity.class);
				Log.d(TAG,"Start the BluetoothDeviceActivity");
				startActivity(intent);
			}
		});
//		btn_uart.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				Intent intent = new Intent(DemoActivity.this,UARTDemoActivity.class);
//				startActivity(intent);
//			}
//		});
	}



	@Override
	protected void onDestroy() {
		
		// (2) Example of stopConsoleLog()
		TgStreamReader.stopConsoleLog();
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
	}


}
