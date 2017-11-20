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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
    
    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<BluetoothDevice> pairedDevices = new ArrayList<>(mBluetoothAdapter.getBondedDevices());
        
        mAdapter = new DeviceAdapter(pairedDevices);
    
        // Register for device discovery broadcasts.        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mActivity.registerReceiver(mDiscoveryReceiver, filter);
        
        mBluetoothAdapter.startDiscovery();
        
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
    
    // On the activity using this fragment, this method must be called before showing the fragment.
    // onRequestPermissionsResult must be overridden in the activity so as to know when to show.
    public static void setupPermissions(final Activity activity) {
        // Only ask for these permissions on runtime when running Android 6.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int permissionStatus = ContextCompat.checkSelfPermission(
                    activity.getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                // Request it anyway so that onRequestPermissionsResult will be called in the activity
                requestPermission(activity);
            } else {
                String permissionReasoning = activity.getString(R.string.permission_reasoning);
                Spanned dialogMessage;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dialogMessage = Html.fromHtml(permissionReasoning, Html.FROM_HTML_MODE_LEGACY);
                } else {
                    dialogMessage = Html.fromHtml(permissionReasoning);
                }
                
                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setTitle("Additional permissions needed")
                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission(activity);
                            }
                        })
                        .setMessage(dialogMessage)
                        .show();
                
                TextView textView = dialog.findViewById(android.R.id.message);
                if (textView != null) {
                    // Make the link clickable. Needs to be called after show().
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        } else {
            // Request it anyway so that onRequestPermissionsResult will be called in the activity
            requestPermission(activity);
        }
    }
    
    private static void requestPermission(final Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_ACCESS_COARSE_LOCATION);
    }
    
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
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.unregisterReceiver(mDiscoveryReceiver);
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
            
            ((TextView) convertView).setText(getItem(position).getName());
            
            return convertView;
        }
    }
}
