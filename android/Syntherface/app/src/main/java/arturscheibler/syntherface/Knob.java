package arturscheibler.syntherface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class Knob extends SynthWidget {
    
    private View mView = null;
    
    @Override
    int getIconResourceId() {
        return R.mipmap.ic_knob;
    }
    
    @Override
    void inflateAndAttachTo(ViewGroup root) {
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        inflater.inflate(R.layout.knob, root);
    }
}
