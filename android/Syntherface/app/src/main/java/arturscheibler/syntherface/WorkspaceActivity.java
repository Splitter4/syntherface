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
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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

    private int mColumns = 0;
    private int mRows = 0;
    private float mCellSize = 0;
    private List<List<Boolean>> mCellIsOccupied = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ////////// Setup UI //////////
        
        final RelativeLayout workspace = findViewById(R.id.workspace);
        workspace.setOnDragListener(new WorkspaceDragListener());
        workspace.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float gridCellTargetSize = workspace.getContext()
                        .getResources().getDimension(R.dimen.cell_target_size);
                mColumns = Math.round(workspace.getWidth()/gridCellTargetSize);
                mCellSize = workspace.getWidth()/mColumns;
                mRows = (int) Math.ceil((double) workspace.getHeight()/mCellSize);
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

    private void changeCellVacancy(
            boolean occupied, int column, int row, int columnSpan, int rowSpan) {
        while (column + columnSpan > mCellIsOccupied.size()) {
            mCellIsOccupied.add(new ArrayList<Boolean>());
        }

        for (int c = column; c < column + columnSpan; c++) {
            List<Boolean> rows = mCellIsOccupied.get(c);
            while (row + rowSpan > rows.size()) {
                rows.add(false);
            }

            for (int r = row; r < row + rowSpan; r++) {
                rows.set(r, occupied);
            }
        }
    }

    private boolean canPlaceOn(int column, int row, int columnSpan, int rowSpan) {
        while (column + columnSpan > mCellIsOccupied.size()) {
            mCellIsOccupied.add(new ArrayList<Boolean>());
        }

        for (int c = column; c < column + columnSpan; c++) {
            List<Boolean> rows = mCellIsOccupied.get(c);
            while (row + rowSpan > rows.size()) {
                rows.add(false);
            }

            for (int r = row; r < row + rowSpan; r++) {
                if (rows.get(r)) {
                    return false;
                }
            }
        }

        return true;
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

    private class WorkspaceDragListener implements View.OnDragListener {

        private static final String TAG = "WorkspaceDragListener";

        public boolean onDrag(View view, DragEvent event) {
            RelativeLayout workspace = (RelativeLayout) view;
            SynthWidget synthWidget = (SynthWidget) event.getLocalState();

            switch(event.getAction()) {

                case DragEvent.ACTION_DRAG_STARTED:
                    // Ignore the event.
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    synthWidget.inflateFrom(workspace, mCellSize);
                    workspace.addView(synthWidget.getView(), synthWidget.getLayoutParams());
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    int column = (int) Math.floor(event.getX()/mCellSize);
                    int row = (int) Math.floor(event.getY()/mCellSize);

                    synthWidget.setPosition(column, row);

                    int columnSpan = synthWidget.getColumnSpan();
                    int rowSpan = synthWidget.getRowSpan();
                    boolean canPlace = canPlaceOn(column, row, columnSpan, rowSpan);

                    ViewGroup synthWidgetViewGroup = (ViewGroup) synthWidget.getView();
                    View redOverlay = synthWidgetViewGroup.findViewById(R.id.red_overlay);

                    if (!canPlace && redOverlay == null) {
                        LayoutInflater inflater = LayoutInflater.from(workspace.getContext());
                        inflater.inflate(R.layout.red_overlay, synthWidgetViewGroup);
                    } else if (canPlace && redOverlay != null) {
                        synthWidgetViewGroup.removeView(redOverlay);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    workspace.removeView(synthWidget.getView());
                    return true;

                case DragEvent.ACTION_DROP:
                    column = (int) Math.floor(event.getX()/mCellSize);
                    row = (int) Math.floor(event.getY()/mCellSize);
                    columnSpan = synthWidget.getColumnSpan();
                    rowSpan = synthWidget.getRowSpan();

                    if (canPlaceOn(column, row, columnSpan, rowSpan)) {
                        changeCellVacancy(true, column, row, columnSpan, rowSpan);
                        
                        SynthWidgetDialogFragment dialogFragment = new SynthWidgetDialogFragment();
                        dialogFragment.setSynthWidget(synthWidget);
                        dialogFragment.show(
                                getSupportFragmentManager(),
                                DIALOG_SYNTH_WIDGET_SETUP);
                        return true;
                    } else {
                        workspace.removeView(synthWidget.getView());
                        return false;
                    }

                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        // Drop did not happen on workspace.
                        synthWidget.deflate();
                    }
                    return true;

                default:
                    Log.e(TAG,"Unknown action received by OnDragListener: " + event.getAction());
                    return true;
            }
        }
    }
}
