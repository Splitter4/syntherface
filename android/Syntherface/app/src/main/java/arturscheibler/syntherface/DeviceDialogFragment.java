package arturscheibler.syntherface;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DeviceDialogFragment extends DialogFragment {
    
    interface DeviceDialogListener {
        void onDeviceChosen(BluetoothDevice device);
    }
    
    public final static int REQUEST_ACCESS_COARSE_LOCATION = 1;
    
    private Activity mActivity = null;
    private DeviceDialogListener mListener = null;
    private DeviceAdapter mAdapter = null;
    private final static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mAdapter.add(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    };
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            mActivity = activity;
            
            try {
                mListener = (DeviceDialogListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement DeviceDialogListener");
            }
        }
    }
    
    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<BluetoothDevice> pairedDevices = new ArrayList<>(mBluetoothAdapter.getBondedDevices());
        mAdapter = new DeviceAdapter(pairedDevices);
        
        return new AlertDialog.Builder(mActivity)
                .setTitle(R.string.device_dialog_title)
                .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothAdapter.cancelDiscovery();
                        mListener.onDeviceChosen(mAdapter.getItem(which));
                    }
                })
                .create();
    }
    
    @Override
    public void onResume() {
        super.onResume();

        // A permission to access the device's coarse location is needed to discover unpaired
        // devices. These permissions are only necessary for Android 6.0 and higher.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            discoverUnpairedDevices();
        } else {
            final int permissionStatus = ContextCompat.checkSelfPermission(
                    mActivity.getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                discoverUnpairedDevices();
            } else {
                new AlertDialog.Builder(mActivity)
                        .setTitle("Additional permissions needed")
                        .setMessage(mActivity.getString(R.string.permission_rationale))
                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                Toast.makeText(
                                        mActivity,
                                        getString(R.string.only_paired_devices),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        })
                        .show();
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mActivity.unregisterReceiver(mDiscoveryReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered. No need to unregister it.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {        
        if (requestCode == DeviceDialogFragment.REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                        mActivity,
                        getString(R.string.only_paired_devices),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                discoverUnpairedDevices();
            }
        }
    }
    
    private void discoverUnpairedDevices() {
        // Register for device discovery broadcasts.        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mActivity.registerReceiver(mDiscoveryReceiver, filter);

        mBluetoothAdapter.startDiscovery();
    }
    
    private class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
        
        DeviceAdapter(List<BluetoothDevice> deviceList) {
            super(mActivity, android.R.layout.simple_list_item_1, deviceList);
        }
        
        @Override @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater()
                        .inflate(android.R.layout.simple_list_item_1, null);
            }

            BluetoothDevice device = getItem(position);
            String deviceName;
            if (device != null && device.getName() != null) {
                deviceName = device.getName();
            } else {
                deviceName = getString(R.string.default_device_name);
            }
            
            ((TextView) convertView).setText(deviceName);
            
            return convertView;
        }
    }
}
