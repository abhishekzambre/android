package me.abhishekz.azhealthmonitor;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    //Declaration of variables used for Layout objects
    TextView textFirst, textLast, textID, textAge;
    RadioGroup radioSex;

    //Declaration of variables used for GraphView
    float[] values = new float[10];
    String[] verlabels = new String[]{"3000", "2500", "2000", "1500", "1000", "500", "0"};
    String[] horlabels = new String[]{"0", "50", "100", "150", "200", "250", "300", "350", "400"};
    GraphView graphView;
    ViewGroup layout;

    //Declaration of variables used for Sensor
    boolean SensorInitialized = false;
    SensorManager mySensorManager;
    Sensor AccelSensor;

    //Declaration of variables used for Database access
    DatabaseHelper myDB;
    String table_name = "";
    boolean dbCreated = false;
    String dbFilePath;

    //Miscellaneous variables
    String upLoadServerUrl = "https://impact.asu.edu/CSE535Fall16Folder/UploadToServer.php";
    InputMethodManager inputManager;
    ProgressDialog dialog = null;
    ProgressDialog mProgressDialog;
    private long lastUpdate = 0;
    boolean show_graph = false;
    private static final String TAG = MainActivity.class.getSimpleName();


    //Accelerometer sensor listener class
    private final SensorEventListener AccelSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                long curTime = System.currentTimeMillis();

                //Achieving sampling frequency of 1Hz.
                if ((curTime - lastUpdate) > 1000) {
                    lastUpdate = curTime;
                    myDB.insertData(table_name, Long.toString(curTime), event.values[0], event.values[1], event.values[2]);
                    if (show_graph) {
                        replaceQueue(event.values[0]);
                    }
                    graphView.invalidate();
                    graphView.setValues(values);
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (ViewGroup) findViewById(R.id.graphLayout);

        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        AccelSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        graphView = new GraphView(this, values, "AZ Health Monitor", horlabels, verlabels, GraphView.LINE);
        graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(graphView);

        myDB = new DatabaseHelper(this);

        verifyStoragePermissions(MainActivity.this);

    }

    //When Save button clicked
    public void createTable(View view) {

        //Hide keyboard when Save button clicked
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        textFirst = (TextView) findViewById(R.id.editText);
        textLast = (TextView) findViewById(R.id.editText2);
        textID = (TextView) findViewById(R.id.editText3);
        textAge = (TextView) findViewById(R.id.editText4);
        radioSex = (RadioGroup) findViewById(R.id.radioSex);
        int index = radioSex.indexOfChild(findViewById(radioSex.getCheckedRadioButtonId()));
        String sex = ((index == 1) ? "Female" : "Male");

        //Input validation checks
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
            table_name = table_name.replaceAll("\\s+", "");

            //Creating table with values as -> FirstName_LastName_ID_Age_Sex
            myDB.createTable(table_name);
            dbCreated = true;

            //Initialize sensor and starts gathering data
            if (!SensorInitialized) {
                if (AccelSensor != null) {
                    mySensorManager.registerListener(AccelSensorListener, AccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                SensorInitialized = true;
            }
            Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    //When Start button clicked
    public void displayGraph(View view) {

        //Hide keyboard when Start button clicked
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        //If Database is created, pulling last 10 seconds values from database and displaying on the graph
        if (dbCreated) {
            show_graph = true;
            if (!SensorInitialized) {
                if (AccelSensor != null) {
                    mySensorManager.registerListener(AccelSensorListener, AccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                SensorInitialized = true;
            }

            Cursor res = myDB.getAllData(table_name);
            int i = 9;
            while (res.moveToNext()) {
                values[i] = res.getLong(1);
                i--;
            }
            res.close();
            graphView.invalidate();
            graphView.setValues(values);
        } else {
            Toast.makeText(getApplicationContext(), "Please save patient data.", Toast.LENGTH_SHORT).show();
        }

    }

    //When Stop button clicked
    public void clearGraph(View view) {

        //Clearing graph and stopping sensor
        for (int i = 0; i < values.length; i++)
            values[i] = 0f;

        show_graph = false;
        graphView.invalidate();
        graphView.setValues(values);

        if (SensorInitialized) {
            mySensorManager.unregisterListener(AccelSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            SensorInitialized = false;
        }
    }

    //When Upload button clicked
    public void uploadData(View view) {

        if (SensorInitialized) {
            mySensorManager.unregisterListener(AccelSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            SensorInitialized = false;
        }

        Context ctx = this;
        String dbname = "AZHealthMonitor.db";
        File dbfile = ctx.getDatabasePath(dbname);
        dbFilePath = dbfile.getAbsolutePath();
        dialog = ProgressDialog.show(this, "", "Uploading file...", true);

        //Method call for upload file
        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {}
                });
                uploadFile(dbFilePath);
            }
        }).start();
    }

    //When Download button clicked
    public void downloadData(View view) {

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        if (SensorInitialized) {
            mySensorManager.unregisterListener(AccelSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            SensorInitialized = false;
        }

        //Downloading database file
        final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
        downloadTask.execute("https://impact.asu.edu/CSE535Fall16Folder/" + "AZHealthMonitor.db", "AZHealthMonitor.db");

        Toast.makeText(getApplicationContext(), "Database downloaded successfully", Toast.LENGTH_SHORT).show();

        //Getting table name and values from database file

        SQLiteDatabase dl_db = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory().getPath() + "/AZHealthMonitor.db", null, 0);

        String last_table = "";
        Cursor res = dl_db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' and name != 'android_metadata' ORDER BY name DESC LIMIT 1", null);
        while (res.moveToNext()) {
            last_table = res.getString(0);
        }
        res.close();

        textFirst = (TextView) findViewById(R.id.editText);
        textLast = (TextView) findViewById(R.id.editText2);
        textID = (TextView) findViewById(R.id.editText3);
        textAge = (TextView) findViewById(R.id.editText4);
        radioSex = (RadioGroup) findViewById(R.id.radioSex);

        String[] record = last_table.split("_");

        //Displaying patient record in Edit views
        textFirst.setText(record[0]);
        textLast.setText(record[1]);
        textID.setText(record[2]);
        textAge.setText(record[3]);

        if (record[4].equals("Male")) {
            radioSex.check(R.id.radioMale);
        } else {
            radioSex.check(R.id.radioFemale);
        }

        //Displaying last 10 secods from database on the graph
        res = dl_db.rawQuery("SELECT * FROM " + last_table + " ORDER BY TIMESTAMP DESC LIMIT 10", null);
        int i = 9;
        while (res.moveToNext()) {
            values[i] = res.getLong(1);
            i--;
        }
        res.close();
        graphView.invalidate();
        graphView.setValues(values);

        dl_db.close();

    }

    //For About menu
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //For About menu
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Shift values of array to the left
    void replaceQueue(float a) {
        for (int i = 1; i < values.length; i++)
            values[i - 1] = values[i];
        values[values.length - 1] = a;
    }

    //Method which uploads file to server
    public int uploadFile(String selectedFilePath) {

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {
            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //tvFileName.setText("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(upLoadServerUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tvFileName.setText("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/"+ fileName);
                            Toast.makeText(MainActivity.this, "File Uploaded Complete.\n\nFile Location : https://impact.asu.edu/CSE535Fall16Folder/AZHealthMonitor.db", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            return serverResponseCode;
        }

    }

    //To download file from the server
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            //searchButton = (Button) findViewById(R.id.button1);
            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection connection = null;
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            }};

            try {
                SSLContext sc = SSLContext.getInstance("TLS");

                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpsURLConnection) url.openConnection();

                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                //downloadButton.setText(Integer.toString(fileLength));
                // download the file
                input = connection.getInputStream();

                output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + sUrl[1]);

                //downloadButton.setText("Connecting .....");
                byte data[] = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;


        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();


            }
            //else{
            //Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();

            //}
        }

    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
