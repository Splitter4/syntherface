package arturscheibler.syntherface;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;

class Knob extends SynthWidget {
    
    private int mValue = 0;

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    @Override
    int getViewResourceId() {
        return R.layout.knob;
    }
    
    @Override
    int getIconResourceId() {
        return R.mipmap.ic_knob;
    }

    @Override
    int getDialogParametersLayoutResourceId() {
        return R.layout.dialog_knob;
    }
    
    @Override
    void setParametersFrom(ViewGroup parametersLayout) throws InvalidSynthWidgetParameterException {
        Context context = parametersLayout.getContext();

        EditText valueView = parametersLayout.findViewById(R.id.value);
        String textValue = valueView.getText().toString();
        if (textValue.isEmpty()) {
            throw new InvalidSynthWidgetParameterException(
                    context.getString(R.string.knob_value_required));
        }

        try {
            setValue(Integer.parseInt(textValue));
        } catch (NumberFormatException numberFormatException) {
            throw new InvalidSynthWidgetParameterException(
                    context.getString(R.string.knob_value_not_integer),
                    numberFormatException);
        }
    }
}
