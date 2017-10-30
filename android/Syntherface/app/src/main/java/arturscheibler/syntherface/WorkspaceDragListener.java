package arturscheibler.syntherface;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

class WorkspaceDragListener implements View.OnDragListener {
    
    private static final String TAG = "WorkspaceDragListener";
    
    public boolean onDrag(View view, DragEvent event) {
    
        GridLayout workspace = (GridLayout) view;
        
        switch(event.getAction()) {
            
            case DragEvent.ACTION_DRAG_STARTED:
                // Ignore the event.
                return true;
            
            case DragEvent.ACTION_DRAG_ENTERED:
                
                workspace.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
                
                // Invalidate the view to force a redraw in the new tint.
                workspace.invalidate();
                
                return true;
            
            case DragEvent.ACTION_DRAG_LOCATION:
                // Ignore the event.
                return true;
            
            case DragEvent.ACTION_DRAG_EXITED:
                
                workspace.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
                
                // Invalidate the view to force a redraw in the new tint.
                workspace.invalidate();
                
                return true;
            
            case DragEvent.ACTION_DROP:
                
                SynthWidget synthWidget = (SynthWidget) event.getLocalState();
                synthWidget.inflateAndAttachTo(workspace);
                
                workspace.getBackground().clearColorFilter();
                
                // Invalidates the view to force a redraw.
                workspace.invalidate();
                
                return true;
            
            case DragEvent.ACTION_DRAG_ENDED:
                
                workspace.getBackground().clearColorFilter();
                
                // Invalidates the view to force a redraw.
                workspace.invalidate();
                
                if (event.getResult()) {
                    Toast.makeText(workspace.getContext(), "The drop was handled.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(workspace.getContext(), "The drop didn't work.", Toast.LENGTH_LONG).show();
                }
                
                return true;
            
            default:
                Log.e(TAG,"Unknown action type received by OnDragListener.");
                return false;
        }
    }
}