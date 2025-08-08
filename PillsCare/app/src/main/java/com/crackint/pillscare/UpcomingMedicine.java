package com.crackint.pillscare;

public class UpcomingMedicine {
    private String medicineName;
    private String time;

    public UpcomingMedicine() {}

    public UpcomingMedicine(String medicineName, String time) {
        this.medicineName = medicineName;
        this.time = time;
    }

    public String getMedicineName() { return medicineName; }
    public String getTime() { return time; }
}
