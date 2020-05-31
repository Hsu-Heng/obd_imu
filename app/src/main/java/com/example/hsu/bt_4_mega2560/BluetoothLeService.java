package com.example.hsu.bt_4_mega2560;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by hsu on 2017/2/10.
 */
public class BluetoothLeService extends Service {
    private static final String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    public static final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public static final UUID UUID_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private String vla;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(BluetoothLeService.TAG, "oldStatus=" + status + " NewStates=" + newState);
            if(status == 0) {
                String intentAction;
                if(newState == 2) {
                    intentAction = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
                    BluetoothLeService.this.broadcastUpdate(intentAction);
                    Log.i(BluetoothLeService.TAG, "Connected to GATT server.");
                    Log.i(BluetoothLeService.TAG, "Attempting to start service discovery:" + BluetoothLeService.this.mBluetoothGatt.discoverServices());
                } else if(newState == 0) {
                    intentAction = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
                    BluetoothLeService.this.mBluetoothGatt.close();
                    BluetoothLeService.this.mBluetoothGatt = null;
                    Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
                    BluetoothLeService.this.broadcastUpdate(intentAction);
                }
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == 0) {
                Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + status);
                BluetoothLeService.this.findService(gatt.getServices());
            } else if(BluetoothLeService.this.mBluetoothGatt.getDevice().getUuids() == null) {
                Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + status);
            }

        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", characteristic);
            }


        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", characteristic);
            Log.e(BluetoothLeService.TAG, "OnCharacteristicWrite");
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(BluetoothLeService.TAG, "OnCharacteristicWrite");

        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
            Log.e(BluetoothLeService.TAG, "onDescriptorRead");
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
            Log.e(BluetoothLeService.TAG, "onDescriptorWrite");
        }
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b) {
            Log.e(BluetoothLeService.TAG, "onReadRemoteRssi");
        }
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int a) {
            Log.e(BluetoothLeService.TAG, "onReliableWriteCompleted");
        }
    };
    private final IBinder mBinder = new BluetoothLeService.LocalBinder();

    public BluetoothLeService() {
    }

    public void WriteValue(String strValue) {
        this.mNotifyCharacteristic.setValue(strValue.getBytes());
        this.mBluetoothGatt.writeCharacteristic(this.mNotifyCharacteristic);
    }

    public void findService(List<BluetoothGattService> gattServices) {
        Log.i(TAG, "Count is:" + gattServices.size());
        Iterator var3 = gattServices.iterator();

        while(true) {
            BluetoothGattService gattService;
            do {
                if(!var3.hasNext()) {
                    return;
                }

                gattService = (BluetoothGattService)var3.next();
                Log.i(TAG, gattService.getUuid().toString());
                Log.i(TAG, UUID_SERVICE.toString());
            } while(!gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString()));

            List gattCharacteristics = gattService.getCharacteristics();
            Log.i(TAG, "Count is:" + gattCharacteristics.size());
            Iterator var6 = gattCharacteristics.iterator();

            while(var6.hasNext()) {
                BluetoothGattCharacteristic gattCharacteristic = (BluetoothGattCharacteristic)var6.next();
                if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString())) {
                    Log.i(TAG, gattCharacteristic.getUuid().toString());
                    Log.i(TAG, UUID_NOTIFY.toString());
                    this.mNotifyCharacteristic = gattCharacteristic;
                    this.setCharacteristicNotification(gattCharacteristic, true);
                    this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
                    return;
                }
            }
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        Log.w("action",characteristic.toString());
        System.out.println("BluetoothLeService broadcastUpdate");
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, new String(data));
            Log.d("TEST",new String(data));

            sendBroadcast(intent);
        }
//        }
    }
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        this.close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        if(this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
            if(this.mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if(this.mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        } else {
            return true;
        }
    }

    public boolean connect(String address) {
        if(this.mBluetoothAdapter != null && address != null) {
            BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
            if(device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            } else {
                if(this.mBluetoothGatt != null) {
                    this.mBluetoothGatt.close();
                    this.mBluetoothGatt = null;
                }

                this.mBluetoothGatt = device.connectGatt(this, false, this.mGattCallback);
                Log.d(TAG, "Trying to create a new connection.");
                return true;
            }
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
    }

    public void disconnect() {
        if(this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
    }

    public void close() {
        if(this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.readCharacteristic(characteristic);
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if(this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        return this.mBluetoothGatt == null?null:this.mBluetoothGatt.getServices();
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
