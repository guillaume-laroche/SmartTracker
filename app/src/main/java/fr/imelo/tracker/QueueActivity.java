package fr.imelo.tracker;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

import fr.imelo.tracker.util.AndroidUtil;
import fr.imelo.tracker.util.Settings;

public class QueueActivity extends ListActivity {

    public final static String EXTRA_FILENAME = "fr.imelo.tracker.FILENAME";

    ArrayAdapter<String> adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        listFiles();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, FileDetailActivity.class);
        intent.putExtra(EXTRA_FILENAME, (String) getListAdapter().getItem(position));
        startActivity(intent);
    }

    protected void listFiles() {
        //get root directory instance
        File root = Environment.getExternalStorageDirectory();
        File dataDirectory = new File(root.getAbsolutePath() + Settings.getDataDirectory());
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataDirectory.list());
        setListAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            listFiles();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
