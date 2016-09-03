package me.abhishekz.azhealthmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    float[] values = new float[] { 0,5,3,9,5,0,6,4,7,8,4,2,4,6,4,4,6,3,2,1,0,8 };
    String[] verlabels = new String[] { "3000", "2500", "2000", "1500", "1000", "500", "0" };
    String[] horlabels = new String[] { "0", "50", "100", "150", "200", "250", "300", "350", "400" };
    float[] emptyFloat = new float[] {0};
    GraphView graphView, clearView;
    ViewGroup layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void displayGraph(View view) {
        layout = (ViewGroup) findViewById(R.id.graphLayout);

        graphView = new GraphView(this, values, "AZ Health Monitor",horlabels, verlabels, GraphView.LINE);
        graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.removeAllViews();
        layout.addView(graphView);
    }

    public void clearGraph(View view){
        layout = (ViewGroup) findViewById(R.id.graphLayout);

        clearView = new GraphView(this, emptyFloat, "AZ Health Monitor",horlabels, verlabels, GraphView.LINE);
        clearView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.removeAllViews();
        layout.addView(clearView);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
