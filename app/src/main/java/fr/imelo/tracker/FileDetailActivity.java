package fr.imelo.tracker;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.imelo.tracker.net.AsyncFileTransfer;
import fr.imelo.tracker.net.FileTransfer;
import fr.imelo.tracker.net.NetException;
import fr.imelo.tracker.util.AndroidUtil;
import fr.imelo.tracker.util.Settings;

@TargetApi(19)
public class FileDetailActivity extends Activity {

    private File file;
    private BufferedReader bufferedReader;
    private TextView filename, fileSize, fileDate, fileContent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            if(file != null) {
                file.delete();
                AndroidUtil.toast(this, getResources().getString(R.string.file_delete_ok));
            }
                return true;
        }
        else if (id == R.id.action_upload) {
            if(AndroidUtil.isNetworkAvailable(FileDetailActivity.this)) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                new AsyncFileTransfer(FileDetailActivity.this).execute(Settings.getFtpServer(),
                                                                       Settings.getFtpLogin(),
                                                                       Settings.getFtpPassword(),
                                                                       getIntent().getStringExtra(QueueActivity.EXTRA_FILENAME),
                                                                       Settings.getDataDirectoryFullPath(),
                                                                       Settings.getFtpDir(),
                                                                       "false",
                                                                       Settings.getHttpScript(),
                                                                       Settings.getHttpAuth(),
                                                                       Settings.getHttpLogin(),
                                                                       Settings.getHttpPassword(),
                                                                       Settings.getLoadAuto());
                finish();
            }
            else {
                AndroidUtil.longToast(FileDetailActivity.this, getResources().getString(R.string.network_not_available));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_detail);

        filename = (TextView) findViewById(R.id.file_detail_name);
        fileSize = (TextView) findViewById(R.id.file_detail_size);
        fileDate = (TextView) findViewById(R.id.file_detail_date);
        fileContent = (TextView) findViewById(R.id.file_content);
        fileContent.setMovementMethod(new ScrollingMovementMethod());

        File root = Environment.getExternalStorageDirectory();
        file = new File(root.getAbsolutePath() + Settings.getDataDirectory() + "/" + getIntent().getStringExtra(QueueActivity.EXTRA_FILENAME));

        filename.setText(getIntent().getStringExtra(QueueActivity.EXTRA_FILENAME));

        if(file.length() < 1000)
            fileSize.setText(getResources().getString(R.string.label_size) + " : " + String.valueOf(file.length()) + " o");
        else
            fileSize.setText(getResources().getString(R.string.label_size) + " : " + String.valueOf(file.length() / 1000) + " ko");

        fileDate.setText(getResources().getString(R.string.label_date) + " : " + new SimpleDateFormat("dd-MM-yyyy kk:mm:ss").format(new Date(file.lastModified())));

        if(file.length() > 0) {
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    fileContent.append(line);
                    fileContent.append(System.lineSeparator());
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                AndroidUtil.toast(this, e.getMessage());
            }
        }
    }
}
