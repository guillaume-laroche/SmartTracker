package fr.imelo.tracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import fr.imelo.tracker.net.AsyncFileTransfer;
import fr.imelo.tracker.util.AndroidUtil;
import fr.imelo.tracker.util.DateUtil;
import fr.imelo.tracker.util.Settings;

public class TrackActivity extends Activity implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final int MAX_LOG_SIZE = 15; //Max number of messages to store in memory

    static final int MAX_ACCURACY = 50; //Max accuracy allowed in meters, otherwise discard location

    static final int EARTH_RADIUS = 6378;

    //Define tracking state : no tracking, tracking in progress, pause tracking
    static final int STATE_NO_TRACKING = 0;
    static final int STATE_TRACKING = 1;
    static final int STATE_PAUSE_TRACKING = 2;

    //Bundle final variables
    static final String CHRONO_STATE = "chronoState";
    static final String CHRONO_PAUSE_STATE = "chronoPauseState";
    static final String TRACKING_STATE = "trackingState";
    static final String LOG_MESSAGES = "logMessages";
    static final String PREVIOUS_LATITUDE = "previousLatitude";
    static final String PREVIOUS_LONGITUDE = "previousLongitude";

    final static String DATA_FILENAME = "tracking";

    Chronometer chronometer;
    TextView log;
    Button start;
    Button pause;
    Button finish;
    Long chronoState;
    int tracking;
    int interval;
    double previousLatitude;
    double previousLongitude;
    //float[] distance;
    double distance;
    GoogleApiClient googleApiClient;
    LocationManager locationManager;
    LocationRequest locationRequest;
    String filename = DATA_FILENAME + "_" + DateUtil.getCurrentDateTimeNoSpace() + ".json";
    String trackingType;
    String username;

    JSONObject jsonLocation;
    JSONObject jsonEvent;

    ArrayList<String> messages = new ArrayList<String>();

    File logfile, dataDirectory;
    BufferedWriter writer;


    private void displayMessage(String message) {
        log.setText(message + log.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        trackingType = getIntent().getStringExtra(StartActivity.EXTRA_TRACKING);
        username = getIntent().getStringExtra(StartActivity.EXTRA_USERNAME);

        //Get java object instances for layout components
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        log = (TextView) findViewById(R.id.log_area);
        start = (Button) findViewById(R.id.button_start);
        pause = (Button) findViewById(R.id.button_pause);
        finish = (Button) findViewById(R.id.button_finish);

        //Add listener on buttons
        start.setOnClickListener(buttonListener);
        pause.setOnClickListener(buttonListener);
        finish.setOnClickListener(buttonListener);

        interval = Settings.getInterval();

        jsonLocation = new JSONObject();
        jsonEvent = new JSONObject();

        previousLatitude = 0;
        previousLongitude = 0;
        //distance = new float[3];

        try {
            jsonLocation.put("type", "location");
            jsonEvent.put("type", "event");
        } catch (JSONException e) {
            AndroidUtil.toast(this, e.getMessage());
        }

        tracking = STATE_NO_TRACKING;
        pause.setEnabled(false);
        finish.setEnabled(false);

        //Files are written on SDCard, we first check we have I/O permissions
        if (!AndroidUtil.isExternalStorageReadable())
            AndroidUtil.toast(this, getResources().getString(R.string.sdcard_not_readable));
        if (!AndroidUtil.isExternalStorageWritable())
            AndroidUtil.toast(this, getResources().getString(R.string.sdcard_not_writable));

        //get root directory instance
        File root = Environment.getExternalStorageDirectory();
        //if data directory doesn't exists, create it
        dataDirectory = new File(root.getAbsolutePath() + Settings.getDataDirectory());
        if (!dataDirectory.exists())
            dataDirectory.mkdirs();

        logfile = new File(dataDirectory, filename);
        try {
            writer = new BufferedWriter(new FileWriter(logfile));
        } catch (IOException e) {
            AndroidUtil.toast(this, e.getMessage());
        }

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //Instantiate Google API Client, in order to access location service
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (savedInstanceState != null) {
            //Restore state for components saved within Bundle
            chronometer.setBase(savedInstanceState.getLong(CHRONO_STATE));
            tracking = savedInstanceState.getInt(TRACKING_STATE);
            messages = savedInstanceState.getStringArrayList(LOG_MESSAGES);
            previousLatitude = savedInstanceState.getDouble(PREVIOUS_LATITUDE);
            previousLongitude = savedInstanceState.getDouble(PREVIOUS_LONGITUDE);
            updateUI();

            if (tracking == STATE_TRACKING) {
                start.setEnabled(false);
                pause.setEnabled(true);
                finish.setEnabled(true);
                chronometer.start();
                googleApiClient.connect();
            } else if (tracking == STATE_PAUSE_TRACKING) {
                start.setEnabled(false);
                pause.setText(R.string.action_resume);
                chronoState = savedInstanceState.getLong(CHRONO_PAUSE_STATE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Save current state for Chronometer, tracking and log
        savedInstanceState.putLong(CHRONO_STATE, chronometer.getBase());
        savedInstanceState.putInt(TRACKING_STATE, tracking);
        savedInstanceState.putStringArrayList(LOG_MESSAGES, messages);
        savedInstanceState.putDouble(PREVIOUS_LATITUDE, previousLatitude);
        savedInstanceState.putDouble(PREVIOUS_LONGITUDE, previousLongitude);
        if (chronoState != null)
            savedInstanceState.putLong(CHRONO_PAUSE_STATE, chronoState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);

        //Hide back button, force to use cancel button to get back
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_cancel) {
            AlertDialog alertCancel = new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_cancel_message)
                    .setNegativeButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(googleApiClient.isConnected()) {
                                chronometer.stop();
                                googleApiClient.disconnect();
                                tracking = STATE_NO_TRACKING;
                                try {
                                    writer.close();
                                }
                                catch (Exception e) {
                                }
                            }
                            if(logfile != null) {
                                logfile.delete();
                            }
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            alertCancel.show();
        }
        else if (id == R.id.action_map) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        addMessage("Connected");
        locationRequest = new LocationRequest()
                .setInterval(interval)
                .setFastestInterval(interval)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private double radian(double value) {
        return (Math.PI / 180) * value;
    }

    private double getDistance(double sourceLat, double sourceLong, double destLat, double destLong) {
        if((sourceLat == destLat) && (sourceLong == destLong))
            return 0;
        else {
            return EARTH_RADIUS * (Math.PI / 2 - Math.asin(Math.sin(radian(destLat)) * Math.sin(radian(sourceLat))
                    + Math.cos(radian(destLong) - radian(sourceLong)) * Math.cos(radian(destLat)) * Math.cos(radian(sourceLat))));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        int relevant = 1;
        addMessage(DateUtil.getCurrentTime() + " - Location : " + location.getLatitude() + ", " + location.getLongitude());
        //addMessage("Provider = " + location.getProvider());
        try {
            jsonLocation.put("timestamp", DateUtil.getCurrentDateTime());
            jsonLocation.put("latitude", location.getLatitude());
            jsonLocation.put("longitude", location.getLongitude());
            if (location.hasAccuracy()) {
                jsonLocation.put("accuracy", location.getAccuracy());
                if(location.getAccuracy() > MAX_ACCURACY)
                    relevant = 0;
            }
            if (location.hasAltitude())
                jsonLocation.put("altitude", location.getAltitude());
            if (location.hasSpeed())
                jsonLocation.put("speed", location.getSpeed() * 3.6); //Location default in meter/second, conversion to km/h

            if(previousLatitude != 0 && previousLongitude != 0) {
                try {
                    //Self computation more accurate for distance, strange results with API computation
                    //Location.distanceBetween(previousLatitude, previousLongitude, location.getLatitude(), location.getLongitude(), distance);
                    //jsonLocation.put("distance", distance[0]);

                    distance = getDistance(previousLatitude, previousLongitude, location.getLatitude(), location.getLongitude());
                    jsonLocation.put("distance", distance);
                }
                catch(IllegalArgumentException e) {
                }
            }
            jsonLocation.put("relevant", relevant);

            previousLatitude = location.getLatitude();
            previousLongitude = location.getLongitude();

            writer.write("," + jsonLocation.toString(1));

        } catch (Exception e) {
            AndroidUtil.toast(this, e.getMessage());
        }
        updateUI();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("GOOGLE_API", "Connection failed");
        Log.v("GOOGLE_API", connectionResult.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("GOOGLE_API", "Connection suspended");
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_start:
                    startTracking();
                    break;
                case R.id.button_pause:
                    pauseTracking();
                    break;
                case R.id.button_finish:
                    finishTracking();
                    break;
            }
        }
    };

    private void addMessage(String message) {
        if (messages.size() < MAX_LOG_SIZE) {
            if (messages.isEmpty())
                messages.add(0, message);
            else
                messages.add(messages.size(), messages.get(messages.size() - 1));
        }

        for (int i = messages.size() - 1; i >= 1; i--) {
            messages.set(i, messages.get(i - 1));
        }
        messages.set(0, message + "\n");
    }

    private void addMessageAndUpdateUI(String message) {
        addMessage(message);
        updateUI();
    }

    private void updateUI() {
        log.setText(null);
        for (int i = 0; i <= messages.size() - 1; i++) {
            log.append(messages.get(i));
        }
    }

    private void logEvent(String event, String tracking) {
        try {
            jsonEvent.put("what", event);
            jsonEvent.put("timestamp", DateUtil.getCurrentDateTime());
            if(event.equals("start") && tracking != null) {
                jsonEvent.put("tracking", tracking);
                jsonEvent.put("username", username);
            }
            if (!event.equals("start")) {
                writer.newLine();
                writer.write("," + jsonEvent.toString(1));
            } else {
                writer.write("[");
                writer.write(jsonEvent.toString(1));
            }

            if (event.equals("finish"))
                writer.write("]");
        } catch (Exception e) {
            AndroidUtil.toast(this, e.getMessage());
        }
    }

    public void startTracking() {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AndroidUtil.longToast(this, getResources().getString(R.string.gps_not_available));
        }
        else if(!AndroidUtil.isNetworkAvailable(this)) {
            AndroidUtil.longToast(this, getResources().getString(R.string.network_not_available));
        }
        else {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            start.setEnabled(false);
            pause.setEnabled(true);
            finish.setEnabled(true);
            tracking = STATE_TRACKING;
            googleApiClient.connect();
            logEvent("start", trackingType);
            addMessageAndUpdateUI(DateUtil.getCurrentDateTime() + " - " + getResources().getString(R.string.action_start));
        }
    }

    public void pauseTracking() {
        if (tracking == STATE_TRACKING) {
            //Pause tracking
            // Stop chronometer, change state and disconnect Google API Client
            chronoState = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
            pause.setText(R.string.action_resume);
            tracking = STATE_PAUSE_TRACKING;
            googleApiClient.disconnect();
            logEvent("pause", null);
            addMessageAndUpdateUI(DateUtil.getCurrentTime() + " - " + getResources().getString(R.string.action_pause));

        } else {
            //Resume tracking
            // Restart chronometer, change state and reconnect Google API Client
            chronometer.setBase(SystemClock.elapsedRealtime() + chronoState);
            chronometer.start();
            chronoState = null;
            pause.setText(R.string.action_pause);
            tracking = STATE_TRACKING;
            googleApiClient.connect();
            logEvent("resume", null);
            addMessageAndUpdateUI(DateUtil.getCurrentTime() + " - " + getResources().getString(R.string.action_resume));
        }
    }

    public void finishTracking() {
        AlertDialog alertFinish = new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_finish_message)
                .setNegativeButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chronometer.stop();
                        googleApiClient.disconnect();
                        tracking = STATE_NO_TRACKING;
                        addMessageAndUpdateUI(DateUtil.getCurrentDateTime() + " - " + getResources().getString(R.string.action_finish));
                        try {
                            logEvent("finish", null);
                            writer.close();
                        } catch (Exception e) {
                            AndroidUtil.toast(TrackActivity.this, e.getMessage());
                        }
                        if(AndroidUtil.isNetworkAvailable(TrackActivity.this)) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            new AsyncFileTransfer(TrackActivity.this).execute(Settings.getFtpServer(),
                                                                        Settings.getFtpLogin(),
                                                                        Settings.getFtpPassword(),
                                                                        filename,
                                                                        Settings.getDataDirectoryFullPath(),
                                                                        Settings.getFtpDir(),
                                                                        "true",
                                                                        Settings.getHttpScript(),
                                                                        Settings.getHttpAuth(),
                                                                        Settings.getHttpLogin(),
                                                                        Settings.getHttpPassword(),
                                                                        Settings.getLoadAuto());
                            finish();
                        }
                        else {
                            AndroidUtil.longToast(TrackActivity.this, getResources().getString(R.string.network_not_available));
                        }

                    }
                })
                .setPositiveButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        alertFinish.show();

    }
}
