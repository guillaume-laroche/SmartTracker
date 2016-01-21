package fr.imelo.tracker.net;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.imelo.tracker.R;

/**
 * Created by gl on 08/01/2016.
 */
public abstract class FileTransfer {

    public static void upload(Context context, String server, String login, String password, String filename, String localDir, String remoteDir, String deleteFile) throws NetException {

        FTPClient ftpClient;
        int replyCode = 0;

        if(server == null || login == null || password == null || filename == null || localDir == null || remoteDir == null)
            throw new NetException(context.getResources().getString(R.string.bad_ftp_parameters));

        ftpClient = new FTPClient();
        try {
            ftpClient.connect(server);
            replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                throw new NetException(context.getResources().getString(R.string.ftp_connection_refused));
            }

            if (!ftpClient.login(login, password)) {
                ftpClient.disconnect();
                throw new NetException(context.getResources().getString(R.string.ftp_bad_credentials));
            }

            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);

            ftpClient.changeWorkingDirectory(remoteDir);

            InputStream inputFile;
            inputFile = new FileInputStream(localDir + "/" + filename);
            ftpClient.storeFile(filename, inputFile);
            inputFile.close();

            ftpClient.noop();
            ftpClient.logout();
            ftpClient.disconnect();

            //Delete local file if required
            if(deleteFile.equals("true")) {
                File f = new File(localDir + "/" + filename);
                if(f.exists())
                    f.delete();
            }
        }
        catch (IOException e) {
            throw new NetException(e);
        }
    }

    protected static int load(String filename, String script, String auth, String login, String password) throws NetException{
        InputStream is = null;
        try {
            URL url = new URL(script + "?filename=" + filename);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("GET");
            if(auth.equals("true")) {
                String authString = login + ":" + password;
                conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP));
            }
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();

            //Parse JSON response
            JSONObject response = new JSONObject(parseString(is));
            if(response.getString("result").equals("NOK"))
                throw new NetException(response.getString("message"));
            else
                return Integer.parseInt(response.getString("id"));
        }
        catch(Exception e) {
            throw new NetException(e);
        }
    }

    private static String parseString(InputStream stream) throws UnsupportedEncodingException, IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[500];
        reader.read(buffer);
        return new String(buffer);
    }

}
