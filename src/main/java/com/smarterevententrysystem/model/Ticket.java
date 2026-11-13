package com.smarterevententrysystem.model;

import java.time.LocalDateTime;

public class Ticket {
    private int ticketId;
    private String ticketCode;       // the text stored inside the QR code
    private int participantId;
    private String participantName;  // joined from participants table
    private String participantEmail; // joined from participants table
    private String eventName;        // joined from events table
    private LocalDateTime issuedAt;

    public Ticket() { }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public String getParticipantEmail() { return participantEmail; }
    public void setParticipantEmail(String participantEmail) { this.participantEmail = participantEmail; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    @Override
    public String toString() {
        return ticketCode + " - " + participantName;
    }
}
