package com.smarterevententrysystem.ui;

import com.google.zxing.WriterException;
import com.smarterevententrysystem.dao.TicketDAO;
import com.smarterevententrysystem.model.Ticket;
import com.smarterevententrysystem.util.EmailConfig;
import com.smarterevententrysystem.util.EmailSender;
import com.smarterevententrysystem.util.QRGenerator;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

/** Shows a ticket's QR code, participant, event and Ticket ID, and lets the admin save the QR image. */
public class TicketForm extends JFrame {

    private JComboBox<Ticket> cmbTickets;
    private JLabel lblQR;
    private JLabel lblParticipantValue;
    private JLabel lblEventValue;
    private JLabel lblTicketIdValue;
    private JButton btnSaveQR;
    private JButton btnEmail;
    private JButton btnClose;

    private final TicketDAO ticketDAO = new TicketDAO();
    private BufferedImage currentQRImage;   // the QR picture that is on screen

    /** Pass a ticket code to open that ticket directly, or null to let the admin choose. */
    public TicketForm(String selectedTicketCode) {
        setTitle("QR Ticket");
        setSize(660, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadTickets(selectedTicketCode);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ----- top: header + ticket chooser -----
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(UIHelper.createHeader("QR TICKET"), BorderLayout.NORTH);
        cmbTickets = new JComboBox<>();
        cmbTickets.setFont(UIHelper.LABEL_FONT);
        JPanel choosePanel = new JPanel(new BorderLayout(10, 0));
        choosePanel.setBackground(UIHelper.BACKGROUND);
        choosePanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        choosePanel.add(UIHelper.createLabel("Select Ticket:"), BorderLayout.WEST);
        choosePanel.add(cmbTickets, BorderLayout.CENTER);
        topPanel.add(choosePanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // ----- centre: the QR picture -----
        lblQR = new JLabel("No ticket selected", SwingConstants.CENTER);
        lblQR.setFont(UIHelper.LABEL_FONT);
        lblQR.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        qrPanel.add(lblQR, BorderLayout.CENTER);
        add(qrPanel, BorderLayout.CENTER);

        // ----- bottom: details + buttons -----
        lblParticipantValue = UIHelper.createLabel("-");
        lblEventValue = UIHelper.createLabel("-");
        lblTicketIdValue = UIHelper.createLabel("-");
        lblTicketIdValue.setFont(UIHelper.BUTTON_FONT);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(UIHelper.BACKGROUND);
        UIHelper.addFormRow(detailsPanel, 0, "Participant:", lblParticipantValue);
        UIHelper.addFormRow(detailsPanel, 1, "Event:", lblEventValue);
        UIHelper.addFormRow(detailsPanel, 2, "Ticket ID:", lblTicketIdValue);

        btnSaveQR = UIHelper.createButton("Save QR Image", UIHelper.SUCCESS);
        btnEmail = UIHelper.createButton("Email QR Ticket", UIHelper.PRIMARY);
        btnClose = UIHelper.createButton("Close", UIHelper.GRAY);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(UIHelper.BACKGROUND);
        buttonPanel.add(btnSaveQR);
        buttonPanel.add(btnEmail);
        buttonPanel.add(btnClose);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(detailsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // ----- connect buttons with ActionListener -----
        btnSaveQR.addActionListener(e -> saveQRImage());
        btnEmail.addActionListener(e -> emailTicket());
        btnClose.addActionListener(e -> dispose());
    }

    private void loadTickets(String selectedTicketCode) {
        try {
            for (Ticket t : ticketDAO.getAllTickets()) {
                cmbTickets.addItem(t);
            }
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
            return;
        }

        if (selectedTicketCode != null) {
            for (int i = 0; i < cmbTickets.getItemCount(); i++) {
                if (cmbTickets.getItemAt(i).getTicketCode().equals(selectedTicketCode)) {
                    cmbTickets.setSelectedIndex(i);
                    break;
                }
            }
        }
        showSelectedTicket();
        // added after loading, so it only reacts when the admin picks another ticket
        cmbTickets.addActionListener(e -> showSelectedTicket());
    }

    private void showSelectedTicket() {
        Ticket ticket = (Ticket) cmbTickets.getSelectedItem();
        if (ticket == null) {
            return;
        }
        try {
            currentQRImage = QRGenerator.generateQRImage(ticket.getTicketCode(), 300);
            lblQR.setText("");
            lblQR.setIcon(new ImageIcon(currentQRImage));
            lblParticipantValue.setText(ticket.getParticipantName());
            lblEventValue.setText(ticket.getEventName());
            lblTicketIdValue.setText(ticket.getTicketCode());
        } catch (WriterException ex) {
            UIHelper.showError(this, "Could not generate the QR code: " + ex.getMessage());
        }
    }

    private void saveQRImage() {
        Ticket ticket = (Ticket) cmbTickets.getSelectedItem();
        if (ticket == null || currentQRImage == null) {
            UIHelper.showError(this, "There is no QR code to save.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(ticket.getTicketCode() + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }
        try {
            QRGenerator.saveQRImage(currentQRImage, file);
            UIHelper.showInfo(this, "QR image saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            UIHelper.showError(this, "Could not save the image: " + ex.getMessage());
        }
    }

    /** Sends the QR ticket to the participant's email (runs in the background so the window does not freeze). */
    private void emailTicket() {
        Ticket ticket = (Ticket) cmbTickets.getSelectedItem();
        if (ticket == null || currentQRImage == null) {
            UIHelper.showError(this, "There is no ticket to send.");
            return;
        }
        if (!EmailConfig.isConfigured()) {
            UIHelper.showError(this, "Email is not set up yet.\n"
                    + "Open EmailConfig.java and enter your Gmail address and App Password.");
            return;
        }
        String email = JOptionPane.showInputDialog(this,
                "Send the QR ticket to this email address:", ticket.getParticipantEmail());
        if (email == null) {
            return;   // cancelled
        }
        email = email.trim();
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            UIHelper.showError(this, "Please enter a valid email address.");
            return;
        }

        final String toEmail = email;
        final Ticket sendTicket = ticket;
        final BufferedImage qrImage = currentQRImage;
        btnEmail.setEnabled(false);
        btnEmail.setText("Sending...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                byte[] png = QRGenerator.toPngBytes(qrImage);
                EmailSender.sendTicket(toEmail, sendTicket.getParticipantName(),
                        sendTicket.getEventName(), sendTicket.getTicketCode(), png);
                return null;
            }

            @Override
            protected void done() {
                btnEmail.setEnabled(true);
                btnEmail.setText("Email QR Ticket");
                try {
                    get();
                    UIHelper.showInfo(TicketForm.this, "Ticket sent to " + toEmail);
                } catch (ExecutionException ex) {
                    UIHelper.showError(TicketForm.this, "Could not send the email:\n"
                            + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        worker.execute();
    }
}
