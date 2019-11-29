package com.neuricius.masterproject.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.neuricius.masterproject.R;
import com.neuricius.masterproject.async.SimpleReceiver;
import com.neuricius.masterproject.async.SimpleService;
import com.neuricius.masterproject.dialog.AboutDialog;
import com.neuricius.masterproject.net.model.Cast;
import com.neuricius.masterproject.net.model.Credit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

public class UtilTools {

    private static AlertDialog dialog;
    private static SharedPreferences sharedPreferences;

    private SimpleReceiver sync;
    private PendingIntent pintent;
    private AlarmManager alarm;

    public static final String NOTIFY_TOAST = "notifyByToast";
    public static final String NOTIFY_STATUS = "notifyByStatusBar";

    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectionType(Integer type){
        switch (type){
            case 1:
                return "WIFI";
            case 2:
                return "Mobilni internet";
            default:
                return "";
        }
    }

    public static int calculateTimeTillNextSync(int minutes){
        return 1000 * 60 * minutes;
    }

    public static String returnGender(Integer genderID) {
        switch (genderID) {
            case 1:
                return "Female";
            case 2:
                return "Male";
            default:
                return "N/A";
        }
    }

    public static void showAboutDialog(Context context){
        if (dialog == null){
            dialog = new AboutDialog(context).prepareDialog();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        dialog.show();
    }

    public static void sharedPrefNotify(Activity activity, String msg){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        boolean toast = sharedPreferences.getBoolean(NOTIFY_TOAST, false);
        boolean status = sharedPreferences.getBoolean(NOTIFY_STATUS, false);

        if (toast){
            showToast(activity, msg);
        }

        if (status){
            showStatusMesage(activity, msg);
        }
    }

    public static void showToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showStatusMesage(Activity activity, String message){
        NotificationManager mNotificationManager = (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity);
        mBuilder.setSmallIcon(R.drawable.ic_pic_error_foreground);
        mBuilder.setContentTitle("Master Project");
        mBuilder.setContentText(message);

        Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_pic_error_foreground);

        mBuilder.setLargeIcon(bm);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    /**Rad sa teksutalnim fajlovima u android-u**/
    public static void writeToFile(String data, Context context, String filename) {
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFromFile(Context context, String file) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public static List<Cast> getCastfromJson(String file) {
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            Credit result = gson.fromJson(br, Credit.class);
            if (result != null) {
                return result.getCast();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean isFileExists(Context context, String filename){
        File file = new File(context.getFilesDir().getAbsolutePath() + "/" + filename);
        if(file.exists()){
            return true;
        }else{
            return false;
        }
    }
    /*******/

    /**Rad sa servisima**/
    private void setUpSimpleReceiver(Activity activity){
        sync = new SimpleReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("SYNC_DATA");
        activity.registerReceiver(sync, filter);

        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(activity);


//        consultPreferences();
        setUpManager(activity);
    }

    private void setUpManager(Activity activity){
        Intent intent = new Intent(activity, SimpleService.class);
        int status = getConnectivityStatus(activity);
        intent.putExtra("STATUS", status);

        //allowSync i synctime su id opcija iz SharedPref
        //allowsync boolean da li je dozvoljena autosinhronizacija
        //synctime je String koji oznacava interval u minutima
//        if (allowSync) {
            pintent = PendingIntent.getService(activity, 0, intent, 0);
            alarm = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    calculateTimeTillNextSync(Integer.parseInt("1")),
                    pintent);

            sharedPrefNotify(activity, "Alarm Set");
//        }
    }

    public void killServices(Activity activity){
        if (alarm != null) {
            alarm.cancel(pintent);
            alarm = null;
        }


        if(sync != null){
            activity.unregisterReceiver(sync);
            sync = null;
        }
    }

    /**ukoliko smo koristili servise i alarm manager
     * neophodno je osloboditi resurse prilikom odlaska
     * aktivnosti u drugi plan. Ovaj kod ide u aktivnost
     * koja je zvala servis/alarm mngr
     **/
//    @Override
//    protected void onPause() {
//        if (manager != null) {
//            manager.cancel(pendingIntent);
//            manager = null;
//        }
//
//
//        if(sync != null){
//            unregisterReceiver(sync);
//            sync = null;
//        }
//
//        super.onPause();
//
//    }
    /*******/
}
