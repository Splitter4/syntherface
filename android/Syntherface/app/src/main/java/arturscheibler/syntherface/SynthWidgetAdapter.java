package arturscheibler.syntherface;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

class SynthWidgetViewHolder extends RecyclerView.ViewHolder {
    
    ImageView mImageView = null;
    
    SynthWidgetViewHolder(View synthWidgetView) {
        super(synthWidgetView);
        mImageView = (ImageView) synthWidgetView;
    }
}

class SynthWidgetAdapter extends RecyclerView.Adapter<SynthWidgetViewHolder> {
    
    private ArrayList<SynthWidget> mSynthWidgets = null;
    
    SynthWidgetAdapter(ArrayList<SynthWidget> synthWidgets) {
        mSynthWidgets = synthWidgets;
    }
    
    public SynthWidgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View synthWidgetView = inflater.inflate(R.layout.synth_widget_list_item, parent, false);
        return new SynthWidgetViewHolder(synthWidgetView);
    }
    
    public void onBindViewHolder(final SynthWidgetViewHolder holder, final int position) {
        holder.mImageView.setImageResource(mSynthWidgets.get(position).getIconResourceId());
        holder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
                SynthWidget synthWidget = mSynthWidgets.get(holder.getAdapterPosition());
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(null, shadow, synthWidget, 0);
                } else {
                    // startDrag was deprecated on API 24.
                    view.startDrag(null, shadow, synthWidget, 0);
                }
        
                return true;
            }
        });
    }
    
    public int getItemCount() {
        return mSynthWidgets.size();
    }
}
