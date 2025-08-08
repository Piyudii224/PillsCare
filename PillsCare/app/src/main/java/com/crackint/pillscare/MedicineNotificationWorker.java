package com.crackint.pillscare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MedicineNotificationWorker extends Worker {

    public MedicineNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String medicineName = getInputData().getString("medicineName");
        String time = getInputData().getString("time");
        String medicineId = getInputData().getString("medicineId");
        String day = getInputData().getString("day"); // Optional

        sendNotification(medicineName, time, medicineId, day);
        return Result.success();
    }

    private void sendNotification(String medicineName, String time, String medicineId, String day) {
        Context context = getApplicationContext();
        String channelId = "medicine_reminders";
        String channelName = "Medicine Reminders";

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(channelId) == null) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Intent for "Taken" action
        Intent takenIntent = new Intent(context, NotificationActionReceiver.class);
        takenIntent.setAction("ACTION_TAKEN");
        takenIntent.putExtra("medicineId", medicineId);
        takenIntent.putExtra("medicineName", medicineName);
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(context, medicineId.hashCode() + 1, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent for "Snooze" action
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("medicineId", medicineId);
        snoozeIntent.putExtra("medicineName", medicineName);
        snoozeIntent.putExtra("time", time);
        snoozeIntent.putExtra("day", day);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, medicineId.hashCode() + 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(androidx.biometric.R.drawable.fingerprint_dialog_fp_icon)
                .setContentTitle("Medicine Reminder")
                .setContentText("Time to take: " + medicineName + " at " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_dialog_alert, "Taken", takenPendingIntent)
                .addAction(android.R.drawable.ic_dialog_alert, "Remind Me Later", snoozePendingIntent);

        int notificationId = medicineId.hashCode();
        notificationManager.notify(notificationId, builder.build());
    }
}
