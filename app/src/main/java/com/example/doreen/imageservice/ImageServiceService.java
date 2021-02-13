package com.example.doreen.imageservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


/***
 * ImageServiceService Class.
 */
public class ImageServiceService extends Service {
    public ImageServiceService(){}
    private BroadcastReceiver yourReceiver;
    Tcp tcp = new Tcp("10.0.2.2",8000);

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    /***
     * Activated on create and creates a new BroadcastReceiver. When the WIFI is connected it calls the startTransfer function.
     */
    @Override
    public void onCreate(){
        super.onCreate();

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.yourReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent){
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI){

                        //get the different network states
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED){
                            startTransfer(context); // Starting the Transfer
                        }
                    }
                }
            }
        };
        // Registers the receiver so that your service will listen for broadcasts
        this.registerReceiver(this.yourReceiver, theFilter);

    }

    /***
     * The Start button was clicked. We write an appropriate Toast.
     * @param intent
     * @param flag
     * @param startId
     * @return START_STICKY to keep the service going.
     */
    public int onStartCommand(Intent intent, int flag, int startId){
        Toast.makeText(this,"Service starting...", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    /***
     * The Stop button was clicked. We write an appropriate Toast and unregister the BroadcastReceiver.
     */
    public void onDestroy(){
        Toast.makeText(this,"Service ending...",Toast.LENGTH_SHORT).show();
        // unRegisters the receiver
        unregisterReceiver(this.yourReceiver);
    }

    /***
     * Initializes the Scroll bar and calls the tcp run function.
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTransfer(Context context){
        Toast.makeText(this,"WIFI WORKS NOW...",Toast.LENGTH_SHORT).show();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        final NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", "Progress bar", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Progress bar for image transfer");
        NM.createNotificationChannel(channel);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        //Progress bar title.
        builder.setContentTitle("Picture Transfer");
        builder.setContentText("Transfer in progress");
        //Give tcp client nm and builder so it will use it will transferring.
        this.tcp.setNM(NM);
        this.tcp.setBuilder(builder);

        Thread thread = new Thread(this.tcp);
        //Start run function of tcpclient.
        thread.start();
    }

}