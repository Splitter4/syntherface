package arturscheibler.syntherface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DeviceDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DeviceDialogListener {
        void onListItemClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    DeviceDialogListener mListener;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.device_dialog_title)
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
