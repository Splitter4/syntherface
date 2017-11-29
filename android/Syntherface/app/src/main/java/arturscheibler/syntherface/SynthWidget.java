package arturscheibler.syntherface;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

abstract class SynthWidget {

    private String mName = null;
    private View mShadow = null;
    private View mView = null;
    private RelativeLayout.LayoutParams mLayoutParams = null;
    private int mColumn = 0;
    private int mRow = 0;
    private int mColumnSpan = 1;
    private int mRowSpan = 1;
    private static float mCellSize = 0;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

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

    private int getColumn() {
        return mColumn;
    }

    private void setColumn(int column) {
        mColumn = column;
    }

    private int getRow() {
        return mRow;
    }

    private void setRow(int row) {
        mRow = row;
    }

    int getColumnSpan() {
        return mColumnSpan;
    }

    private void setColumnSpan(int columnSpan) {
        mColumnSpan = columnSpan;
    }

    int getRowSpan() {
        return mRowSpan;
    }

    private void setRowSpan(int rowSpan) {
        mRowSpan = rowSpan;
    }

    private static float getCellSize() {
        return mCellSize;
    }

    private static void setCellSize(float cellSize) {
        mCellSize = cellSize;
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
        
        setCellSize(cellSize);
    }

    void deflate() {
        setView(null);
        setLayoutParams(null);
    }
    
    void setPosition(int column, int row) {
        setColumn(column);
        setRow(row);
        
        int x = (int) (column*getCellSize());
        int y = (int) (row*getCellSize());
        
        RelativeLayout.LayoutParams layoutParams = getLayoutParams();
        layoutParams.leftMargin = x >= 0 ? x : 0;
        layoutParams.topMargin = y >= 0 ? y : 0;
        setLayoutParams(layoutParams);
    }

    abstract int getIconResourceId();
    
    abstract int getViewResourceId();
    
    abstract int getDialogLayoutResourceId();
    class InvalidSynthWidgetParameterException extends Exception {
        InvalidSynthWidgetParameterException() {
            super();
        }
        
        InvalidSynthWidgetParameterException(String message) {
            super(message);
        }
        
        InvalidSynthWidgetParameterException(Throwable cause) {
            super(cause);
        }
        
        InvalidSynthWidgetParameterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
