package arturscheibler.syntherface;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

class ComponentViewHolder extends RecyclerView.ViewHolder {
    
    Button mComponentView = null;
    
    ComponentViewHolder(View componentView) {
        super(componentView);
        mComponentView = (Button) componentView;
    }
}

class ComponentAdapter extends RecyclerView.Adapter<ComponentViewHolder> {
    
    private Context mContext = null;
    private ArrayList<String> mStrings = null;
    
    ComponentAdapter(ArrayList<String> strings) {
        mStrings = strings;
    }
    
    public ComponentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View componentView = inflater.inflate(R.layout.component_list_item, parent, false);
        return new ComponentViewHolder(componentView);
    }
    
    public void onBindViewHolder(final ComponentViewHolder holder, final int position) {
        Button componentButton = holder.mComponentView;
        componentButton.setText(mStrings.get(position));
        componentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, Integer.toString(holder.getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    public int getItemCount() {
        return mStrings.size();
    }
}
