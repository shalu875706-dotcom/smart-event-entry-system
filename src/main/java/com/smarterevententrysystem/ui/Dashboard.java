package com.smarterevententrysystem.ui;

import com.smarterevententrysystem.dao.AttendanceDAO;
import com.smarterevententrysystem.dao.EventDAO;
import com.smarterevententrysystem.model.Admin;
import com.smarterevententrysystem.model.Event;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/** Main menu shown after a successful login, with live attendance numbers. */
public class Dashboard extends JFrame {

    private final Admin loggedInAdmin;

    private JButton btnCreateEvent;
    private JButton btnViewEvents;
    private JButton btnRegisterParticipant;
    private JButton btnParticipants;
    private JButton btnGenerateQR;
    private JButton btnVerifyEntry;
    private JButton btnAttendance;
    private JButton btnLogout;

    private JComboBox<Event> cmbStatsEvent;
    private JButton btnRefreshStats;
    private JLabel lblRegistered;
    private JLabel lblEntered;
    private JLabel lblRemaining;

    private final EventDAO eventDAO = new EventDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private Timer statsTimer;
    private boolean loadingStatsEvents = false;

    public Dashboard(Admin admin) {
        this.loggedInAdmin = admin;
        setTitle("Smart Event Entry System - Dashboard");
        setSize(700, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadStatsEvents();

        // update the live numbers every 5 seconds
        statsTimer = new Timer(5000, e -> refreshStats());
        statsTimer.start();
        // reload the event list (and numbers) whenever this window gets focus again
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                loadStatsEvents();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ----- top: header + live count -----
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(UIHelper.createHeader("ADMIN DASHBOARD"), BorderLayout.NORTH);
        topPanel.add(createStatsPanel(), BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ----- menu buttons -----
        JPanel menuPanel = new JPanel(new GridLayout(4, 2, 18, 18));
        menuPanel.setBackground(UIHelper.BACKGROUND);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 15, 40));

        btnCreateEvent = UIHelper.createButton("Create Event", UIHelper.PRIMARY);
        btnViewEvents = UIHelper.createButton("View Events", UIHelper.PRIMARY);
        btnRegisterParticipant = UIHelper.createButton("Register Participant", UIHelper.SUCCESS);
        btnParticipants = UIHelper.createButton("Participants List", UIHelper.SUCCESS);
        btnGenerateQR = UIHelper.createButton("Generate QR Ticket", UIHelper.SUCCESS);
        btnVerifyEntry = UIHelper.createButton("Scan QR / Verify Entry", UIHelper.WARNING);
        btnAttendance = UIHelper.createButton("Attendance", UIHelper.GRAY);
        btnLogout = UIHelper.createButton("Logout", UIHelper.DANGER);

        menuPanel.add(btnCreateEvent);
        menuPanel.add(btnViewEvents);
        menuPanel.add(btnRegisterParticipant);
        menuPanel.add(btnParticipants);
        menuPanel.add(btnGenerateQR);
        menuPanel.add(btnVerifyEntry);
        menuPanel.add(btnAttendance);
        menuPanel.add(btnLogout);
        add(menuPanel, BorderLayout.CENTER);

        // ----- bottom: who is logged in -----
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIHelper.BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 15, 40));
        bottomPanel.add(UIHelper.createLabel("Logged in as: " + loggedInAdmin.getFullName()),
                BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);

        // ----- connect buttons with ActionListener -----
        btnCreateEvent.addActionListener(e -> new EventForm().setVisible(true));
        btnViewEvents.addActionListener(e -> new EventForm().setVisible(true));
        btnRegisterParticipant.addActionListener(e -> new ParticipantRegistration().setVisible(true));
        btnParticipants.addActionListener(e -> new ParticipantForm().setVisible(true));
        btnGenerateQR.addActionListener(e -> new TicketForm(null).setVisible(true));
        btnVerifyEntry.addActionListener(e -> new QRVerification().setVisible(true));
        btnAttendance.addActionListener(e -> new AttendanceForm().setVisible(true));
        btnLogout.addActionListener(e -> logout());
    }

    /** The "Live count" strip: event chooser + Registered / Entered / Remaining cards. */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIHelper.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 40, 0, 40));

        cmbStatsEvent = new JComboBox<>();
        cmbStatsEvent.setFont(UIHelper.LABEL_FONT);
        cmbStatsEvent.addActionListener(e -> {
            if (!loadingStatsEvents) {
                refreshStats();
            }
        });
        btnRefreshStats = UIHelper.createButton("Refresh", UIHelper.PRIMARY);
        btnRefreshStats.setPreferredSize(new Dimension(100, 30));
        btnRefreshStats.addActionListener(e -> loadStatsEvents());

        JPanel selectRow = new JPanel(new BorderLayout(10, 0));
        selectRow.setOpaque(false);
        selectRow.add(UIHelper.createLabel("Live count for:"), BorderLayout.WEST);
        selectRow.add(cmbStatsEvent, BorderLayout.CENTER);
        selectRow.add(btnRefreshStats, BorderLayout.EAST);

        lblRegistered = new JLabel("0", SwingConstants.CENTER);
        lblEntered = new JLabel("0", SwingConstants.CENTER);
        lblRemaining = new JLabel("0", SwingConstants.CENTER);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.add(createStatCard(lblRegistered, "Registered", UIHelper.PRIMARY));
        cards.add(createStatCard(lblEntered, "Entered", UIHelper.SUCCESS));
        cards.add(createStatCard(lblRemaining, "Remaining", UIHelper.WARNING));

        panel.add(selectRow, BorderLayout.NORTH);
        panel.add(cards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(JLabel valueLabel, String caption, Color color) {
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(color);
        JLabel captionLabel = new JLabel(caption, SwingConstants.CENTER);
        captionLabel.setFont(UIHelper.LABEL_FONT);
        captionLabel.setForeground(UIHelper.GRAY);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 218)),
                BorderFactory.createEmptyBorder(8, 4, 8, 4)));
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(captionLabel, BorderLayout.SOUTH);
        return card;
    }

    /** Fills the event chooser ("All events" + every event) and keeps the current choice. */
    private void loadStatsEvents() {
        int keepEventId = 0;
        Event current = (Event) cmbStatsEvent.getSelectedItem();
        if (current != null) {
            keepEventId = current.getEventId();
        }
        loadingStatsEvents = true;
        try {
            cmbStatsEvent.removeAllItems();
            cmbStatsEvent.addItem(Event.allEvents());
            for (Event ev : eventDAO.getAllEvents()) {
                cmbStatsEvent.addItem(ev);
            }
            for (int i = 0; i < cmbStatsEvent.getItemCount(); i++) {
                if (cmbStatsEvent.getItemAt(i).getEventId() == keepEventId) {
                    cmbStatsEvent.setSelectedIndex(i);
                    break;
                }
            }
        } catch (SQLException ex) {
            // shown as "-" by refreshStats(); no popup because this also runs on a timer
        } finally {
            loadingStatsEvents = false;
        }
        refreshStats();
    }

    private void refreshStats() {
        Event selected = (Event) cmbStatsEvent.getSelectedItem();
        int eventId = selected == null ? 0 : selected.getEventId();
        try {
            int[] stats = attendanceDAO.getStats(eventId);
            lblRegistered.setText(String.valueOf(stats[0]));
            lblEntered.setText(String.valueOf(stats[1]));
            lblRemaining.setText(String.valueOf(stats[0] - stats[1]));
        } catch (SQLException ex) {
            lblRegistered.setText("-");
            lblEntered.setText("-");
            lblRemaining.setText("-");
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you really want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // close every other open window, then show the login screen again
            for (Window window : Window.getWindows()) {
                window.dispose();
            }
            new Login().setVisible(true);
        }
    }

    @Override
    public void dispose() {
        if (statsTimer != null) {
            statsTimer.stop();
        }
        super.dispose();
    }
}
