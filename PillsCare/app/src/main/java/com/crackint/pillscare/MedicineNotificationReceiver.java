package com.crackint.pillscare;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MedicineNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals("ACTION_TAKEN")) {
            String medicineId = intent.getStringExtra("medicineId");
            if (medicineId == null) return;

            if (FirebaseAuth.getInstance().getCurrentUser() == null) return;  // User must be logged in

            FirebaseDatabase.getInstance().getReference("medicines")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(medicineId)
                    .child("taken")
                    .setValue(true);

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(medicineId.hashCode());

            ReminderScheduler.cancelReminder(context, medicineId);

        } else if (action.equals("ACTION_SNOOZE")) {
            String medicineName = intent.getStringExtra("medicineName");
            String time = intent.getStringExtra("time");
            String medicineId = intent.getStringExtra("medicineId");

            if (medicineName == null || medicineId == null) return;

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(medicineId.hashCode());

            Data inputData = new Data.Builder()
                    .putString("medicineName", medicineName)
                    .putString("time", time)
                    .putString("medicineId", medicineId)
                    .build();

            OneTimeWorkRequest snoozeWork = new OneTimeWorkRequest.Builder(MedicineNotificationWorker.class)
                    .setInitialDelay(10, java.util.concurrent.TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .addTag(medicineId)
                    .build();

            WorkManager.getInstance(context).enqueue(snoozeWork);
        }
    }
}
