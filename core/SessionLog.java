package core;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

public class SessionLog {
    private LocalDate date;
    private int durationMinutes; // ALWAYS required
    private LocalTime startTime; // Might be null
    private LocalTime endTime;   // Might be null
    private String notes;
    private String id;
    private ArrayList<SessionAttribute> attributes; // List of attributes for this session

    // CONSTRUCTOR 1: For the "Manual Input" option
    public SessionLog(LocalDate date, int durationMinutes, ArrayList<SessionAttribute> attributes, String notes) {
        this(date, durationMinutes, attributes, notes, UUID.randomUUID().toString());
    }

    public SessionLog(LocalDate date, int durationMinutes, ArrayList<SessionAttribute> attributes, String notes, String id) {
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.startTime = null; // We don't know when they did it
        this.endTime = null;   // We don't know when they finished
        this.attributes = attributes; // Initialize attributes with the provided list
        this.notes = notes;
        this.id = id; // Persisted identifier when available
    }

    // CONSTRUCTOR 2: For the "Timer" option
    public SessionLog(LocalDate date, LocalTime startTime, LocalTime endTime, int durationMinutes, ArrayList<SessionAttribute
        > attributes, String notes) {
        this(date, startTime, endTime, durationMinutes, attributes, notes, UUID.randomUUID().toString());
    }

    public SessionLog(LocalDate date, LocalTime startTime, LocalTime endTime, int durationMinutes, ArrayList<SessionAttribute
        > attributes, String notes, String id) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.attributes = attributes;
        this.notes = notes;
        this.id = id; // Persisted identifier when available
    }

    public LocalDate getDate() {
        return date;
    }
    public int getDurationMinutes() {
        return durationMinutes;
    }
    public String getNotes() {
        return notes;
    }
    public LocalTime getStartTime() {
        return startTime;
    }
    public LocalTime getEndTime() {
        return endTime;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public ArrayList<SessionAttribute> getAttributes() {
        return attributes;
    }
    public void addAttribute(SessionAttribute attribute) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<>();
        }
        this.attributes.add(attribute);
    }
    public void removeAttribute(SessionAttribute attribute) {
        if (this.attributes != null) {
            this.attributes.remove(attribute);
        }
    }
    
}