package com.smarterevententrysystem.dao;

import com.smarterevententrysystem.database.DatabaseConnection;
import com.smarterevententrysystem.model.Attendance;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/** Database access for the attendance table. */
public class AttendanceDAO {

    /** True if this ticket has already been used to enter. */
    public boolean isAlreadyMarked(int ticketId) throws SQLException {
        String sql = "SELECT 1 FROM attendance WHERE ticket_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Marks the ticket as used. Returns false if it was already marked.
     * The UNIQUE constraint on attendance.ticket_id is the final safety net:
     * even if two scanners check at the same moment, only one insert succeeds.
     */
    public boolean markAttendance(int ticketId) throws SQLException {
        String sql = "INSERT INTO attendance (ticket_id) VALUES (?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        }
    }

    public List<Attendance> getAllAttendance() throws SQLException {
        return getAttendance(0);
    }

    /** Attendance of one event. eventId 0 means "all events". */
    public List<Attendance> getAttendance(int eventId) throws SQLException {
        String sql = "SELECT a.attendance_id, a.ticket_id, a.entry_time, a.status, "
                   + "t.ticket_code, p.name AS participant_name, e.event_name "
                   + "FROM attendance a "
                   + "JOIN tickets t ON a.ticket_id = t.ticket_id "
                   + "JOIN participants p ON t.participant_id = p.participant_id "
                   + "JOIN events e ON p.event_id = e.event_id "
                   + "WHERE (? = 0 OR e.event_id = ?) "
                   + "ORDER BY a.entry_time DESC";
        List<Attendance> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Attendance a = new Attendance();
                    a.setAttendanceId(rs.getInt("attendance_id"));
                    a.setTicketId(rs.getInt("ticket_id"));
                    a.setTicketCode(rs.getString("ticket_code"));
                    a.setParticipantName(rs.getString("participant_name"));
                    a.setEventName(rs.getString("event_name"));
                    a.setEntryTime(rs.getTimestamp("entry_time").toLocalDateTime());
                    a.setStatus(rs.getString("status"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    /**
     * Live numbers for the dashboard.
     * Returns {registered, entered}. eventId 0 means "all events".
     */
    public int[] getStats(int eventId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT p.participant_id) AS registered, "
                   + "COUNT(a.attendance_id) AS entered "
                   + "FROM participants p "
                   + "LEFT JOIN tickets t ON t.participant_id = p.participant_id "
                   + "LEFT JOIN attendance a ON a.ticket_id = t.ticket_id "
                   + "WHERE (? = 0 OR p.event_id = ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[]{rs.getInt("registered"), rs.getInt("entered")};
            }
        }
    }
}
