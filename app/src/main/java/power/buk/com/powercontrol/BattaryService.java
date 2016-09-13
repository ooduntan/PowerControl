package power.buk.com.powercontrol;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by OduntanOluwatobiloba on 13/09/16.
 */
public class BattaryService extends Service {

    final Handler handler = new Handler();
    Runnable runnableCode = null;
    BluetoothAdapter bluetoothController;
    private Context mContext;
    private int level;
    private boolean checker = false;



    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothController =  BluetoothAdapter.getDefaultAdapter();
        mContext = getApplicationContext();
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        mContext.registerReceiver(mBroadcastReceiver, iFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Power mode now active", Toast.LENGTH_LONG).show();

        // Define the code block to be executed
         runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                if (level <= 15 && (!checker)) {
                        checker = true;
                        powerMode();
                }

                // Repeat this the same runnable code block again another 2 seconds
                handler.postDelayed(runnableCode, 2000);
            }
        };

    // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);

        return START_STICKY;

//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
                BatteryManager
                    The BatteryManager class contains strings and constants used for values in the
                    ACTION_BATTERY_CHANGED Intent, and provides a method for querying battery
                    and charging properties.
            */
            /*
                public static final String EXTRA_SCALE
                    Extra for ACTION_BATTERY_CHANGED: integer containing the maximum battery level.
                    Constant Value: "scale"
            */
            // Get the battery scale
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);

            /*
                public static final String EXTRA_LEVEL
                    Extra for ACTION_BATTERY_CHANGED: integer field containing the current battery
                    level, from 0 to EXTRA_SCALE.

                    Constant Value: "level"
            */
            // get the battery level
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            // Display the battery level in TextView
        }
    };


    private void powerMode() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            //Toast.makeText(Home.this, counter+" items were Optimized", Toast.LENGTH_LONG).show();
            // TODO Auto-generated method stub
            if(bluetoothController.isEnabled()){
                bluetoothController.disable();
            }
            if(WifiChecker() == true){
                wifiManager.setWifiEnabled(false);
            }

            if(android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
            {
                android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
            }


            if (checkMobileDataStatus()==true) {
                //Boolean name=turnData(false);
                try {
                    setMobileDataEnabled(false);
                }catch (InvocationTargetException es){ Toast.makeText(getApplicationContext(), es.getCause().toString(), Toast.LENGTH_LONG).show();}
                catch (Exception e){ Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();}
            }


            int brightnessmode = 1;

            try {
                brightnessmode = android.provider.Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            } catch (Exception e) {
                Log.d("tag", e.toString());
            }
            if(brightnessmode==0){
                android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }

            if (ContentResolver.getMasterSyncAutomatically()) {
                ContentResolver.setMasterSyncAutomatically(false);
            }

        }

    private Boolean  checkMobileDataStatus(){
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return  mobileDataEnabled;
    }

    public void setMobileDataEnabled(boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ConnectivityManager conman = (ConnectivityManager)  getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class<?> conmanClass = Class.forName(conman.getClass().getName());
        final java.lang.reflect.Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class<?> connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
    }

    public boolean WifiChecker(){
        WifiManager  wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getConnectionInfo().getNetworkId() == -1){
            return false;
        }else{
            return true;
        }
    }
}
