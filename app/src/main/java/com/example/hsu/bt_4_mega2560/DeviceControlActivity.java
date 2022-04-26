package com.example.hsu.bt_4_mega2560;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.progresviews.ProgressLine;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hsu on 2017/2/10.
 */
public class DeviceControlActivity extends Activity implements LocationListener, GpsStatus.Listener {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private StringBuffer bluetoothintentdata = new StringBuffer();
    private StringBuffer bufferdata;
    private boolean mConnected = false;
    private static final int REQUEST_LOCATION = 1;
    private int copycount = 0;
    boolean retuens;
    private ProgressLine progress_rpm = null;
    private ProgressLine progress_speed = null;
    private ProgressLine progress_throttle = null;
    private ProgressLine progress_airflow = null;
    private ProgressLine progress_barometric = null;
    private SurfaceView surfaceView;
    private SurfaceHolder.Callback callback;   //
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private ImageView imu_view;
    //    EditText edtSend;
    ScrollView svResult;
    //    Button btnSend;
    TextView data_geo;
    EditText edt_filename;
    Button btn_write_data;
    float last_degree, nextdegree;
    private LocationManager mLocationManager;
    private static Data data;
    int gps_speed;
    int gps_percantage;
    int max_speed = 240;
    private boolean getGPSService = false;
    // obd_update
    private Handler mUI_Handler = new Handler();
    // IMU
    private SensorEventListener onRecieveAccListener;
    private SensorEventListener onRecieveGyroListener;
    private SensorEventListener onRecieveOrientationListener;
    private SensorEventListener onRecieveMagnetometrListener;
    private SensorManager mSensorManager;
    private Sensor mAcceleration;
    private Sensor mGyroscope;
    private Sensor mMagnetometr;
    private Sensor mOrientation;
    private String lat, lon;
    private long receive_time;
    private obd_ring_buffer my_ring_buffer = new obd_ring_buffer();
    private ble_receiver_data receiver_data;
    private Thread thread;
    private MadgwickAHRS mMadgwickAHRS = new MadgwickAHRS(0.01f, 0.00001f);
    private int counter = 0;
    private String myreceive = "";
    private export_csv mycsv = new export_csv("defaultname");
    private String bestGPSProvider = LocationManager.GPS_PROVIDER;

