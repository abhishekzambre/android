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

    float[] values = new float[] { 0,0,0,0,0,0,0,0,0,0 };
    String[] verlabels = new String[] { "3000", "2500", "2000", "1500", "1000", "500", "0" };
    String[] horlabels = new String[] { "0", "50", "100", "150", "200", "250", "300", "350", "400" };
    float[] emptyFloat = new float[] {0};
    GraphView graphView, clearView;
    ViewGroup layout;

    TextView textView;

    SensorManager mySensorManager;
    Sensor LightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void displayGraph(View view) {
        layout = (ViewGroup) findViewById(R.id.graphLayout);

        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (LightSensor != null) {
            mySensorManager.registerListener(LightSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        }

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

        mySensorManager.unregisterListener(LightSensorListener,mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        textView.setText(R.string.textMonitor);
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
                textView = (TextView) findViewById(R.id.textView2);
                textView.setText("Monitor : " + String.valueOf(event.values[0]));
                replaceQueue(event.values[0]);
                graphView.invalidate();
                graphView.setValues(values);
            }

        }

    };

    void replaceQueue(float a){
        values[0]=values[1];
        values[1]=values[2];
        values[2]=values[3];
        values[3]=values[4];
        values[4]=values[5];
        values[5]=values[6];
        values[6]=values[7];
        values[7]=values[8];
        values[8]=values[9];
        values[9]=a;
    }
}
