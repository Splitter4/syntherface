package arturscheibler.syntherface;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends FragmentActivity implements
        DeviceDialogFragment.DeviceDialogListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private final static int REQUEST_ACCESS_COARSE_LOCATION = 1;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static String DIALOG_DEVICE = "device";
    private final String DEVICE_ADDRESS = "98:D3:36:80:F6:8D"; // TODO: Get address from list of paired devices
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Serial Port Service ID
    
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
    
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mConnectButton = (Button)findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "This device doesn't support Bluetooth.", Toast.LENGTH_SHORT).show();
                } else {
                    if (mBluetoothAdapter.isEnabled()) {
                        getDevice();
                    } else {
                        Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableAdapter, REQUEST_ENABLE_BT);
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
                mConsoleTextView.append("Sent Data:" + string);
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
    
    // TODO: Consider statically moving this to DeviceDialogFragment and creating a new interface method to signal when to show the dialog.
    private void getDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Only ask for these permissions on runtime when running Android 6.0 or higher
            final int permissionStatus = ContextCompat.checkSelfPermission(
                    getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                showDeviceDialog();
            } else {
                ((TextView) new AlertDialog.Builder(this)
                    .setTitle("Additional permissions needed")
                    // TODO: Use a non-deprecated method.
                    .setMessage(Html.fromHtml("<p>To find nearby Bluetooth devices please click \"Allow\" on the next permissions dialog.</p>" +
                            "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                    .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_ACCESS_COARSE_LOCATION);
                        }
                    })
                    .show()
                    .findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance()); // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
            }
        } else {
            showDeviceDialog();
        }
    }
    
    private void showDeviceDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DeviceDialogFragment dialogFragment = new DeviceDialogFragment();
        dialogFragment.show(fragmentManager, DIALOG_DEVICE);
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
                    catch (IOException ex)
                    {
                        mStopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
    
    public void onDeviceChosen(BluetoothDevice device) {
        try {
            mSocket = device.createRfcommSocketToServiceRecord(PORT_UUID);
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
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Only paired devices will be shown.", Toast.LENGTH_LONG).show();
            }
            
            showDeviceDialog();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                getDevice();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth needs to be activated for the app to work!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Unrecognized result when trying to enable Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
