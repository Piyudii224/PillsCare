package com.crackint.pillscare;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String medicineId = intent.getStringExtra("medicineId");
        String medicineName = intent.getStringExtra("medicineName");
        String time = intent.getStringExtra("time");
        String day = intent.getStringExtra("day");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (medicineId != null && notificationManager != null) {
            notificationManager.cancel(medicineId.hashCode());
        }

        if ("ACTION_TAKEN".equals(action)) {
            Toast.makeText(context, medicineName + " marked as taken.", Toast.LENGTH_SHORT).show();

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String randomId = FirebaseDatabase.getInstance().getReference().push().getKey();

            long takenAt = System.currentTimeMillis();
            TakenMedicine takenMedicine = new TakenMedicine(medicineId, medicineName, takenAt);

            FirebaseDatabase.getInstance()
                    .getReference("takenHistory")
                    .child(userId)
                    .child(dateKey)
                    .child(randomId)
                    .setValue(takenMedicine);

        } else if ("ACTION_SNOOZE".equals(action)) {
            Data data = new Data.Builder()
                    .putString("medicineName", medicineName)
                    .putString("medicineId", medicineId)
                    .putString("time", time)
                    .putString("day", day)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MedicineNotificationWorker.class)
                    .setInitialDelay(5, TimeUnit.MINUTES)
                    .setInputData(data)
                    .build();

            WorkManager.getInstance(context).enqueue(workRequest);

            Toast.makeText(context, medicineName + " Reminder snoozed for 5 minutes.", Toast.LENGTH_SHORT).show();
        }
    }
}
