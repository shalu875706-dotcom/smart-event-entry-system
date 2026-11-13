package com.smarterevententrysystem.dao;

import com.smarterevententrysystem.database.DatabaseConnection;
import com.smarterevententrysystem.model.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Database access for the tickets table. */
public class TicketDAO {

    private static final String SELECT_TICKET =
            "SELECT t.ticket_id, t.ticket_code, t.participant_id, t.issued_at, "
          + "p.name AS participant_name, p.email AS participant_email, e.event_name "
          + "FROM tickets t "
          + "JOIN participants p ON t.participant_id = p.participant_id "
          + "JOIN events e ON p.event_id = e.event_id ";

    /** Creates a unique ticket for the participant and returns the Ticket ID text. */
    public String createTicket(int participantId) throws SQLException {
        String sql = "INSERT INTO tickets (ticket_code, participant_id) VALUES (?, ?)";
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = generateTicketCode();
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, code);
                ps.setInt(2, participantId);
                ps.executeUpdate();
                return code;
            } catch (SQLIntegrityConstraintViolationException e) {
                // the random code already existed (very rare) - try again with a new one
            }
        }
        throw new SQLException("Could not create a unique ticket. Please try again.");
    }

    /** Finds a ticket by its code. Returns null if it does not exist. */
    public Ticket findByCode(String ticketCode) throws SQLException {
        String sql = SELECT_TICKET + "WHERE t.ticket_code = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ticketCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Ticket> getAllTickets() throws SQLException {
        String sql = SELECT_TICKET + "ORDER BY t.ticket_id DESC";
        List<Ticket> tickets = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tickets.add(mapRow(rs));
            }
        }
        return tickets;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getInt("ticket_id"));
        ticket.setTicketCode(rs.getString("ticket_code"));
        ticket.setParticipantId(rs.getInt("participant_id"));
        ticket.setParticipantName(rs.getString("participant_name"));
        ticket.setParticipantEmail(rs.getString("participant_email"));
        ticket.setEventName(rs.getString("event_name"));
        ticket.setIssuedAt(rs.getTimestamp("issued_at").toLocalDateTime());
        return ticket;
    }

    /** Example result: TKT-3F9A1C7B */
    private String generateTicketCode() {
        return "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
