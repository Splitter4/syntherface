package arturscheibler.syntherface;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

abstract class SynthWidget {

    private View mShadow = null;
    private View mView = null;
    private RelativeLayout.LayoutParams mLayoutParams = null;

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
    
    void inflateFrom(@NonNull RelativeLayout root) {
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        View view = inflater.inflate(getViewResourceId(), root, false);
        setView(view);
        setLayoutParams((RelativeLayout.LayoutParams) view.getLayoutParams());
    }

    void deflate() {
        setView(null);
        setLayoutParams(null);
    }
    
    void setPosition(int x, int y) {
        RelativeLayout.LayoutParams layoutParams = getLayoutParams();
        layoutParams.leftMargin = x >= 0 ? x : 0;
        layoutParams.topMargin = y >= 0 ? y : 0;
        setLayoutParams(layoutParams);
    }
    
    abstract int getViewResourceId();
    
    abstract int getIconResourceId();
    
}
