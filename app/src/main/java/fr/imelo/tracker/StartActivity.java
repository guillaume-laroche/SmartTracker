package fr.imelo.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;

import fr.imelo.tracker.util.AndroidUtil;
import fr.imelo.tracker.util.Settings;

public class StartActivity extends Activity {

    final static String TRACKING_MTB = "MTB";
    final static String TRACKING_TREKKING = "TREKKING";
    final static String TRACKING_RUNNING = "RUNNING";

    public final static String EXTRA_TRACKING = "fr.imelo.tracker.TRACKING_TYPE";
    public final static String EXTRA_USERNAME = "fr.imelo.tracker.USERNAME";

    Button startButton;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        CharSequence[] googleAccounts = AndroidUtil.getGoogleAccounts(this);

        if(googleAccounts.length > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.choose_google_account));
            builder.setSingleChoiceItems(googleAccounts, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
        }
        else {
            username = String.valueOf(googleAccounts[0]);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!Settings.isLoaded()) {
            try {
                Settings.load(this);
            }
            catch(FileNotFoundException e) {
                //Settings file doesn't exists, no message to display but prevent user from launching tracking by disabling button
            }
            catch(Exception e) {
                AndroidUtil.longToast(this, e.getMessage());
            }
        }

        //Interval undefined in settings, unable to launch tracking
        startButton = (Button) findViewById(R.id.button_start);
        if(Settings.getInterval() == 0)
            startButton.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_exit) {
            AlertDialog alertExit = new AlertDialog.Builder(this)
                                            .setMessage(R.string.confirm_exit_message)
                                            .setNegativeButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                }
                                            })
                                            .setPositiveButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .create();
            alertExit.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void confirmTracking(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.choose_tracking);
        builder.setPositiveButton(R.string.tracking_mtb, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callTracking(TRACKING_MTB);
            }
        });

        builder.setNegativeButton(R.string.tracking_running, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callTracking(TRACKING_RUNNING);
            }
        });

        builder.setNeutralButton(R.string.tracking_trekking, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callTracking(TRACKING_TREKKING);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void callTracking(String trackingType) {
        Intent intent = new Intent(this, TrackActivity.class);
        intent.putExtra(EXTRA_TRACKING, trackingType);
        if(username != null)
            intent.putExtra(EXTRA_USERNAME, username);
        startActivity(intent);
    }

    public void callSettings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void callFileQueue(View v) {
        Intent intent = new Intent(this, QueueActivity.class);
        startActivity(intent);
    }

    public void test(View v) {

    }


}
