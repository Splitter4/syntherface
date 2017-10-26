package arturscheibler.syntherface;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        ArrayList<String> strings = new ArrayList<>();
        strings.add("a");
        strings.add("b");
        strings.add("c");
        strings.add("d");
        strings.add("e");
        
        RecyclerView componentList = (RecyclerView) findViewById(R.id.component_list_rv);
        componentList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        componentList.setAdapter(new ComponentAdapter(strings));
        
    }
}
