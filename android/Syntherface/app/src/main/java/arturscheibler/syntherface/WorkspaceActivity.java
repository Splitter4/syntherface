package arturscheibler.syntherface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class WorkspaceActivity extends FragmentActivity implements
        DeviceDialogFragment.DeviceDialogListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private final static String TAG = "WorkspaceActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private final static String DIALOG_DEVICE = "device";
    private static final String DIALOG_SYNTH_WIDGET_SETUP = "synth_widget_setup";
    
    private final UUIDBroadcastReceiver mUuidBroadcastReceiver = new UUIDBroadcastReceiver();
    private final DeviceDialogFragment mDeviceDialogFragment = new DeviceDialogFragment();
    
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mDevice = null;
    private UUID mUuid = null;
    private BluetoothSocket mSocket = null;
    private OutputStream mOutputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ////////// Setup UI //////////
        
        new Workspace((RelativeLayout) findViewById(R.id.workspace), new Workspace.OnDropListener() {
            @Override
            public void onDrop(SynthWidget synthWidget) {
                synthWidget.showSetupDialog(getSupportFragmentManager(), DIALOG_SYNTH_WIDGET_SETUP);
            }
        });

        ArrayList<SynthWidget> synthWidgets = new ArrayList<>();
        synthWidgets.add(new Knob());

        RecyclerView synthWidgetList = findViewById(R.id.synth_widget_list);
        synthWidgetList.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false));
        synthWidgetList.setAdapter(new SynthWidgetAdapter(synthWidgets));
    
        //////////// Setup Bluetooth connection to device //////////
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.bluetooth_not_supported),
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mDeviceDialogFragment.show(getSupportFragmentManager(), DIALOG_DEVICE);
            } else {
                Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableAdapter, REQUEST_ENABLE_BT);
            }
        }

//        mSendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String string = mInputText.getText().toString().concat("\n");
//                try {
//                    mOutputStream.write(string.getBytes());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (mSocket != null && !mSocket.isConnected()) {
            connectToDevice();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        try {
            unregisterReceiver(mUuidBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered. No need to unregister it.
        }
        
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }

            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.bluetooth_disconnection_error),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
    
    private void connectToDevice() {
        try {
            unregisterReceiver(mUuidBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered. No need to unregister it.
        }
        
        mBluetoothAdapter.cancelDiscovery();
        
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(mUuid);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.bluetooth_connection_error),
                    Toast.LENGTH_SHORT)
                    .show();

            mDeviceDialogFragment.show(getSupportFragmentManager(), DIALOG_DEVICE);
        }
        
        new Thread() {
            @Override
            public void run() {
                try {
                    mSocket.connect();
                    mOutputStream = mSocket.getOutputStream();
                } catch (IOException connectException) {
                    connectException.printStackTrace();
                    
                    WorkspaceActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getString(R.string.bluetooth_connection_error),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    
                    try {
                        mSocket.close();
                        if (mOutputStream != null) {
                            mOutputStream.close();
                        }
                    } catch (IOException closeException) {
                        closeException.printStackTrace();
                        Log.e(TAG, "Could not close socket or output stream.");
                    }

                    mDeviceDialogFragment.show(getSupportFragmentManager(), DIALOG_DEVICE);
                }

            }
        }.start();
    }
    
    public void onDeviceChosen(BluetoothDevice device) {
        mDevice = device;
        
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        registerReceiver(mUuidBroadcastReceiver, intentFilter);
        device.fetchUuidsWithSdp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:
                        mDeviceDialogFragment.show(getSupportFragmentManager(), DIALOG_DEVICE);
                        break;
                    
                    case RESULT_CANCELED:
                        Toast.makeText(
                                getApplicationContext(),
                                getString(R.string.bluetooth_must_be_active),
                                Toast.LENGTH_SHORT)
                                .show();
                        break;
                    
                    default:
                        Log.i(TAG, "Unrecognized result when trying to enable Bluetooth.");
                        break;
                }
                break;
        }
    }
    
    private class UUIDBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            
            if (BluetoothDevice.ACTION_UUID.equals(intent.getAction())) {
                Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                mUuid = ((ParcelUuid) uuids[0]).getUuid();
                
                connectToDevice();
            }
        }
    }
}
