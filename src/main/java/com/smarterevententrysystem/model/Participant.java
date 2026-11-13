package com.smarterevententrysystem.model;

public class Participant {
    private int participantId;
    private String name;
    private String email;
    private String phone;
    private int eventId;
    private String eventName;   // filled by ParticipantDAO.search()
    private String ticketCode;  // filled by ParticipantDAO.search()
    private boolean attended;   // filled by ParticipantDAO.search()

    public Participant(String name, String email, String phone, int eventId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.eventId = eventId;
    }

    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public int getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
}
