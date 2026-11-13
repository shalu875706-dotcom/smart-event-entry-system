package com.smarterevententrysystem.dao;

import com.smarterevententrysystem.database.DatabaseConnection;
import com.smarterevententrysystem.model.Participant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Database access for the participants table. */
public class ParticipantDAO {

    /** Saves the participant and returns the new participant_id. */
    public int addParticipant(Participant p) throws SQLException {
        String sql = "INSERT INTO participants (name, email, phone, event_id) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getPhone());
            ps.setInt(4, p.getEventId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Participant was saved but no ID was returned.");
    }

    public boolean emailExistsForEvent(String email, int eventId) throws SQLException {
        String sql = "SELECT 1 FROM participants WHERE email = ? AND event_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** How many people are already registered for the event. */
    public int countByEvent(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participants WHERE event_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Search participants by name, email, phone or Ticket ID.
     * eventId 0 means "all events". An empty keyword returns everybody.
     */
    public List<Participant> search(String keyword, int eventId) throws SQLException {
        String sql = "SELECT p.participant_id, p.name, p.email, p.phone, p.event_id, "
                   + "e.event_name, t.ticket_code, (a.attendance_id IS NOT NULL) AS attended "
                   + "FROM participants p "
                   + "JOIN events e ON p.event_id = e.event_id "
                   + "LEFT JOIN tickets t ON t.participant_id = p.participant_id "
                   + "LEFT JOIN attendance a ON a.ticket_id = t.ticket_id "
                   + "WHERE (? = 0 OR p.event_id = ?) "
                   + "AND (p.name LIKE ? OR p.email LIKE ? OR p.phone LIKE ? OR t.ticket_code LIKE ?) "
                   + "ORDER BY p.participant_id DESC";
        String like = "%" + keyword + "%";
        List<Participant> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, eventId);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Participant p = new Participant(rs.getString("name"), rs.getString("email"),
                            rs.getString("phone"), rs.getInt("event_id"));
                    p.setParticipantId(rs.getInt("participant_id"));
                    p.setEventName(rs.getString("event_name"));
                    p.setTicketCode(rs.getString("ticket_code"));
                    p.setAttended(rs.getBoolean("attended"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    /** Deletes the participant. Their ticket and attendance are removed too (ON DELETE CASCADE). */
    public boolean deleteParticipant(int participantId) throws SQLException {
        String sql = "DELETE FROM participants WHERE participant_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            return ps.executeUpdate() > 0;
        }
    }
}
