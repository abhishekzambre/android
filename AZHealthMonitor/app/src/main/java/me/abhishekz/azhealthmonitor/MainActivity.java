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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void displayGraph(View view) {
        ViewGroup layout = (ViewGroup) findViewById(R.id.graphLayout);

        float[] values = new float[] { 0,5,3,9,5,0,5,3,9,5,0,5,3,9,5,0,5,3,9,5,0 };
        String[] verlabels = new String[] { "3000", "2500", "2000", "1500", "1000", "500", "0" };
        String[] horlabels = new String[] { "0", "50", "100", "150", "200", "250", "300", "350", "400" };
        GraphView graphView = new GraphView(this, values, "AZ Health Monitor",horlabels, verlabels, GraphView.LINE);

        graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(graphView);
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
