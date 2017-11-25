package arturscheibler.syntherface;

class Knob extends SynthWidget {
    
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
