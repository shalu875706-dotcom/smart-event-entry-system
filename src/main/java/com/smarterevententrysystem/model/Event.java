package com.smarterevententrysystem.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {
    private int eventId;
    private String eventName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venue;
    private int maxParticipants;
    private int registeredCount;   // filled when events are loaded for the table

    public Event() { }

    public Event(String eventName, LocalDate eventDate, LocalTime eventTime, String venue, int maxParticipants) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.maxParticipants = maxParticipants;
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }

    /** JComboBox shows whatever toString() returns. */
    @Override
    public String toString() {
        if (eventDate == null) {
            return eventName;   // used for the "All events" choice
        }
        return eventName + " (" + eventDate + ")";
    }

    /** A special choice (id 0) used by filters: "All events". */
    public static Event allEvents() {
        Event e = new Event();
        e.setEventId(0);
        e.setEventName("All events");
        return e;
    }
}
