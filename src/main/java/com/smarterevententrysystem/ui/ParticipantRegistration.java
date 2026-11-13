package com.smarterevententrysystem.ui;

import com.smarterevententrysystem.dao.EventDAO;
import com.smarterevententrysystem.dao.ParticipantDAO;
import com.smarterevententrysystem.dao.TicketDAO;
import com.smarterevententrysystem.model.Event;
import com.smarterevententrysystem.model.Participant;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Registers a participant for an event and creates the ticket. */
public class ParticipantRegistration extends JFrame {

    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JComboBox<Event> cmbEvent;
    private JButton btnRegister;
    private JButton btnClear;

    private final EventDAO eventDAO = new EventDAO();
    private final ParticipantDAO participantDAO = new ParticipantDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    public ParticipantRegistration() {
        setTitle("Participant Registration");
        setSize(580, 460);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadEvents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(UIHelper.createHeader("PARTICIPANT REGISTRATION"), BorderLayout.NORTH);

        txtName = new JTextField(22);
        txtEmail = new JTextField(22);
        txtPhone = new JTextField(22);
        cmbEvent = new JComboBox<>();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        UIHelper.addFormRow(formPanel, 0, "Participant Name:", txtName);
        UIHelper.addFormRow(formPanel, 1, "Email:", txtEmail);
        UIHelper.addFormRow(formPanel, 2, "Phone Number:", txtPhone);
        UIHelper.addFormRow(formPanel, 3, "Select Event:", cmbEvent);
        add(formPanel, BorderLayout.CENTER);

        btnRegister = UIHelper.createButton("Register", UIHelper.SUCCESS);
        btnClear = UIHelper.createButton("Clear", UIHelper.GRAY);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(UIHelper.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnClear);
        add(buttonPanel, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> registerParticipant());
        btnClear.addActionListener(e -> clearForm());
    }

    private void loadEvents() {
        try {
            cmbEvent.removeAllItems();
            for (Event ev : eventDAO.getAllEvents()) {
                cmbEvent.addItem(ev);
            }
            if (cmbEvent.getItemCount() == 0) {
                UIHelper.showInfo(this, "No events found. Please create an event first.");
            }
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void registerParticipant() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        Event selectedEvent = (Event) cmbEvent.getSelectedItem();

        // ----- validation -----
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            UIHelper.showError(this, "Please fill in name, email and phone number.");
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            UIHelper.showError(this, "Please enter a valid email address.");
            return;
        }
        if (!phone.matches("^[0-9]{10,15}$")) {
            UIHelper.showError(this, "Phone number must contain 10 to 15 digits only.");
            return;
        }
        if (selectedEvent == null) {
            UIHelper.showError(this, "Please select an event.");
            return;
        }

        try {
            if (participantDAO.countByEvent(selectedEvent.getEventId()) >= selectedEvent.getMaxParticipants()) {
                UIHelper.showError(this, "Sorry, this event is full ("
                        + selectedEvent.getMaxParticipants() + " participants).");
                return;
            }
            if (participantDAO.emailExistsForEvent(email, selectedEvent.getEventId())) {
                UIHelper.showError(this, "This email is already registered for the selected event.");
                return;
            }

            // ----- save participant, then create the ticket -----
            int participantId = participantDAO.addParticipant(
                    new Participant(name, email, phone, selectedEvent.getEventId()));
            String ticketCode = ticketDAO.createTicket(participantId);

            UIHelper.showInfo(this, "Registration successful!\nYour Ticket ID is: " + ticketCode);
            clearForm();
            new TicketForm(ticketCode).setVisible(true);   // show the QR ticket
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        if (cmbEvent.getItemCount() > 0) {
            cmbEvent.setSelectedIndex(0);
        }
    }
}