    private int bike_speed, bike_rpm, bike_af, bike_bio, bike_tps;
    private float ax, ay, az, gx, gy, gz, mx, my, mz;
    private float xy_angle, xz_angle, zy_angle;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.e(TAG, "mBluetoothLeService is okay");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //���ӳɹ�
                Log.e(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //�Ͽ�����
                mConnected = false;
                invalidateOptionsMenu();
//                btnSend.setEnabled(false);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //���Կ�ʼ�ɻ���
            {
                mConnected = true;
                mDataField.setText("");
                ShowDialog();
//                btnSend.setEnabled(true);
                Log.e(TAG, "In what we need");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (data != null) {
                    long time = System.currentTimeMillis();
                    ble_receiver_data newdata = new ble_receiver_data(time, data);
                    if (my_ring_buffer.full() == false) {
                        my_ring_buffer.put(newdata);
                    }


                    mDataField.setText(data);
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {                                        //��ʼ��
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mDataField = (TextView) findViewById(R.id.data_value);
//        edtSend = (EditText) this.findViewById(R.id.edtSend);
//        edtSend.setText("Welcome ");
        svResult = (ScrollView) this.findViewById(R.id.svResult);
        data_geo = (TextView) this.findViewById(R.id.data_geo);
//        btnSend = (Button) this.findViewById(R.id.btnSend);
//        btnSend.setOnClickListener(new ClickEvent());
//        btnSend.setEnabled(false);
        edt_filename = (EditText) this.findViewById(R.id.edt_filename);
        btn_write_data = (Button) this.findViewById(R.id.btn_write_state);
        btn_write_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mycsv.setFilename(edt_filename.getText().toString() + ".csv");
                mycsv.setState(!mycsv.isState());
                if (mycsv.isState()) {
                    btn_write_data.setText("寫入中");
                    startVoide(true);
                } else {
                    btn_write_data.setText("未寫入");
                    startVoide(false);
                }
            }
        });


        progress_rpm = (ProgressLine) this.findViewById(R.id.progress_rpm);
        progress_rpm.setmPercentage(0);
        progress_speed = (ProgressLine) this.findViewById(R.id.progress_speed);
        progress_speed.setmPercentage(0);
        progress_throttle = (ProgressLine) this.findViewById(R.id.progress_throttle);
        progress_throttle.setmPercentage(0);
        progress_airflow = (ProgressLine) this.findViewById(R.id.progress_airflow);
        progress_airflow.setmPercentage(0);
        progress_barometric = (ProgressLine) this.findViewById(R.id.progress_barometric);
        progress_barometric.setmPercentage(0);
        imu_view = (ImageView) this.findViewById(R.id.iv_bike);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometr = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        onRecieveAccListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    ax = event.values[0];
                    ay = event.values[1];
                    az = event.values[2];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        onRecieveGyroListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    gx = event.values[0];
                    gy = event.values[1];
                    gz = event.values[2];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        onRecieveOrientationListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    xy_angle = event.values[0]; //Плоскость XY
                    xz_angle = event.values[1]; //Плоскость XZ
                    zy_angle = event.values[2]; //Плоскость ZY
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        onRecieveMagnetometrListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mx = event.values[0]; //Плоскость XY
                    my = event.values[1]; //Плоскость XZ
                    mz = event.values[2]; //Плоскость ZY
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };


        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationServiceInitial();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            getGPSService = true; //確認開啟定位服務
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //開啟設定頁面
        }


        Timer mTimer;
        GuiTimer guiTimer;
        mTimer = new Timer();
        guiTimer = new GuiTimer();
        mTimer.schedule(guiTimer, 1000, 10); // 100Hz

        Timer ReceiveTimer;
        ReceiverTask receiverTask;
        ReceiveTimer = new Timer();
        receiverTask = new ReceiverTask();
        ReceiveTimer.schedule(receiverTask, 1000, 10); // 100Hz
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        callback = new MyCallback();
        surfaceView.getHolder().addCallback(callback);
    }

    private void locationServiceInitial() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //取得系統定位服務

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DeviceControlActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location1 = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location != null){
                getLocation(location);
            }
            else if(location1 != null){
                getLocation(location1);
            }
        }


    }
    private void getLocation(Location location) { //將定位資訊顯示在畫面中
        if(location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
            bike_speed = (int)(location.getSpeed());
            gps_percantage = (int) (bike_speed / max_speed);
            progress_speed.setmPercentage(gps_percantage);
            progress_speed.setmValueText(String.valueOf(bike_speed));
            data_geo.setText(lat +"\r"+lon);
        }
        else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(getBaseContext(), GpsService.class));
        mSensorManager.registerListener(onRecieveAccListener,
                mAcceleration,
                SensorManager.SENSOR_DELAY_FASTEST); // 200Hz, 5 ms delay
        mSensorManager.registerListener(onRecieveGyroListener,
                mGyroscope,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(onRecieveOrientationListener,
                mOrientation,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(onRecieveMagnetometrListener,
                mMagnetometr,
                SensorManager.SENSOR_DELAY_GAME);

//        if (data == null) {
//            data = new Data(onGpsServiceUpdate);
//        } else {
//            data.setOnGpsServiceUpdate(onGpsServiceUpdate);
//        }

//        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, (LocationListener) this);
//        } else {
//            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(DeviceControlActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

            return;
        }
        if(getGPSService) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, (LocationListener) this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, (LocationListener) this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        stopService(new Intent(getBaseContext(), GpsService.class));
        mSensorManager.unregisterListener(onRecieveAccListener);
        mSensorManager.unregisterListener(onRecieveGyroListener);
        mSensorManager.unregisterListener(onRecieveOrientationListener);
        if(getGPSService) {
            mLocationManager.removeUpdates(this);   //離開頁面時停止更新
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothLeService != null)
        {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }
        stopService(new Intent(getBaseContext(), GpsService.class));
        Log.d(TAG, "We are in destroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                              //�����ť
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);

                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();

                return true;
            case android.R.id.home:
                if(mConnected)
                {
                    mBluetoothLeService.disconnect();
                    mConnected = false;
                }
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ShowDialog()
    {
        Toast.makeText(this, "cool", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGpsStatusChanged(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        getLocation(location);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "請開啟gps或3G網路", Toast.LENGTH_LONG).show();

    }



    private static IntentFilter makeGattUpdateIntentFilter() {                        //ע����յ��¼�
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    class GuiTimer extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mMadgwickAHRS.update(gx, gy, gz, ax, ay, az, mx, my, mz);

                    counter++;
                    float[] eulerAngles = mMadgwickAHRS.getEulerAngles();
                    if ( counter > 20 ) {
                        float[] quaternion = mMadgwickAHRS.getQuaternion();
//                        float[] eulerAngles = mMadgwickAHRS.getEulerAngles();
                        nextdegree = eulerAngles[2];
                        RotateAnimation ra = new RotateAnimation(
                                last_degree,
                                nextdegree,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);
                        ra.setDuration(1);

                        ra.setFillAfter(true);

                        imu_view.startAnimation(ra);
                        last_degree = nextdegree;


                        counter = 0;
                    }
                    if(mycsv.isState()){
                        long time = System.currentTimeMillis();
                        String writecsv = String.format ("%.2f", ax)+","+String.format ("%.2f", ay)+","+String.format ("%.2f", az)+",";
                        writecsv += String.format ("%.2f", gx)+","+String.format ("%.2f", gy)+","+String.format ("%.2f", gz)+",";
                        writecsv += String.format ("%.2f", mx)+","+String.format ("%.2f", my)+","+String.format ("%.2f", mz)+",";
                        writecsv += String.format ("%.2f", eulerAngles[0])+","+String.format ("%.2f", eulerAngles[1])+","+String.format ("%.2f", eulerAngles[2])+",";
                        writecsv += String.format("%d", bike_speed)+","+String.format("%d", bike_rpm)+","+String.format("%d", bike_tps)+","+String.format("%d", bike_af)+","+String.format("%d", bike_bio)+",";
                        writecsv += lat+","+lon+","+String.format("%d", time);
                        try {
                            mycsv.write_csv(writecsv);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
    public static Data getData() {
        return data;
    }
    class ReceiverTask extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(my_ring_buffer.empty()==false){
                        try{
                        receiver_data = (ble_receiver_data) my_ring_buffer.get();
//                                myreceive = receiver_data.getRece_string();
                        // obd format: AA,pid,value,\n
                        Log.d("parse",receiver_data.getRece_string());
                        String[] splt_r1 = receiver_data.getRece_string().split("\n");
                        String[] splt_r = splt_r1[0].split(",");
                        String pid = splt_r[1];
                        String header = splt_r[0];
                        String s_value = splt_r[2];
                        if(header.contains("AA")) {

                            int value = Integer.valueOf(s_value);
                            float percentange = 0;
                            switch (pid) {
                                case "11":
                                    progress_throttle.setmPercentage(value);
                                    progress_throttle.setmValueText(splt_r[2]);
                                    bike_tps = value;
                                    break;
                                case "0C":
                                    percentange = value / 100;
                                    progress_rpm.setmPercentage((int) percentange);
                                    progress_rpm.setmValueText(splt_r[2]);
                                    bike_rpm = value;
                                    break;
//                                case "0D":
//                                    percentange = value / 2.55f;
//                                    progress_speed.setmPercentage((int) percentange);
//                                    progress_speed.setmValueText(splt_r[2]);
//                                    bike_speed = value;
//                                    break;
                                case "10":
                                    percentange = value / 6.56f;
                                    progress_airflow.setmPercentage((int) percentange);
                                    progress_airflow.setmValueText(splt_r[2]);
                                    bike_af = value;
                                    break;
                                case "33":
                                    percentange = value / 2.55f;
                                    progress_barometric.setmPercentage((int) percentange);
                                    progress_barometric.setmValueText(splt_r[2]);
                                    bike_bio = value;
                                    break;
                                default:
                                    break;

                            }
                        }
                            }catch (Exception e){

                            }


                    }
                }
            });
        }
    }
    private class MyCallback implements SurfaceHolder.Callback   //回调类
    {

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
                                   int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            // TODO Auto-generated method stub
            try
            {
                camera = android.hardware.Camera.open();
                camera.setDisplayOrientation(90);

                camera.setPreviewDisplay(surfaceView.getHolder());
                camera.startPreview();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            // TODO Auto-generated method stub
            if(camera != null)
            {
                camera.stopPreview();   //停止预览
                camera.release();      //释放资源
                camera = null;
            }
        }

    }

    public void startVoide(boolean status)
    {
        if(status){
            try
            {
                String filepath = "/sdcard/"+"bike"
                        +  System.currentTimeMillis() + ".mp4";
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"bike"
                        +  System.currentTimeMillis() + ".mp4");
                camera.unlock();
                mediaRecorder = new MediaRecorder();    //媒体录制对象
                mediaRecorder.setCamera(camera);   //设置摄像
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);  //设置输出的文件的格式
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);   //设置编码
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mediaRecorder.setOutputFile(filepath);   //设置输出文件的路径
                mediaRecorder.setVideoSize(320, 240);  //设置video的大小
                mediaRecorder.setVideoFrameRate(5);
                mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
                mediaRecorder.prepare();   //缓冲
                mediaRecorder.start();   //开始录制

            }catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }else{
            if(mediaRecorder != null)
            {
                mediaRecorder.setOnErrorListener(null);
                mediaRecorder.setOnInfoListener(null);
                mediaRecorder.setPreviewDisplay(null);
                try {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                camera.lock();
                mediaRecorder = null;
            }
        }


        }

}
