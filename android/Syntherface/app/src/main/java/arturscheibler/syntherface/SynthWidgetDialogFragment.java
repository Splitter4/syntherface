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
import android.widget.Button;

public class SynthWidgetDialogFragment extends DialogFragment {

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
                                // automatically dismiss the dialog on click. This way, we can
                                // validate the data and approve of the dialog's dismissal or not.
                                
                                // However, we still need to instantiate this listener here because,
                                // on older versions of Android, unless we pass a handler, the
                                // button doesn't get instantiated.
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
