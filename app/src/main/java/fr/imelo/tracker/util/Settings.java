package fr.imelo.tracker.util;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import fr.imelo.tracker.R;
import fr.imelo.tracker.StartActivity;

/**
 * Created by gl on 17/04/2015.
 */
public abstract class Settings {

    final static String SETTINGS_FILENAME = "settings.xml";
    final private static String DATA_DIRECTORY = "/SmartTracker";
    final private static String DATA_DIRECTORY_FULL_PATH = "/mnt/sdcard/SmartTracker";

    private static int interval = 0;
    private static String ftpServer;
    private static String ftpLogin;
    private static String ftpPassword;
    private static String ftpDir;
    private static String httpScript;
    private static String httpLogin;
    private static String httpPassword;
    private static String httpAuth;

    private static String loadAuto;
    private static boolean loaded = false;

    public static String getFtpServer() { return ftpServer; }

    public static String getFtpLogin() {
        return ftpLogin;
    }

    public static String getFtpPassword() {
        return ftpPassword;
    }

    public static String getFtpDir() {
        return ftpDir;
    }

    public static int getInterval() {
        return interval;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static String getHttpScript() {
        return httpScript;
    }

    public static String getHttpLogin() {
        return httpLogin;
    }

    public static String getHttpPassword() {
        return httpPassword;
    }

    public static String getHttpAuth() {
        return httpAuth;
    }

    public static String getLoadAuto() {
        return loadAuto;
    }

    public static String getDataDirectory() {
        return DATA_DIRECTORY;
    }

    public static String getDataDirectoryFullPath() {
        return DATA_DIRECTORY_FULL_PATH;
    }

    public static void load(Context context) throws FileNotFoundException, XmlPullParserException, IOException {
        FileInputStream settingsFile = context.openFileInput(SETTINGS_FILENAME);
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(settingsFile, null);
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "settings");
        while(parser.next() != XmlPullParser.END_TAG) {
            String name = parser.getName();

            if(name.equals("interval")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    if(parser.getText() != null)
                        interval = Integer.parseInt(parser.getText());
                    else
                        interval = 0;

                    parser.nextTag();
                }
            }
            else if(name.equals("ftp_server")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    ftpServer = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("ftp_login")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    ftpLogin = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("ftp_password")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    ftpPassword = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("ftp_dir")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    ftpDir = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("http_script")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    httpScript = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("http_auth")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    httpAuth = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("http_login")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    httpLogin = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("http_password")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    httpPassword = parser.getText();
                    parser.nextTag();
                }
            }
            else if(name.equals("load_auto")) {
                if(parser.next() == XmlPullParser.TEXT) {
                    loadAuto = parser.getText();
                    parser.nextTag();
                }
            }
        }
        loaded = true;
    }

    public static void save(Context context, Map settings) throws IOException {
        FileOutputStream file = context.openFileOutput(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        file.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>".getBytes());
        file.write("<settings>".getBytes());
        String temp;

        Iterator it = settings.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry setting = (Map.Entry)it.next();
            temp = "<" + String.valueOf(setting.getKey()) + ">" + String.valueOf(setting.getValue()) + "</" + String.valueOf(setting.getKey()) + ">";
            file.write(temp.getBytes());
            it.remove();
        }
        file.write("</settings>".getBytes());
        file.close();
        loaded = false;
    }

}
