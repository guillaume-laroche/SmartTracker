package fr.imelo.tracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import fr.imelo.tracker.util.AndroidUtil;
import fr.imelo.tracker.util.Settings;


public class SettingsActivity extends Activity implements CompoundButton.OnCheckedChangeListener  {

    final static String SETTINGS_FILENAME = "settings.xml";

    TextView interval, ftpServer, ftpLogin, ftpPassword, ftpDir, httpScript, httpLogin, httpPassword;
    Switch httpAuth, loadAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        interval = (TextView) findViewById(R.id.setting_interval);
        ftpServer = (TextView) findViewById(R.id.setting_ftp_server);
        ftpLogin = (TextView) findViewById(R.id.setting_ftp_login);
        ftpPassword = (TextView) findViewById(R.id.setting_ftp_password);
        ftpDir = (TextView) findViewById(R.id.setting_ftp_dir);
        httpScript = (TextView) findViewById(R.id.setting_script_integration);
        httpLogin = (TextView) findViewById(R.id.setting_http_login);
        httpPassword = (TextView) findViewById(R.id.setting_http_password);
        httpAuth = (Switch) findViewById(R.id.setting_http_auth);
        httpAuth = (Switch) findViewById(R.id.setting_http_auth);
        loadAuto = (Switch) findViewById(R.id.setting_auto_load);

        httpAuth.setOnCheckedChangeListener(this);

        if(Settings.isLoaded()) {
            interval.setText(String.valueOf(Settings.getInterval()));
            ftpServer.setText(Settings.getFtpServer());
            ftpLogin.setText(Settings.getFtpLogin());
            ftpPassword.setText(Settings.getFtpPassword());
            ftpDir.setText(Settings.getFtpDir());
            httpScript.setText(Settings.getHttpScript());
            httpLogin.setText(Settings.getHttpLogin());
            httpPassword.setText(Settings.getHttpPassword());
            httpAuth.setChecked(Boolean.valueOf(Settings.getHttpAuth()));

            httpLogin.setEnabled(httpAuth.isChecked());
            httpPassword.setEnabled(httpAuth.isChecked());

            loadAuto.setChecked(Boolean.valueOf(Settings.getLoadAuto()));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void cancelSettings(View v) {
        finish();
    }

    public void saveSettings(View v) {

        Map<String, String> settings = new HashMap<String, String>();
        settings.put("interval", String.valueOf(interval.getText()));
        settings.put("ftp_server", ftpServer.getText().toString());
        settings.put("ftp_login", ftpLogin.getText().toString());
        settings.put("ftp_password", ftpPassword.getText().toString());
        settings.put("ftp_dir", ftpDir.getText().toString());
        settings.put("http_script", httpScript.getText().toString());
        settings.put("http_auth", String.valueOf(httpAuth.isChecked()));
        settings.put("http_login", httpLogin.getText().toString());
        settings.put("http_password", httpPassword.getText().toString());
        settings.put("load_auto", String.valueOf(loadAuto.isChecked()));

        try {
            Settings.save(this, settings);
            AndroidUtil.toast(this, getResources().getString(R.string.settings_saved));
            finish();
        }
        catch(Exception e) {
            AndroidUtil.longToast(this, e.getMessage());
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.setting_http_auth) {
            httpLogin.setEnabled(isChecked);
            httpPassword.setEnabled(isChecked);
        }
    }
}
