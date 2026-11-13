package com.smarterevententrysystem.ui;

import com.google.zxing.NotFoundException;
import com.smarterevententrysystem.dao.AttendanceDAO;
import com.smarterevententrysystem.dao.TicketDAO;
import com.smarterevententrysystem.model.Ticket;
import com.smarterevententrysystem.util.QRGenerator;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Entry gate screen. The admin types (or a USB barcode scanner types) the Ticket ID,
 * or loads a saved QR image. The ticket is checked in MySQL and attendance is marked.
 */
public class QRVerification extends JFrame {

    private JTextField txtTicketId;
    private JButton btnVerify;
    private JButton btnBrowseQR;
    private JButton btnClear;
    private JLabel lblResult;
    private JLabel lblParticipant;
    private JLabel lblEvent;
    private JLabel lblTicket;

    private final TicketDAO ticketDAO = new TicketDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public QRVerification() {
        setTitle("QR Verification / Entry Gate");
        setSize(680, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(UIHelper.createHeader("QR SCANNER / ENTRY VERIFICATION"), BorderLayout.NORTH);

        // ----- input area -----
        txtTicketId = new JTextField(22);
        txtTicketId.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(UIHelper.BACKGROUND);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));
        UIHelper.addFormRow(inputPanel, 0, "Ticket ID:", txtTicketId);

        btnVerify = UIHelper.createButton("Verify Entry", UIHelper.SUCCESS);
        btnBrowseQR = UIHelper.createButton("Load QR Image", UIHelper.PRIMARY);
        btnClear = UIHelper.createButton("Clear", UIHelper.GRAY);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttonPanel.setBackground(UIHelper.BACKGROUND);
        buttonPanel.add(btnVerify);
        buttonPanel.add(btnBrowseQR);
        buttonPanel.add(btnClear);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        // ----- result area -----
        lblResult = new JLabel("Waiting for ticket...", SwingConstants.CENTER);
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblResult.setForeground(UIHelper.GRAY);
        lblParticipant = new JLabel(" ", SwingConstants.CENTER);
        lblEvent = new JLabel(" ", SwingConstants.CENTER);
        lblTicket = new JLabel(" ", SwingConstants.CENTER);
        lblParticipant.setFont(UIHelper.LABEL_FONT);
        lblEvent.setFont(UIHelper.LABEL_FONT);
        lblTicket.setFont(UIHelper.LABEL_FONT);

        JPanel resultPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 25, 25, 25),
                BorderFactory.createLineBorder(new Color(200, 200, 200))));
        resultPanel.add(lblResult);
        resultPanel.add(lblParticipant);
        resultPanel.add(lblEvent);
        resultPanel.add(lblTicket);

        add(inputPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);
        resultPanel.setPreferredSize(new java.awt.Dimension(680, 270));

        // ----- connect buttons with ActionListener -----
        btnVerify.addActionListener(e -> verifyTicket());
        txtTicketId.addActionListener(e -> verifyTicket());   // Enter key (barcode scanners press Enter)
        btnBrowseQR.addActionListener(e -> loadQRImage());
        btnClear.addActionListener(e -> clearAll());
    }

    /** The heart of the project: valid -> allow + mark, used -> deny, unknown -> invalid. */
    private void verifyTicket() {
        String code = txtTicketId.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            UIHelper.showError(this, "Please enter or scan a Ticket ID.");
            return;
        }

        try {
            Ticket ticket = ticketDAO.findByCode(code);

            if (ticket == null) {
                showResult("Invalid Ticket", UIHelper.DANGER, null, JOptionPane.ERROR_MESSAGE);
            } else if (attendanceDAO.isAlreadyMarked(ticket.getTicketId())) {
                showResult("Duplicate Entry - Access Denied", UIHelper.WARNING, ticket,
                        JOptionPane.WARNING_MESSAGE);
            } else if (attendanceDAO.markAttendance(ticket.getTicketId())) {
                showResult("Entry Allowed", UIHelper.SUCCESS, ticket, JOptionPane.INFORMATION_MESSAGE);
            } else {
                // another gate marked the same ticket a split second earlier
                showResult("Duplicate Entry - Access Denied", UIHelper.WARNING, ticket,
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }

        txtTicketId.selectAll();
        txtTicketId.requestFocusInWindow();
    }

    private void showResult(String message, Color color, Ticket ticket, int dialogType) {
        lblResult.setText(message);
        lblResult.setForeground(color);
        if (ticket != null) {
            lblParticipant.setText("Participant: " + ticket.getParticipantName());
            lblEvent.setText("Event: " + ticket.getEventName());
            lblTicket.setText("Ticket ID: " + ticket.getTicketCode());
        } else {
            lblParticipant.setText("This Ticket ID does not exist in the system.");
            lblEvent.setText(" ");
            lblTicket.setText(" ");
        }
        JOptionPane.showMessageDialog(this, message, "Verification Result", dialogType);
    }

    /** Reads the QR code from a saved PNG/JPG image and verifies it. */
    private void loadQRImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            String text = QRGenerator.decodeQRFromFile(file);
            txtTicketId.setText(text);
            verifyTicket();
        } catch (NotFoundException ex) {
            UIHelper.showError(this, "No QR code could be found in that image.");
        } catch (IOException ex) {
            UIHelper.showError(this, "Could not read the image: " + ex.getMessage());
        }
    }

    private void clearAll() {
        txtTicketId.setText("");
        lblResult.setText("Waiting for ticket...");
        lblResult.setForeground(UIHelper.GRAY);
        lblParticipant.setText(" ");
        lblEvent.setText(" ");
        lblTicket.setText(" ");
        txtTicketId.requestFocusInWindow();
    }
}
