package arturscheibler.syntherface;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SynthWidgetDialogFragment extends DialogFragment {
    
    interface OnCancelListener {
        void onCancel();
    }

    public static final String PARAMETERS_LAYOUT_RESOURCE_ID = "PARAMETERS_LAYOUT_RESOURCE_ID";
    private static final String TAG = "SynthWidgetDialogFragme";
    
    private SynthWidget mSynthWidget = null;
    private View mDialogView = null;
    private OnCancelListener mOnCancelListener = null;
    
    private SynthWidget getSynthWidget() {
        return mSynthWidget;
    }

    public void setSynthWidget(SynthWidget synthWidget) {
        mSynthWidget = synthWidget;
    }

    private View getDialogView() {
        return mDialogView;
    }

    private void setDialogView(View dialogView) {
        mDialogView = dialogView;
    }

    private OnCancelListener getOnCancelListener() {
        return mOnCancelListener;
    }
    
    public void setOnCancelListener(OnCancelListener listener) {
        mOnCancelListener = listener;
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(
                R.layout.dialog_synth_widget,
                null);
        setDialogView(dialogView);

        SynthWidget synthWidget = getSynthWidget();
        if (synthWidget != null) {
            int parametersLayoutResourceId = synthWidget.getDialogParametersLayoutResourceId();
            if (parametersLayoutResourceId != 0) {
                ViewGroup parametersLayout = (ViewGroup) inflater.inflate(
                        parametersLayoutResourceId,
                        null);
                
                // Make soft keyboard show "Done" button for last input view.
                int childIndex = parametersLayout.getChildCount() - 1;
                while (childIndex >= 0) {
                    View parameterView = parametersLayout.getChildAt(childIndex);
                    if (parameterView instanceof TextView) {
                        ((TextView) parameterView).setImeOptions(EditorInfo.IME_ACTION_DONE);
                        break;
                    }
                    
                    childIndex--;
                }
    
                ViewGroup parametersLayoutContainer =
                        dialogView.findViewById(R.id.synth_widget_parameters_container);
    
                parametersLayoutContainer.addView(parametersLayout);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setPositiveButton(
                        R.string.synth_widget_dialog_positive_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // We're overriding this button's behavior later so that it won't
                                // automatically dismiss the dialog on click. This way, we can
                                // validate the data and approve of the dialog's dismissal or not.
                                
                                // However, we still need to instantiate this listener here because,
                                // on older versions of Android, unless we pass a handler, the
                                // button doesn't get instantiated.
                            }
                        })
                .setNegativeButton(R.string.synth_widget_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

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
                    View dialogView = getDialogView();
                    
                    EditText nameView = dialogView.findViewById(R.id.name);
                    String name = nameView.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.name_required),
                                Toast.LENGTH_LONG)
                                .show();
                    } else {
                        SynthWidget synthWidget = getSynthWidget();
                        
                        ViewGroup parametersLayoutContainer =
                                dialogView.findViewById(R.id.synth_widget_parameters_container);
                        ViewGroup parametersLayout =
                                (ViewGroup) parametersLayoutContainer.getChildAt(0);
                        if (synthWidget != null && parametersLayout != null) {
                            try {
                                synthWidget.setParametersFrom(parametersLayout);
                                synthWidget.setName(name);
                                dialog.dismiss();
                            } catch (SynthWidget.InvalidSynthWidgetParameterException e) {
                                Toast.makeText(
                                        getActivity(),
                                        e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        getOnCancelListener().onCancel();
    }
}
