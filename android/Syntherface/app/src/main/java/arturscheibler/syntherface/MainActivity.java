package arturscheibler.syntherface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_ENABLE_BT = 1;
    public final String DEVICE_ADDRESS = "98:D3:36:80:F6:8D"; // TODO: Get address from list of paired devices
    public final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Serial Port Service ID
    
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private boolean mStopThread;
    private boolean mEnableBtIntentReturned = false;
    
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
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableAdapter, REQUEST_ENABLE_BT);
                    } else {
                        BluetoothDevice device = getDevice(DEVICE_ADDRESS);
                        connectToDevice(device);
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

    private BluetoothDevice getDevice(String deviceAddress) {
        BluetoothDevice device = null;
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please pair with the Bluetooth device.", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice bondedDevice : pairedDevices) {
                // Found device by address
                if (bondedDevice.getAddress().equals(deviceAddress)) {
                    mConsoleTextView.append("Found device:\n");
                    mConsoleTextView.append(bondedDevice.getName() + "\n");
                    mConsoleTextView.append(bondedDevice.getAddress() + "\n\n");
                    
                    device = bondedDevice;
                    break;
                }
            }
        }

        return device;
    }
    
    private void connectToDevice(BluetoothDevice device) {
        if(device != null) {
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
    }

    void beginListenForData() {
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                BluetoothDevice device = getDevice(DEVICE_ADDRESS);
                connectToDevice(device);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth needs to be activated for the app to work!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Unrecognized result when trying to enable Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
