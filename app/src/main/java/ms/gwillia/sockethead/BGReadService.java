package ms.gwillia.sockethead;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.rvalerio.fgchecker.AppChecker;

import net.rehacktive.waspdb.WaspDb;
import net.rehacktive.waspdb.WaspFactory;
import net.rehacktive.waspdb.WaspHash;

import java.util.Calendar;
import java.util.List;

public class BGReadService extends Service {
    private int badPacketCount = 0;
    private TgStreamReader tgStreamReader;
    WaspHash logs;
    private String TAG = "BGRead";
    Context ctx;

    public BGReadService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.w("MyService", "onBind callback called");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("MyService", "onStartCommand callback called");
        this.ctx = this;
        Bundle extras = intent.getExtras();
        BluetoothDevice bd = (BluetoothDevice) extras.get("BD");
        tgStreamReader = new TgStreamReader(bd, callback);
        tgStreamReader.startLog();
        tgStreamReader.connectAndStart();
        return START_STICKY;
//        super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WaspDb db = WaspFactory.openOrCreateDatabase(getFilesDir().getPath(), "log", "pwd");
        logs = db.openOrCreateHash("logs");
        List<Reading> allReads = logs.getAllValues();
//        Log.i(TAG, "Readings: " + allReads.size());
        Log.w("MyService", "onCreate callback called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tgStreamReader.close();
        Log.w("MyService", "onDestroy callback called");
    }


    private int currentState = 0;
    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            currentState  = connectionStates;
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTED:
                    //sensor.start()
                    break;
                case ConnectionStates.STATE_WORKING:
                    LinkDetectedHandler.sendEmptyMessageDelayed(1234, 5000);
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    //get data time out
                    break;
                case ConnectionStates.STATE_COMPLETE:
                    //read file complete
                    break;
                case ConnectionStates.STATE_STOPPED:
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    break;
                case ConnectionStates.STATE_ERROR:
                    Log.d(TAG,"Connect error, Please try again!");
                    break;
                case ConnectionStates.STATE_FAILED:
                    Log.d(TAG,"Connect failed, Please try again!");
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);


        }

        @Override
        public void onRecordFail(int a) {
            Log.e(TAG,"onRecordFail: " +a);
        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            badPacketCount ++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
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
    private static final int MSG_CONNECT = 1003;
    private boolean isReadFilter = false;

    int raw;
    private Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower)msg.obj;
                    AppChecker appChecker = new AppChecker();
                    String packageName = appChecker.getForegroundApp(ctx);
                    long time =  Calendar.getInstance().getTimeInMillis();
                    Reading r = new Reading(new Wave(power.delta), new Wave(power.theta),
                            new Wave(power.lowAlpha, power.highAlpha), new Wave(power.lowBeta, power.highBeta),
                            new Wave(power.lowGamma, power.middleGamma), time,
                            "ANDROID", packageName);
                    logs.put(time, r);


                    Log.d(TAG, r.toString());

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

}
