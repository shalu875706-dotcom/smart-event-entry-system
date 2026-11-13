package com.smarterevententrysystem.dao;

import com.smarterevententrysystem.database.DatabaseConnection;
import com.smarterevententrysystem.model.Event;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/** Database access for the events table (create, read, update, delete). */
public class EventDAO {

    public boolean addEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (event_name, event_date, event_time, venue, max_participants) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, event.getEventName());
            ps.setDate(2, Date.valueOf(event.getEventDate()));
            ps.setTime(3, Time.valueOf(event.getEventTime()));
            ps.setString(4, event.getVenue());
            ps.setInt(5, event.getMaxParticipants());
            return ps.executeUpdate() > 0;
        }
    }

    /** All events, with the number of registered participants for each. */
    public List<Event> getAllEvents() throws SQLException {
        String sql = "SELECT e.event_id, e.event_name, e.event_date, e.event_time, e.venue, "
                   + "e.max_participants, "
                   + "(SELECT COUNT(*) FROM participants p WHERE p.event_id = e.event_id) AS registered "
                   + "FROM events e ORDER BY e.event_date, e.event_time";
        List<Event> events = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getInt("event_id"));
                event.setEventName(rs.getString("event_name"));
                event.setEventDate(rs.getDate("event_date").toLocalDate());
                event.setEventTime(rs.getTime("event_time").toLocalTime());
                event.setVenue(rs.getString("venue"));
                event.setMaxParticipants(rs.getInt("max_participants"));
                event.setRegisteredCount(rs.getInt("registered"));
                events.add(event);
            }
        }
        return events;
    }

    public boolean updateEvent(Event event) throws SQLException {
        String sql = "UPDATE events SET event_name = ?, event_date = ?, event_time = ?, "
                   + "venue = ?, max_participants = ? WHERE event_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, event.getEventName());
            ps.setDate(2, Date.valueOf(event.getEventDate()));
            ps.setTime(3, Time.valueOf(event.getEventTime()));
            ps.setString(4, event.getVenue());
            ps.setInt(5, event.getMaxParticipants());
            ps.setInt(6, event.getEventId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteEvent(int eventId) throws SQLException {
        String sql = "DELETE FROM events WHERE event_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }
}
