package arturscheibler.syntherface;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

class Workspace {

    private static final String TAG = "Workspace";
    private int mColumns = 0;
    private int mRows = 0;
    private float mCellSize = 0;

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
                    synthWidget.inflateFrom(workspace, mCellSize);
                    workspace.addView(synthWidget.getView(), synthWidget.getLayoutParams());
                    break;

                case DragEvent.ACTION_DRAG_LOCATION:
                    int column = (int) Math.floor(event.getX()/mCellSize);
                    int row = (int) Math.floor(event.getY()/mCellSize);

                    synthWidget.setPosition(column, row);
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
