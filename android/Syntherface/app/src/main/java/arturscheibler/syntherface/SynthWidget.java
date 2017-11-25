package arturscheibler.syntherface;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

abstract class SynthWidget {

    private View mShadow = null;
    private View mView = null;
    private RelativeLayout.LayoutParams mLayoutParams = null;
    private int mColumn = 0;
    private int mRow = 0;
    private int mColumnSpan = 1;
    private int mRowSpan = 1;
    private static float mCellSize = 0;

    void setShadowView(@NonNull View shadow) {
        mShadow = shadow;
    }

    View getView() {
        return mView;
    }

    private void setView(View view) {
        mView = view;
    }
    
    RelativeLayout.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }
    
    private void setLayoutParams(RelativeLayout.LayoutParams layoutParams) {
        mLayoutParams = layoutParams;
        
        View view = getView();
        if (view != null) {
            view.setLayoutParams(layoutParams);
        }
    }

    private int getColumn() {
        return mColumn;
    }

    private void setColumn(int column) {
        mColumn = column;
    }

    private int getRow() {
        return mRow;
    }

    private void setRow(int row) {
        mRow = row;
    }

    int getColumnSpan() {
        return mColumnSpan;
    }

    private void setColumnSpan(int columnSpan) {
        mColumnSpan = columnSpan;
    }

    int getRowSpan() {
        return mRowSpan;
    }

    private void setRowSpan(int rowSpan) {
        mRowSpan = rowSpan;
    }

    private static float getCellSize() {
        return mCellSize;
    }

    private static void setCellSize(float cellSize) {
        mCellSize = cellSize;
    }

    void inflateFrom(@NonNull RelativeLayout root, float cellSize) {
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        
        View view = inflater.inflate(getViewResourceId(), root, false);
        setView(view);
        
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = (int) cellSize*getColumnSpan();
        layoutParams.height = (int) cellSize*getRowSpan();
        setLayoutParams(layoutParams);
        
        setCellSize(cellSize);
    }

    void deflate() {
        setView(null);
        setLayoutParams(null);
    }
    
    void setPosition(int column, int row) {
        setColumn(column);
        setRow(row);
        
        int x = (int) (column*getCellSize());
        int y = (int) (row*getCellSize());
        
        RelativeLayout.LayoutParams layoutParams = getLayoutParams();
        layoutParams.leftMargin = x >= 0 ? x : 0;
        layoutParams.topMargin = y >= 0 ? y : 0;
        setLayoutParams(layoutParams);
    }
    
    void showSetupDialog(FragmentManager manager, String tag) {
        SynthWidgetDialogFragment dialogFragment = new SynthWidgetDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(
                SynthWidgetDialogFragment.PARAMETERS_LAYOUT_RESOURCE_ID,
                getDialogLayoutResourceId());
        dialogFragment.setArguments(arguments);
        dialogFragment.show(manager, tag);
    }

    abstract int getIconResourceId();
    
    abstract int getViewResourceId();
    
    abstract int getDialogLayoutResourceId();

    public static class SynthWidgetDialogFragment extends DialogFragment {

        public static final String PARAMETERS_LAYOUT_RESOURCE_ID = "PARAMETERS_LAYOUT_RESOURCE_ID";

        @Override @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_synth_widget, null);

            Bundle arguments = getArguments();
            if (arguments != null) {
                View synthWidgetParametersView =
                        inflater.inflate(arguments.getInt(PARAMETERS_LAYOUT_RESOURCE_ID), null);

                ViewGroup parametersContainerView =
                        dialogView.findViewById(R.id.synth_widget_parameters);

                parametersContainerView.addView(synthWidgetParametersView);
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setView(dialogView)
                    .setPositiveButton(
                            R.string.synth_widget_dialog_positive_button,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // We're overriding this button's behavior later so that it won't
                            // automatically dismiss the dialog on click.
                            // However, we still need this because, on older versions of Android, 
                            // unless we pass a handler, the button doesn't get instantiated.
                        }
                    })
                    .setNegativeButton(R.string.synth_widget_dialog_negative_button, null);
            
            return builder.create();
        }
        
        // When we get to onResume(), dialog.show() was already called, so we can override the
        // positive button's behavior.
        @Override
        public void onResume() {
            super.onResume();
            
            final AlertDialog dialog = (AlertDialog) getDialog();
            if(dialog != null) {
                Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        }
        
        @Override
        public void onCancel(DialogInterface dialog) {
            
        }
    }
}
