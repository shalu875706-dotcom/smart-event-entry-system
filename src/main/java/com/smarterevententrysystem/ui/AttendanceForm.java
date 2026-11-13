package com.smarterevententrysystem.ui;

import com.smarterevententrysystem.dao.AttendanceDAO;
import com.smarterevententrysystem.dao.EventDAO;
import com.smarterevententrysystem.model.Attendance;
import com.smarterevententrysystem.model.Event;
import com.smarterevententrysystem.util.CsvExporter;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Shows everybody who has entered, per event, and exports the report as CSV (opens in Excel). */
public class AttendanceForm extends JFrame {

    private JTable tblAttendance;
    private DefaultTableModel tableModel;
    private JComboBox<Event> cmbEventFilter;
    private JLabel lblTotal;
    private JButton btnRefresh;
    private JButton btnExport;
    private JButton btnClose;

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final EventDAO eventDAO = new EventDAO();
    private boolean loadingFilter = false;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AttendanceForm() {
        setTitle("Attendance");
        setSize(900, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadEventFilter();
        loadAttendance();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ----- top: header + event filter + export -----
        cmbEventFilter = new JComboBox<>();
        cmbEventFilter.setFont(UIHelper.LABEL_FONT);
        btnExport = UIHelper.createButton("Export CSV", UIHelper.SUCCESS);

        JPanel filterPanel = new JPanel(new BorderLayout(10, 0));
        filterPanel.setBackground(UIHelper.BACKGROUND);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 4, 15));
        filterPanel.add(UIHelper.createLabel("Event:"), BorderLayout.WEST);
        filterPanel.add(cmbEventFilter, BorderLayout.CENTER);
        filterPanel.add(btnExport, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(UIHelper.createHeader("ATTENDANCE"), BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ----- table -----
        String[] columns = {"Ticket ID", "Participant Name", "Event Name", "Entry Time", "Attendance Status"};
        tableModel = UIHelper.createReadOnlyModel(columns);
        tblAttendance = new JTable(tableModel);
        UIHelper.styleTable(tblAttendance);
        JScrollPane scrollPane = new JScrollPane(tblAttendance);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 15, 5, 15));
        add(scrollPane, BorderLayout.CENTER);

        // ----- bottom -----
        lblTotal = UIHelper.createLabel("Total entries: 0");
        btnRefresh = UIHelper.createButton("Refresh", UIHelper.PRIMARY);
        btnClose = UIHelper.createButton("Close", UIHelper.GRAY);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(UIHelper.BACKGROUND);
        bottomPanel.add(lblTotal);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        // ----- connect buttons with ActionListener -----
        cmbEventFilter.addActionListener(e -> {
            if (!loadingFilter) {
                loadAttendance();
            }
        });
        btnRefresh.addActionListener(e -> {
            loadEventFilter();
            loadAttendance();
        });
        btnExport.addActionListener(e -> exportCsv());
        btnClose.addActionListener(e -> dispose());

        // refresh automatically whenever this window is clicked/focused again
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                loadAttendance();
            }
        });
    }

    /** Fills the event chooser ("All events" + every event) and keeps the current choice. */
    private void loadEventFilter() {
        int keepEventId = 0;
        Event current = (Event) cmbEventFilter.getSelectedItem();
        if (current != null) {
            keepEventId = current.getEventId();
        }
        loadingFilter = true;
        try {
            cmbEventFilter.removeAllItems();
            cmbEventFilter.addItem(Event.allEvents());
            for (Event ev : eventDAO.getAllEvents()) {
                cmbEventFilter.addItem(ev);
            }
            for (int i = 0; i < cmbEventFilter.getItemCount(); i++) {
                if (cmbEventFilter.getItemAt(i).getEventId() == keepEventId) {
                    cmbEventFilter.setSelectedIndex(i);
                    break;
                }
            }
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        } finally {
            loadingFilter = false;
        }
    }

    private void loadAttendance() {
        Event selected = (Event) cmbEventFilter.getSelectedItem();
        int eventId = selected == null ? 0 : selected.getEventId();
        try {
            tableModel.setRowCount(0);
            List<Attendance> list = attendanceDAO.getAttendance(eventId);
            for (Attendance a : list) {
                tableModel.addRow(new Object[]{
                    a.getTicketCode(), a.getParticipantName(), a.getEventName(),
                    a.getEntryTime().format(TIME_FORMAT), a.getStatus()
                });
            }
            lblTotal.setText("Total entries: " + list.size());
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            UIHelper.showError(this, "There is no attendance data to export.");
            return;
        }
        Event selected = (Event) cmbEventFilter.getSelectedItem();
        String name = (selected == null || selected.getEventId() == 0)
                ? "all_events" : selected.getEventName().replaceAll("[^A-Za-z0-9]+", "_");

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("attendance_" + name + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        try {
            CsvExporter.export(file, tableModel);
            UIHelper.showInfo(this, "Report saved to:\n" + file.getAbsolutePath()
                    + "\n\nYou can open it with Excel.");
        } catch (IOException ex) {
            UIHelper.showError(this, "Could not save the report: " + ex.getMessage());
        }
    }
}
