package arturscheibler.syntherface;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DeviceDialogListener {
        void onDeviceChosen(BluetoothDevice device);
    }
    
    public final static int REQUEST_ACCESS_COARSE_LOCATION = 1;
    private DeviceDialogListener mListener; // Use this instance of the interface to deliver action events
    private DeviceAdapter mAdapter;
    
    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        List<BluetoothDevice> pairedDevices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        
        mAdapter = new DeviceAdapter(pairedDevices);
    
        // Register for broadcasts when a device is discovered.
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mAdapter.add(device);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    bluetoothAdapter.cancelDiscovery();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(receiver, filter);
        
        bluetoothAdapter.startDiscovery();
        
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.device_dialog_title)
                .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bluetoothAdapter.cancelDiscovery();
                        mListener.onDeviceChosen(mAdapter.getItem(which));
                    }
                })
                .create();
    }
    
    // On the activity using this fragment, this method must be called before showing the fragment.
    // onRequestPermissionsResult must be overridden in the activity so as to know when to show.
    public static void setupPermissions(final Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Only ask for these permissions on runtime when running Android 6.0 or higher
            final int permissionStatus = ContextCompat.checkSelfPermission(
                    activity.getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                // Request it anyway so that onRequestPermissionsResult will be called in the activity
                requestPermission(activity);
            } else {
                ((TextView) new android.support.v7.app.AlertDialog.Builder(activity)
                        .setTitle("Additional permissions needed")
                        // TODO: Use a non-deprecated method.
                        .setMessage(Html.fromHtml("<p>To find nearby Bluetooth devices please click \"Allow\" on the next permissions dialog.</p>" +
                                "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission(activity);
                            }
                        })
                        .show()
                        .findViewById(android.R.id.message))
                        .setMovementMethod(LinkMovementMethod.getInstance()); // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
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
    
    // Override the Fragment.onAttach() method to instantiate the DeviceDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the DeviceDialogListener so we can send events to the host
                mListener = (DeviceDialogListener) activity;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activity.toString() + " must implement DeviceDialogListener");
            }
        }
    }
    
    private class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
        
        DeviceAdapter(List<BluetoothDevice> deviceList) {
            super(getActivity(), android.R.layout.simple_list_item_1, deviceList);
        }
        
        @Override @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(android.R.layout.simple_list_item_1, null);
            }
            
            ((TextView) convertView).setText(getItem(position).getName());
            
            return convertView;
        }
    }
}
