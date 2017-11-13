package arturscheibler.syntherface;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

abstract class SynthWidget {

    private View mShadow = null;
    private View mView = null;
    private RelativeLayout.LayoutParams mLayoutParams = null;
    private int mColumnSpan = 1;
    private int mRowSpan = 1;

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

    private int getColumnSpan() {
        return mColumnSpan;
    }

    private void setColumnSpan(int columnSpan) {
        mColumnSpan = columnSpan;
    }

    private int getRowSpan() {
        return mRowSpan;
    }

    private void setRowSpan(int rowSpan) {
        mRowSpan = rowSpan;
    }

    void inflateFrom(@NonNull RelativeLayout root, float cellSize) {
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        
        View view = inflater.inflate(getViewResourceId(), root, false);
        setView(view);
        
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = (int) cellSize*getColumnSpan();
        layoutParams.height = (int) cellSize*getRowSpan();
        setLayoutParams(layoutParams);
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
