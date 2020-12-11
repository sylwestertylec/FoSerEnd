package com.example.foser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class MyForegroundService extends Service {

    //1. Kanał notyfikacji
    //1. Do  obsługi Notification
    public static final String CHANNEL_ID = "MyForegroundServiceChannel";
    public static final String CHANNEL_NAME = "FoSer service channel";

    //2. Odczyt danych zapisanych w Intent
    //2. Klucze od wyjmowania danych z intencji, która uruchamia Service
    public static final String MESSAGE = "message";
    public static final String TIME = "time";
    public static final String WORK = "work";
    public static final String WORK_DOUBLE = "work_double";

    //3. Wartości ustawień
    //3. Zmienne do których będą wpisane wartości ustawień
    private String message;
    private Boolean show_time, do_work, double_speed;

    //4
    private Context ctx;
    private Intent notificationIntent;
    private PendingIntent pendingIntent;

    //Stała ta określa co ile milisekund będzie wykonywana praca cykliczna.
    private final long period = 2000; //2s

    //5 Handler umożliwia komunikacje między wątkami.
    private int counter;
    private Timer timer;
    private TimerTask timerTask;

    final Handler handler = new Handler();

    //Runnable pozwala utworzyć obiekt, który będzie wykonany przez inny wątek.
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            //Metoda run uaktualni wyświetlaną notyfikację.

            Notification notification = new Notification.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_my_icon)
                    .setContentTitle(getString(R.string.ser_title))
                    .setShowWhen(show_time)
                    .setContentText(message+" "+String.valueOf(counter))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.circle))
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(1, notification);

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        ctx = this;
        notificationIntent = new Intent(ctx, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        //wyzeruj licznik
        counter = 0;

        //utwórz obiekt timer-a
        timer = new Timer();

        //utwórz zadanie timera (TimerTask).
        timerTask = new TimerTask() {
            @Override
            public void run() {

                //zwiększa licznik o 1 i deleguje zadanie do wątku głównego.
                counter++;
                handler.post(runnable);

            }
        };

    }

    @Override
    public void onDestroy() {

        //zatrzymywała wątek timer-a i umożliwiała jego usuniecie.
        handler.removeCallbacks(runnable);
        timer.cancel();
        timer.purge();
        timer = null;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //tworzy notification
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        message = intent.getStringExtra(MESSAGE);
        show_time = intent.getBooleanExtra(TIME,false);
        do_work = intent.getBooleanExtra(WORK,false);
        double_speed = intent.getBooleanExtra(WORK_DOUBLE,false);

        createNotificationChannel();

        //Intent notificationIntent = new Intent(this, MainActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_my_icon)
                .setContentTitle(getString(R.string.ser_title))
                .setShowWhen(show_time)
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.circle))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        doWork();

        return START_NOT_STICKY;
    }

    private void doWork(){

//        try {
//            Thread.sleep(5000);
//        }
//        catch (Exception e){
//            //
//        }
//
//        String info = "Start working..."
//                +"\n show_time=" + show_time.toString()
//                +"\n do_work=" + do_work.toString()
//                +"\n double_speed=" + double_speed.toString();
//
//        Toast.makeText(this, info, Toast.LENGTH_LONG).show();

        if(do_work){
            timer.schedule(timerTask, 0L, double_speed ? period / 2L : period);
        }

    }

    //metoda tworzy kanał dla Notification:
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(){

        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

    }

}
