package arturscheibler.syntherface;

import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

class Workspace {

    private static final String TAG = "Workspace";
    private int mColumns = 0;
    private int mRows = 0;
    private float mCellSize = 0;
    private List<List<Boolean>> mCellIsOccupied = new ArrayList<>();

    Workspace(final RelativeLayout workspace) {
        workspace.setOnDragListener(new WorkspaceDragListener());
        workspace.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float gridCellTargetSize = workspace.getContext()
                        .getResources().getDimension(R.dimen.cell_target_size);
                mColumns = Math.round(workspace.getWidth()/gridCellTargetSize);
                mCellSize = workspace.getWidth()/mColumns;
                mRows = (int) Math.ceil((double) workspace.getHeight()/mCellSize);
            }
        });
    }
    
    private void changeCellVacancy(
            boolean occupied, int column, int row, int columnSpan, int rowSpan) {
        while (column + columnSpan > mCellIsOccupied.size()) {
            mCellIsOccupied.add(new ArrayList<Boolean>());
        }
        
        for (int c = column; c < column + columnSpan; c++) {
            List<Boolean> rows = mCellIsOccupied.get(c);
            while (row + rowSpan > rows.size()) {
                rows.add(false);
            }
            
            for (int r = row; r < row + rowSpan; r++) {
                rows.set(r, occupied);
            }
        }
    }
    
    private boolean canPlaceOn(int column, int row, int columnSpan, int rowSpan) {
        while (column + columnSpan > mCellIsOccupied.size()) {
            mCellIsOccupied.add(new ArrayList<Boolean>());
        }
        
        for (int c = column; c < column + columnSpan; c++) {
            List<Boolean> rows = mCellIsOccupied.get(c);
            while (row + rowSpan > rows.size()) {
                rows.add(false);
            }
            
            for (int r = row; r < row + rowSpan; r++) {
                if (rows.get(r)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private class WorkspaceDragListener implements View.OnDragListener {

        private static final String TAG = "WorkspaceDragListener";

        public boolean onDrag(View view, DragEvent event) {
            RelativeLayout workspace = (RelativeLayout) view;
            SynthWidget synthWidget = (SynthWidget) event.getLocalState();

            switch(event.getAction()) {

                case DragEvent.ACTION_DRAG_STARTED:
                    // Ignore the event.
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    synthWidget.inflateFrom(workspace, mCellSize);
                    workspace.addView(synthWidget.getView(), synthWidget.getLayoutParams());
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    int column = (int) Math.floor(event.getX()/mCellSize);
                    int row = (int) Math.floor(event.getY()/mCellSize);

                    synthWidget.setPosition(column, row);

                    int columnSpan = synthWidget.getColumnSpan();
                    int rowSpan = synthWidget.getRowSpan();
                    boolean canPlace = canPlaceOn(column, row, columnSpan, rowSpan);
                    
                    ViewGroup synthWidgetViewGroup = (ViewGroup) synthWidget.getView();
                    View redOverlay = synthWidgetViewGroup.findViewById(R.id.red_overlay);
                    
                    if (!canPlace && redOverlay == null) {
                        LayoutInflater inflater = LayoutInflater.from(workspace.getContext());
                        inflater.inflate(R.layout.red_overlay, synthWidgetViewGroup);
                    } else if (canPlace && redOverlay != null) {
                        synthWidgetViewGroup.removeView(redOverlay);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    workspace.removeView(synthWidget.getView());
                    return true;

                case DragEvent.ACTION_DROP:
                    column = (int) Math.floor(event.getX()/mCellSize);
                    row = (int) Math.floor(event.getY()/mCellSize);
                    columnSpan = synthWidget.getColumnSpan();
                    rowSpan = synthWidget.getRowSpan();
                    
                    if (canPlaceOn(column, row, columnSpan, rowSpan)) {
                        changeCellVacancy(true, column, row, columnSpan, rowSpan);
                        return true;
                    } else {
                        workspace.removeView(synthWidget.getView());
                        return false;
                    }

                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        // Drop did not happen on workspace.
                        synthWidget.deflate();
                    }
                    return true;

                default:
                    Log.e(TAG,"Unknown action received by OnDragListener: " + event.getAction());
                    return true;
            }
        }
    }
}
