package me.abhishekz.azhealthmonitor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper myDB;
    float[] values = new float[10];
    String[] verlabels = new String[]{"3000", "2500", "2000", "1500", "1000", "500", "0"};
    String[] horlabels = new String[]{"0", "50", "100", "150", "200", "250", "300", "350", "400"};
    boolean SensorInitialized = false;
    boolean show_graph = false;
    private long lastUpdate = 0;
    String table_name="";

    GraphView graphView;
    ViewGroup layout;
    TextView textView, textFirst, textLast, textID, textAge;
    RadioGroup radioSex;
    Button startButton;

    private final SensorEventListener AccelSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > 1000) {
                    lastUpdate = curTime;
                    boolean isInserted = myDB.insertData(Long.toString(curTime), event.values[0], event.values[1], event.values[2]);
                    Toast.makeText(getApplicationContext(), "DB Status " + isInserted, Toast.LENGTH_SHORT).show();

                    if (show_graph) {
                        Cursor res = myDB.getAllData();
                        int i = 9;
                        while (res.moveToNext()) {
                            values[i] = res.getLong(1);
                            i--;
                        }
                        graphView.invalidate();
                        graphView.setValues(values);
                    }
                }
            }
        }

    };
    InputMethodManager inputManager;
    SensorManager mySensorManager;
    Sensor AccelSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button)findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView3);
        layout = (ViewGroup) findViewById(R.id.graphLayout);

        startButton.setEnabled(false);

        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        AccelSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        graphView = new GraphView(this, values, "AZ Health Monitor", horlabels, verlabels, GraphView.LINE);
        graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(graphView);

    }

    public void createTable(View view){

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        textFirst = (TextView) findViewById(R.id.editText);
        textLast = (TextView) findViewById(R.id.editText2);
        textID = (TextView) findViewById(R.id.editText3);
        textAge = (TextView) findViewById(R.id.editText4);
        radioSex = (RadioGroup) findViewById(R.id.radioSex);

        int index = radioSex.indexOfChild(findViewById(radioSex.getCheckedRadioButtonId()));
        String sex = ((index == 1) ? "Female" : "Male" );


        if (textFirst.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "First Name is empty", Toast.LENGTH_SHORT).show();
        } else if (textLast.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Last Name is empty", Toast.LENGTH_SHORT).show();
        } else if (textID.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Patient ID is empty", Toast.LENGTH_SHORT).show();
        } else if (textAge.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Age is empty", Toast.LENGTH_SHORT).show();
        } else {
            table_name = textFirst.getText().toString() + "_" + textLast.getText().toString() + "_" + textID.getText().toString() + "_" + textAge.getText().toString() + "_" + sex;
            table_name = table_name.replaceAll("\\s+","");
            myDB = new DatabaseHelper(this, table_name);
           // boolean isInserted = myDB.insertData("Asdf","Ghjk","Qwer");
           // Toast.makeText(getApplicationContext(), "DB Status " + isInserted, Toast.LENGTH_SHORT).show();
        }
        textFirst.setEnabled(false);
        textLast.setEnabled(false);
        textID.setEnabled(false);
        textAge.setEnabled(false);
        startButton.setEnabled(true);

        if(!SensorInitialized) {
            if (AccelSensor != null) {
                mySensorManager.registerListener(AccelSensorListener, AccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            SensorInitialized = true;
        }
    }

    public void uploadData(View view){
        Cursor res = myDB.getAllData();
        if (res.getCount() == 0){
            showMessage("Error", "No data found");
            return;
        } else {
            StringBuffer buffer = new StringBuffer();
            while (res.moveToNext()){
                buffer.append("Timestamp : " + res.getString(0) + "\n");
                buffer.append("X : " + res.getString(1) + "\n");
                buffer.append("Y : " + res.getString(2) + "\n");
                buffer.append("Z : " + res.getString(3) + "\n\n");
            }

            showMessage("Data", buffer.toString());
        }
        res.close();
    }

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void downloadData(View view){
        Toast.makeText(getApplicationContext(), "Data downloaded successfully" , Toast.LENGTH_SHORT).show();
    }

    public void displayGraph(View view) {

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        for (int i = 0; i < values.length; i++)
            values[i] = 0f;
        show_graph=true;
        if (!SensorInitialized){
            if (AccelSensor != null) {
                mySensorManager.registerListener(AccelSensorListener, AccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            SensorInitialized = true;
        }
        //graphView.invalidate();
        //graphView.setValues(values);
    }

    public void clearGraph(View view) {
        for (int i = 0; i < values.length; i++)
            values[i] = 0f;

        show_graph=false;
        graphView.invalidate();
        graphView.setValues(values);

        if (SensorInitialized)
            mySensorManager.unregisterListener(AccelSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        else
            SensorInitialized = false;
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

    void replaceQueue(float a) {
        for (int i = 1; i < values.length; i++)
            values[i - 1] = values[i];
        values[values.length - 1] = a;
    }
}
