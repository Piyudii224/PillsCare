package com.crackint.pillscare;

import java.io.Serializable;
import java.util.List;

public class Medicine implements Serializable {
    private String id;
    private String name;
    private List<String> times;   // e.g. ["08:00", "14:00"]
    private List<String> days;    // e.g. ["Mon", "Tue"]
    private String frequency;     // e.g. "Once a day"

    public Medicine() {
        // Default constructor required for Firebase
    }

    public Medicine(String id, String name, List<String> times, List<String> days, String frequency) {
        this.id = id;
        this.name = name;
        this.times = times;
        this.days = days;
        this.frequency = frequency;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getTimes() { return times; }
    public void setTimes(List<String> times) { this.times = times; }

    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
}
