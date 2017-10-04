package arturscheibler.syntherface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DeviceDialogListener {
        void onDeviceChosen(BluetoothDevice device);
    }
    
    DeviceDialogListener mListener; // Use this instance of the interface to deliver action events
    ArrayAdapter<BluetoothDevice> mAdapter;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        List<BluetoothDevice> devices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        
        mAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                devices
        );
        
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.device_dialog_title)
                .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDeviceChosen(mAdapter.getItem(which));
                    }
                })
                .create();
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
}
