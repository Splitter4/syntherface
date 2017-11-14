package arturscheibler.syntherface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements
        DeviceDialogFragment.DeviceDialogListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private final static int REQUEST_ENABLE_BT = 1;
    private final static String DIALOG_DEVICE = "device";
    
    private UUIDBroadcastReceiver mUuidBroadcastReceiver = new UUIDBroadcastReceiver();
    
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private boolean mStopThread;
    
    private Button mConnectButton;
    private Button mDisconnectButton;
    private Button mSendButton;
    private Button mClearButton;
    private EditText mInputText;
    private TextView mConsoleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Workspace((RelativeLayout) findViewById(R.id.workspace));

        ArrayList<SynthWidget> synthWidgets = new ArrayList<>();
        synthWidgets.add(new Knob());

        RecyclerView synthWidgetList = (RecyclerView) findViewById(R.id.synth_widget_list);
        synthWidgetList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        synthWidgetList.setAdapter(new SynthWidgetAdapter(synthWidgets));
    
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mConnectButton = (Button)findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "This device doesn't support Bluetooth.", Toast.LENGTH_SHORT).show();
                } else {
                    if (mBluetoothAdapter.isEnabled()) {
                        DeviceDialogFragment.setupPermissions(MainActivity.this);
                        // The result of this is captured in onRequestPermissionsResult()
                    } else {
                        Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableAdapter, REQUEST_ENABLE_BT);
                        // The result of this is captured in onActivityResult()
                    }
                }
            }
        });

        mDisconnectButton = (Button)findViewById(R.id.disconnect_button);
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopThread = true;
                try {
                    mInputStream.close();
                    mOutputStream.close();
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error disconnecting from device.", Toast.LENGTH_SHORT).show();
                } finally {
                    setConnectionStatus(false);
                    mConsoleTextView.append("Connection Closed!\n\n");
                }
            }
        });

        mSendButton = (Button)findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = mInputText.getText().toString().concat("\n");
                try {
                    mOutputStream.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mConsoleTextView.append("Sent Data: " + string);
            }
        });

        mClearButton = (Button)findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputText.setText("");
            }
        });
        
        mInputText = (EditText)findViewById(R.id.input_text);
        
        mConsoleTextView = (TextView)findViewById(R.id.console_text_view);
        mConsoleTextView.setSingleLine(false);
        
        setConnectionStatus(false);
    }

    private void setConnectionStatus(boolean status) {
        mConnectButton.setEnabled(!status);
        mConnectButton.setVisibility(status ? View.GONE : View.VISIBLE);
        
        mDisconnectButton.setEnabled(status);
        mDisconnectButton.setVisibility(status ? View.VISIBLE : View.GONE);
        
        mSendButton.setEnabled(status);
        mClearButton.setEnabled(status);
        
        mInputText.setEnabled(status);
        mConsoleTextView.setEnabled(status);
    }

    private void beginListenForData() {
        final Handler handler = new Handler();
        mStopThread = false;
        
        Thread thread  = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !mStopThread) {
                    try {
                        int byteCount = mInputStream.available();
                        if(byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            mInputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                public void run() {
                                    mConsoleTextView.append(string);
                                }
                            });
                        }
                    }
                    catch (IOException ex) {
                        mStopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
    
    private void connectToDevice(BluetoothDevice device, UUID uuid) {
        //TODO: When connecting, "Connection opened" is shown twice and "Error connecting to device", once.
        //TODO: App crashes when connecting to unavailable device.
        this.unregisterReceiver(mUuidBroadcastReceiver);
        try {
            mSocket = device.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
        
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error connecting to device.", Toast.LENGTH_SHORT).show();
        } finally {
            setConnectionStatus(true);
            mConsoleTextView.append("Connection Opened!\n\n");
            beginListenForData();
        }
    }
    
    public void onDeviceChosen(BluetoothDevice device) {
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        this.registerReceiver(mUuidBroadcastReceiver, intentFilter);
        device.fetchUuidsWithSdp();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == DeviceDialogFragment.REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Only paired devices will be shown.", Toast.LENGTH_LONG).show();
            }
            
            DeviceDialogFragment dialogFragment = new DeviceDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), DIALOG_DEVICE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    
                    case RESULT_OK:
                        DeviceDialogFragment.setupPermissions(this);
                        // The result of this is captured in onRequestPermissionsResult()
                        break;
                    
                    case RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(), "Bluetooth needs to be activated for the app to work!", Toast.LENGTH_SHORT).show();
                        break;
                    
                    default:
                        Toast.makeText(getApplicationContext(), "Unrecognized result when trying to enable Bluetooth.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }
    
    private class UUIDBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_UUID)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                connectToDevice(device, ((ParcelUuid) uuids[0]).getUuid());
            }
        }
    }
}
