package arturscheibler.syntherface;

import android.view.LayoutInflater;
import android.view.ViewGroup;

class Knob extends SynthWidget {
    
    int getViewResourceId() {
        return R.layout.knob;
    }
    
    int getIconResourceId() {
        return R.mipmap.ic_knob;
    }
    
    void inflateAndAttachTo(ViewGroup root) {
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        inflater.inflate(R.layout.knob, root);
    }
}
