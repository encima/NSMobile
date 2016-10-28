package ms.gwillia.sockethead;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
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

import ms.gwillia.sockethead.brain.Reading;
import ms.gwillia.sockethead.brain.Wave;

public class BGReadService extends Service {
    private int badPacketCount = 0;
    private TgStreamReader tgStreamReader;
    WaspHash logs;
    private String TAG = "BGRead";
    Context ctx;
    int mId = 1419;
    Database cdb;

    public BGReadService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.w(TAG, "onBind callback called");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand callback called");
        this.ctx = this;
        Bundle extras = intent.getExtras();
        BluetoothDevice bd = (BluetoothDevice) extras.get("BD");
        tgStreamReader = new TgStreamReader(bd, callback);
        tgStreamReader.startLog();
        tgStreamReader.connectAndStart();
        createNotif();
        return START_STICKY; //keep it running!
//        super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        WaspDb db = WaspFactory.openOrCreateDatabase(getFilesDir().getPath(), "log", "pwd");
//        logs = db.openOrCreateHash("logs");
//        List<Reading> allReads = logs.getAllValues();
        cdb = setUpCouchbase("logs");
        Log.w(TAG, "onCreate callback called");
    }

    @Override
    public void onDestroy() {
        tgStreamReader.close();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mId);
        Log.w(TAG, "onDestroy callback called");
        super.onDestroy();
    }

    private Database setUpCouchbase(String dbName) {
        Manager manager = null;
        Database database = null;
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(dbName);


            return database;
        } catch (Exception e) {
            Log.e(TAG, "Error getting database", e);
            return null;
        }

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
//                    logs.put(time, r);
                    Document d = cdb.createDocument();
                    try {
                        d.putProperties(r.getMap());
                        Log.d(TAG, r.toString());
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }


                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void createNotif() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Mindwave Service")
        .setOngoing(true).setContentText("Currently Logging").setContentIntent(resultPendingIntent);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mBuilder.build());

    }

}
