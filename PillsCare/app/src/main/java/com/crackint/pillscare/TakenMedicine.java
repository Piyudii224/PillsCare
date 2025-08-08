package com.crackint.pillscare;

public class TakenMedicine {
    private String medicineId;
    private String medicineName;
    private long takenAt;

    public TakenMedicine() {
        // Default constructor required for Firebase
    }

    public TakenMedicine(String medicineId, String medicineName, long takenAt) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.takenAt = takenAt;
    }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public long getTakenAt() { return takenAt; }
    public void setTakenAt(long takenAt) { this.takenAt = takenAt; }
}
