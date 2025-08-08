package com.crackint.pillscare;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, Medicine medicine) {
        List<String> times = medicine.getTimes();
        if (times == null || times.isEmpty()) return;

        for (String time : times) {
            long delay = getDelayUntilTrigger(time);
            Log.d("ReminderScheduler", "Scheduling " + medicine.getName() + " at " + time + ", delay: " + delay + "ms");

            // Use test delay if needed
            // delay = 10000; // <-- Uncomment to test with 10s delay

            Data inputData = new Data.Builder()
                    .putString("medicineName", medicine.getName())
                    .putString("time", time)
                    .putString("medicineId", medicine.getId())
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(MedicineNotificationWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(medicine.getId() + "_" + time.replace(":", "").replace(" ", ""))
                    .build();

            WorkManager.getInstance(context).enqueue(notificationWork);
        }
    }

    private static long getDelayUntilTrigger(String timeString) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        Calendar now = Calendar.getInstance();
        Calendar trigger = Calendar.getInstance();

        try {
            Date date = sdf.parse(timeString);
            if (date == null) {
                Log.e("ReminderScheduler", "Time parsing failed for: " + timeString);
                return 60000; // fallback 1 min
            }

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(date);

            int hour = timeCal.get(Calendar.HOUR_OF_DAY);
            int minute = timeCal.get(Calendar.MINUTE);

            trigger.set(Calendar.HOUR_OF_DAY, hour);
            trigger.set(Calendar.MINUTE, minute);
            trigger.set(Calendar.SECOND, 0);
            trigger.set(Calendar.MILLISECOND, 0);

            if (trigger.before(now)) {
                trigger.add(Calendar.DAY_OF_YEAR, 1);
            }

            long delay = trigger.getTimeInMillis() - now.getTimeInMillis();
            Log.d("ReminderScheduler", "Parsed time: " + timeString + " -> delay(ms): " + delay);
            return delay;

        } catch (ParseException e) {
            Log.e("ReminderScheduler", "ParseException for time: " + timeString, e);
            return 60000; // fallback 1 minute
        }
    }

    public static void cancelReminder(Context context, String medicineId) {
        WorkManager.getInstance(context).cancelAllWorkByTag(medicineId);
        Log.d("ReminderScheduler", "Cancelled all reminders for: " + medicineId);
    }
}
