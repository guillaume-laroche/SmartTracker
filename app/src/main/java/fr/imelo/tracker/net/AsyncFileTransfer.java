package fr.imelo.tracker.net;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import fr.imelo.tracker.R;
import fr.imelo.tracker.util.AndroidUtil;

/**
 * Created by gl on 14/04/2015.
 */
public class AsyncFileTransfer extends AsyncTask<String, Void, Boolean> {

    Context context;
    String message;
    int id;
    boolean error;

    public AsyncFileTransfer(Context context) {
        this.context = context;
        this.message = null;
        this.error = false;
    }

    protected Boolean doInBackground(String... params) {
        try {
            FileTransfer.upload(context, params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
            if(params[11].equals("true")) {
                id = FileTransfer.load(params[3], params[7], params[8], params[9], params[10]);
                message = context.getResources().getString(R.string.ftp_upload_ok) + " (" + id + ")";
            }
            else {
                message = context.getResources().getString(R.string.ftp_upload_ok);
            }
            return true;
        }
        catch(NetException e) {
            message = e.getMessage();
            error = true;
            return false;
        }
    }

    protected void onPostExecute(Boolean result) {
        AndroidUtil.notify(context, context.getResources().getString(R.string.app_name), message, R.drawable.ic_action_upload, true);
    }
}
