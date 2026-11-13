package com.smarterevententrysystem.model;

import java.time.LocalDateTime;

public class Attendance {
    private int attendanceId;
    private int ticketId;
    private String ticketCode;
    private String participantName;
    private String eventName;
    private LocalDateTime entryTime;
    private String status;

    public Attendance() { }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
