package com.smarterevententrysystem.ui;

import com.smarterevententrysystem.dao.EventDAO;
import com.smarterevententrysystem.dao.ParticipantDAO;
import com.smarterevententrysystem.model.Event;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Create, view, update and delete events. */
public class EventForm extends JFrame {

    private JTextField txtEventName;
    private JTextField txtEventDate;
    private JTextField txtEventTime;
    private JTextField txtVenue;
    private JTextField txtMaxParticipants;
    private JButton btnSave;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JTable tblEvents;
    private DefaultTableModel tableModel;

    private final EventDAO eventDAO = new EventDAO();
    private final ParticipantDAO participantDAO = new ParticipantDAO();
    private int selectedEventId = -1;   // -1 means "no event selected in the table"

    public EventForm() {
        setTitle("Event Management");
        setSize(900, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadEvents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(UIHelper.createHeader("EVENT MANAGEMENT"), BorderLayout.NORTH);

        // ----- form -----
        txtEventName = new JTextField(25);
        txtEventDate = new JTextField(25);
        txtEventTime = new JTextField(25);
        txtVenue = new JTextField(25);
        txtMaxParticipants = new JTextField(25);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.BACKGROUND);
        UIHelper.addFormRow(formPanel, 0, "Event Name:", txtEventName);
        UIHelper.addFormRow(formPanel, 1, "Event Date (yyyy-MM-dd):", txtEventDate);
        UIHelper.addFormRow(formPanel, 2, "Event Time (HH:mm, 24-hour):", txtEventTime);
        UIHelper.addFormRow(formPanel, 3, "Venue:", txtVenue);
        UIHelper.addFormRow(formPanel, 4, "Maximum Participants:", txtMaxParticipants);

        // ----- buttons -----
        btnSave = UIHelper.createButton("Save Event", UIHelper.SUCCESS);
        btnUpdate = UIHelper.createButton("Update Event", UIHelper.PRIMARY);
        btnDelete = UIHelper.createButton("Delete Event", UIHelper.DANGER);
        btnClear = UIHelper.createButton("Clear", UIHelper.GRAY);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.setBackground(UIHelper.BACKGROUND);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIHelper.BACKGROUND);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ----- table -----
        String[] columns = {"ID", "Event Name", "Date", "Time", "Venue", "Max", "Registered"};
        tableModel = UIHelper.createReadOnlyModel(columns);
        tblEvents = new JTable(tableModel);
        UIHelper.styleTable(tblEvents);
        JScrollPane scrollPane = new JScrollPane(tblEvents);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        add(topPanel, BorderLayout.CENTER);
        // table gets its own panel so it has a fixed height under the form
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.setPreferredSize(new java.awt.Dimension(900, 280));
        add(tablePanel, BorderLayout.SOUTH);

        // ----- connect buttons with ActionListener -----
        btnSave.addActionListener(e -> saveEvent());
        btnUpdate.addActionListener(e -> updateEvent());
        btnDelete.addActionListener(e -> deleteEvent());
        btnClear.addActionListener(e -> clearForm());
        tblEvents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });
    }

    private void loadEvents() {
        try {
            tableModel.setRowCount(0);
            List<Event> events = eventDAO.getAllEvents();
            for (Event ev : events) {
                tableModel.addRow(new Object[]{
                    ev.getEventId(), ev.getEventName(), ev.getEventDate(), ev.getEventTime(),
                    ev.getVenue(), ev.getMaxParticipants(), ev.getRegisteredCount()
                });
            }
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = tblEvents.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedEventId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        txtEventName.setText(tableModel.getValueAt(row, 1).toString());
        txtEventDate.setText(tableModel.getValueAt(row, 2).toString());
        txtEventTime.setText(tableModel.getValueAt(row, 3).toString());
        txtVenue.setText(tableModel.getValueAt(row, 4).toString());
        txtMaxParticipants.setText(tableModel.getValueAt(row, 5).toString());
    }

    /** Reads and validates the form. Returns null (after showing a message) if something is wrong. */
    private Event readEventFromForm() {
        String name = txtEventName.getText().trim();
        String venue = txtVenue.getText().trim();
        if (name.isEmpty() || venue.isEmpty()) {
            UIHelper.showError(this, "Event Name and Venue cannot be empty.");
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(txtEventDate.getText().trim());
        } catch (DateTimeParseException ex) {
            UIHelper.showError(this, "Invalid date. Use the format yyyy-MM-dd, for example 2026-11-15.");
            return null;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(txtEventTime.getText().trim());
        } catch (DateTimeParseException ex) {
            UIHelper.showError(this, "Invalid time. Use the 24-hour format HH:mm, for example 18:30.");
            return null;
        }

        int max;
        try {
            max = Integer.parseInt(txtMaxParticipants.getText().trim());
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Maximum Participants must be a whole number.");
            return null;
        }
        if (max <= 0) {
            UIHelper.showError(this, "Maximum Participants must be greater than 0.");
            return null;
        }

        return new Event(name, date, time, venue, max);
    }

    private void saveEvent() {
        Event event = readEventFromForm();
        if (event == null) {
            return;
        }
        try {
            eventDAO.addEvent(event);
            UIHelper.showInfo(this, "Event saved successfully.");
            clearForm();
            loadEvents();
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void updateEvent() {
        if (selectedEventId == -1) {
            UIHelper.showError(this, "Please click an event in the table first.");
            return;
        }
        Event event = readEventFromForm();
        if (event == null) {
            return;
        }
        event.setEventId(selectedEventId);
        try {
            int registered = participantDAO.countByEvent(selectedEventId);
            if (event.getMaxParticipants() < registered) {
                UIHelper.showError(this, "Maximum Participants cannot be less than the "
                        + registered + " people already registered.");
                return;
            }
            eventDAO.updateEvent(event);
            UIHelper.showInfo(this, "Event updated successfully.");
            clearForm();
            loadEvents();
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void deleteEvent() {
        if (selectedEventId == -1) {
            UIHelper.showError(this, "Please click an event in the table first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete this event?\nAll participants, tickets and attendance of this event will also be deleted.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            eventDAO.deleteEvent(selectedEventId);
            UIHelper.showInfo(this, "Event deleted.");
            clearForm();
            loadEvents();
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void clearForm() {
        txtEventName.setText("");
        txtEventDate.setText("");
        txtEventTime.setText("");
        txtVenue.setText("");
        txtMaxParticipants.setText("");
        selectedEventId = -1;
        tblEvents.clearSelection();
    }
}
