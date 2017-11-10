package arturscheibler.syntherface;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

abstract class SynthWidget {

    private View mShadow = null;
    private View mView = null;
    private RelativeLayout.LayoutParams mViewLayoutParams = null;

    void setShadowView(@NonNull View shadow) {
        this.mShadow = shadow;
    }

    int getShadowSize() {
        return mShadow.getWidth();
    }

    View getView() {
        return mView;
    }


    RelativeLayout.LayoutParams getViewLayoutParams() {
        return mViewLayoutParams;
    }
    
    void inflateFrom(@NonNull RelativeLayout root) {
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        mView = inflater.inflate(getViewResourceId(), root, false);
        mViewLayoutParams = (RelativeLayout.LayoutParams) mView.getLayoutParams();
    }

    void deflate() {
        mView = null;
        mViewLayoutParams = null;
    }
    
    void setPosition(int x, int y) {
        mViewLayoutParams.leftMargin = x >= 0 ? x : 0;
        mViewLayoutParams.topMargin = y >= 0 ? y : 0;
        mView.setLayoutParams(mViewLayoutParams);
    }
    
    abstract int getViewResourceId();
    
    abstract int getIconResourceId();
    
}
