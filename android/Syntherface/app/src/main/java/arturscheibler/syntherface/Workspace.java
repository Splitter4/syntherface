package arturscheibler.syntherface;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

class Workspace {

    private static final String TAG = "Workspace";
    private int mColumns;
    private int mRows;

    Workspace(final RelativeLayout workspace) {
        workspace.setOnDragListener(new WorkspaceDragListener());
        workspace.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mColumns = Math.round(workspace.getWidth()/synthWidgetTargetSize);
                mRows = Math.round(workspace.getHeight()/synthWidgetTargetSize);
                float gridCellTargetSize = workspace.getContext()
                        .getResources().getDimension(R.dimen.cell_target_size);
            }
        });
    }

    private class WorkspaceDragListener implements View.OnDragListener {

        private static final String TAG = "WorkspaceDragListener";

        public boolean onDrag(View view, DragEvent event) {
            RelativeLayout workspace = (RelativeLayout) view;
            SynthWidget synthWidget = (SynthWidget) event.getLocalState();

            switch(event.getAction()) {

                case DragEvent.ACTION_DRAG_STARTED:
                    // Ignore the event.
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    synthWidget.inflateFrom(workspace);
                    workspace.addView(synthWidget.getView(), synthWidget.getViewLayoutParams());
                    break;

                case DragEvent.ACTION_DRAG_LOCATION:
                    double columnWidth = (double) workspace.getWidth()/ mColumns;
                    double rowHeight = (double) workspace.getHeight()/ mRows;

                    int shadowSize = synthWidget.getShadowSize();

                    int columnIndex = (int) Math.floor((event.getX() - shadowSize/2.0)/columnWidth);
                    int rowIndex = (int) Math.floor((event.getY() - shadowSize/2.0)/rowHeight);

                    int x = (int) (columnIndex*columnWidth);
                    int y = (int) (rowIndex*rowHeight);

                    synthWidget.setPosition(x, y);

                    Log.d(TAG, "x: " + (event.getX() - shadowSize/2) + " y: " + (event.getY() - shadowSize/2));
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    workspace.removeView(synthWidget.getView());
                    break;

                case DragEvent.ACTION_DROP:
                    // Ignore the event.
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        // Drop did not happen on workspace.
                        synthWidget.deflate();
                    }
                    break;

                default:
                    Log.e(TAG,"Unknown action received by OnDragListener: " + event.getAction());
                    break;
            }

            return true;
        }
    }
}
