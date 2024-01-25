package com.example.cronometro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;

public class ChronometerService extends Service {
    private final IBinder binder = new LocalBinder();
    private NotificationManager notificationManager;
    private boolean isRunning;
    private long startTime, elapsedTime = 0L;
    private Handler handler = new Handler();
    private Runnable runnable;
    private long pauseTime = 0L;

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("ingreso a Bind");
        return binder;
    }

    public class LocalBinder extends Binder {
        ChronometerService getService() {
            return ChronometerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            startChronometer();

            // Crear notificación
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            Intent notificationIntent;
            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                notificationIntent = new Intent(this, MainActivity.class);
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                notificationIntent = new Intent(this, MainActivity.class);
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            }
            Notification notification = new NotificationCompat.Builder(this, "channel_01")
                    .setContentTitle("Cronómetro en ejecución")
                    .setContentText("Haz clic para volver a la aplicación.")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ingreso a Destructor");
        isRunning = false;
        stopChronometer();
    }
    public void startChronometer() {
        startTime = System.currentTimeMillis();
        runnable = new Runnable() {
            public void run() {
                elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("en el startChronometer");
                // Formatea elapsedTime a un formato de cronómetro
                String time = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(elapsedTime),
                        TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % TimeUnit.MINUTES.toSeconds(1));
                // Enviar un broadcast con el tiempo transcurrido
                Intent intent = new Intent("com.example.cronometro.UPDATE_TIMER");
                intent.putExtra("time", time);
                sendBroadcast(intent);
                handler.postDelayed(this, 1000);
                createNotification("Cronómetro iniciado", "El cronómetro se ha iniciado.");

            }
        };
        handler.postDelayed(runnable, 0);
    }


    public void pauseChronometer() {
        System.out.println("en el pause");
        pauseTime = elapsedTime;
        handler.removeCallbacks(runnable);
    }

    public void stopChronometer() {
        System.out.println("en el stop");
        handler.removeCallbacks(runnable);

    }

    private void createNotification(String title, String text) {
        // Crear notificación
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent;
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notificationIntent = new Intent(this, MainActivity.class);
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            notificationIntent = new Intent(this, MainActivity.class);
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        }

        Notification notification = new NotificationCompat.Builder(this, "channel_01")
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }
}

