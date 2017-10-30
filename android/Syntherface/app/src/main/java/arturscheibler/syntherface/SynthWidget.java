package arturscheibler.syntherface;

import android.view.ViewGroup;

abstract class SynthWidget {
    
    abstract int getIconResourceId();
    
    abstract void inflateAndAttachTo(ViewGroup root);
    
}
