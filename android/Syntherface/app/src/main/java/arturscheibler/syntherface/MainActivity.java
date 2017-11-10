package arturscheibler.syntherface;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        RelativeLayout workspace = findViewById(R.id.workspace_rl);
        workspace.setOnDragListener(new WorkspaceDragListener());
    
        ArrayList<SynthWidget> synthWidgets = new ArrayList<>();
        synthWidgets.add(new Knob());
        
        RecyclerView synthWidgetList = findViewById(R.id.synth_widget_list_rv);
        synthWidgetList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        synthWidgetList.setAdapter(new SynthWidgetAdapter(synthWidgets));
    }
}
