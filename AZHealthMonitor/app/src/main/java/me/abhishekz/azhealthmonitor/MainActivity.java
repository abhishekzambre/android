package me.abhishekz.azhealthmonitor;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    float[] values = new float[60];
    String[] verlabels = new String[] { "3000", "2500", "2000", "1500", "1000", "500", "0" };
    String[] horlabels = new String[] { "0", "50", "100", "150", "200", "250", "300", "350", "400" };
    float[] emptyFloat = new float[] {0};
    GraphView graphView, clearView;
    ViewGroup layout;

    boolean SensorInitialized=false;

    TextView textView;

    SensorManager mySensorManager;
    Sensor LightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (ViewGroup) findViewById(R.id.graphLayout);

        textView = (TextView) findViewById(R.id.textView3);

        graphView = new GraphView(this, values, "AZ Health Monitor",horlabels, verlabels, GraphView.LINE);
        graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(graphView);
    }

    public void displayGraph(View view) {

        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (LightSensor != null) {
            mySensorManager.registerListener(LightSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        SensorInitialized=true;

        layout = (ViewGroup) findViewById(R.id.graphLayout);

        graphView = new GraphView(this, emptyFloat, "AZ Health Monitor",horlabels, verlabels, GraphView.LINE);
        graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        for(int i=0; i<values.length; i++)
            values[i]=0f;
        layout.removeAllViews();
        layout.addView(graphView);
    }

    public void clearGraph(View view){
        layout = (ViewGroup) findViewById(R.id.graphLayout);

        clearView = new GraphView(this, emptyFloat, "AZ Health Monitor",horlabels, verlabels, GraphView.LINE);
        clearView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.removeAllViews();
        layout.addView(clearView);

        for(int i=0; i<values.length; i++)
            values[i]=0f;

        if (SensorInitialized)
            mySensorManager.unregisterListener(LightSensorListener,mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        else
            SensorInitialized=false;

        textView.setText(R.string.textMonitorVal);
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

    private final SensorEventListener LightSensorListener
            = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                textView.setText(String.valueOf(event.values[0]));
                replaceQueue(event.values[0]);
                graphView.invalidate();
                graphView.setValues(values);
            }

        }

    };

    void replaceQueue(float a){
        for(int i=1; i<values.length; i++)
            values[i-1]=values[i];
        values[values.length-1]=a;
    }
}
