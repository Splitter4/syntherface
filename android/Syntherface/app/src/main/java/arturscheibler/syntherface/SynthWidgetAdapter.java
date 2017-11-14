package arturscheibler.syntherface;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

class SynthWidgetViewHolder extends RecyclerView.ViewHolder {
    
    private ImageView mIcon = null;

    ImageView getIcon() {
        return mIcon;
    }

    SynthWidgetViewHolder(View icon) {
        super(icon);
        mIcon = (ImageView) icon;
    }
}

class SynthWidgetAdapter extends RecyclerView.Adapter<SynthWidgetViewHolder> {
    
    private ArrayList<SynthWidget> mSynthWidgets = null;
    
    SynthWidgetAdapter(ArrayList<SynthWidget> synthWidgets) {
        mSynthWidgets = synthWidgets;
    }
    
    public SynthWidgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View icon = inflater.inflate(R.layout.synth_widget_list_item, parent, false);
        return new SynthWidgetViewHolder(icon);
    }
    
    public void onBindViewHolder(final SynthWidgetViewHolder holder, final int position) {
        ImageView icon = holder.getIcon();
        icon.setImageResource(mSynthWidgets.get(position).getIconResourceId());
        icon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
                SynthWidget synthWidget = mSynthWidgets.get(holder.getAdapterPosition());
                synthWidget.setShadowView(holder.getIcon());
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // startDrag was deprecated on API 24.
                    view.startDragAndDrop(null, shadow, synthWidget, 0);
                } else {
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
