package com.smarterevententrysystem.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Small helper methods so every screen looks the same
 * and we do not repeat the same styling code in every class.
 */
public class UIHelper {

    public static final Color PRIMARY = new Color(25, 118, 210);
    public static final Color DARK = new Color(33, 47, 61);
    public static final Color SUCCESS = new Color(46, 125, 50);
    public static final Color DANGER = new Color(198, 40, 40);
    public static final Color WARNING = new Color(239, 108, 0);
    public static final Color GRAY = new Color(96, 125, 139);
    public static final Color BACKGROUND = new Color(245, 247, 250);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private UIHelper() { }

    public static JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(170, 40));
        return button;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }

    /** Dark blue banner with a white title, shown at the top of every screen. */
    public static JPanel createHeader(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        JLabel label = new JLabel(title);
        label.setFont(TITLE_FONT);
        label.setForeground(Color.WHITE);
        panel.add(label);
        return panel;
    }

    /** Adds "Label: [field]" as one row of a GridBagLayout form. */
    public static void addFormRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 10, 7, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(createLabel(labelText), gbc);

        field.setFont(LABEL_FONT);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(LABEL_FONT);
        table.getTableHeader().setFont(BUTTON_FONT);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(187, 222, 251));
        table.setSelectionForeground(Color.BLACK);
    }

    /** A table model whose cells cannot be edited by typing. */
    public static DefaultTableModel createReadOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showDatabaseError(Component parent, java.sql.SQLException ex) {
        showError(parent, "Database error: " + ex.getMessage()
                + "\n\nCheck that MySQL is running and the password in DatabaseConnection.java is correct.");
    }
}
