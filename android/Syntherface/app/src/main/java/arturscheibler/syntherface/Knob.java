package arturscheibler.syntherface;

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
    int getDialogLayoutResourceId() {
        return R.layout.dialog_knob;
    }
}
