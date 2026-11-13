package com.smarterevententrysystem.dao;

import com.smarterevententrysystem.database.DatabaseConnection;
import com.smarterevententrysystem.model.Admin;
import com.smarterevententrysystem.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Database access for the admins table. */
public class AdminDAO {

    /** Returns the Admin if username/password are correct, otherwise null. */
    public Admin login(String username, String password) throws SQLException {
        String sql = "SELECT admin_id, username, full_name, password_hash FROM admins WHERE username = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String storedHash = rs.getString("password_hash");
                if (!PasswordUtil.verify(password, storedHash)) {
                    return null;
                }
                Admin admin = new Admin(rs.getInt("admin_id"), rs.getString("username"), rs.getString("full_name"));
                if (PasswordUtil.isLegacyHash(storedHash)) {
                    upgradeToBCrypt(con, admin.getAdminId(), password);
                }
                return admin;
            }
        }
    }

    /** Replaces an old SHA-256 hash with a BCrypt hash. A failure here never blocks the login. */
    private void upgradeToBCrypt(Connection con, int adminId, String password) {
        String sql = "UPDATE admins SET password_hash = ? WHERE admin_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(password));
            ps.setInt(2, adminId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // upgrade is optional - the user is already logged in
        }
    }
}
