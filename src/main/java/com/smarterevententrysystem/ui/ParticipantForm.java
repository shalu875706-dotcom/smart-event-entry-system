package com.smarterevententrysystem.ui;

import com.smarterevententrysystem.dao.EventDAO;
import com.smarterevententrysystem.dao.ParticipantDAO;
import com.smarterevententrysystem.model.Event;
import com.smarterevententrysystem.model.Participant;
import com.smarterevententrysystem.util.CsvExporter;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/** List of all registered participants with search, event filter, delete and CSV export. */
public class ParticipantForm extends JFrame {

    private JTextField txtSearch;
    private JComboBox<Event> cmbEventFilter;
    private JButton btnSearch;
    private JButton btnClearSearch;
    private JButton btnDelete;
    private JButton btnExport;
    private JButton btnClose;
    private JLabel lblCount;
    private JTable tblParticipants;
    private DefaultTableModel tableModel;

    private final ParticipantDAO participantDAO = new ParticipantDAO();
    private final EventDAO eventDAO = new EventDAO();
    private boolean loading = true;   // true while the screen is being prepared

    public ParticipantForm() {
        setTitle("Participants List");
        setSize(1000, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadEventFilter();
        loading = false;
        loadParticipants();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ----- top: header + search row -----
        txtSearch = new JTextField(20);
        txtSearch.setFont(UIHelper.LABEL_FONT);
        cmbEventFilter = new JComboBox<>();
        cmbEventFilter.setFont(UIHelper.LABEL_FONT);
        btnSearch = UIHelper.createButton("Search", UIHelper.PRIMARY);
        btnClearSearch = UIHelper.createButton("Clear", UIHelper.GRAY);
        btnSearch.setPreferredSize(new java.awt.Dimension(100, 32));
        btnClearSearch.setPreferredSize(new java.awt.Dimension(100, 32));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        searchPanel.setBackground(UIHelper.BACKGROUND);
        searchPanel.add(UIHelper.createLabel("Search (name, email, phone, ticket):"));
        searchPanel.add(txtSearch);
        searchPanel.add(UIHelper.createLabel("Event:"));
        searchPanel.add(cmbEventFilter);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClearSearch);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(UIHelper.createHeader("PARTICIPANTS"), BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ----- table -----
        String[] columns = {"ID", "Name", "Email", "Phone", "Event", "Ticket ID", "Entry Status"};
        tableModel = UIHelper.createReadOnlyModel(columns);
        tblParticipants = new JTable(tableModel);
        UIHelper.styleTable(tblParticipants);
        JScrollPane scrollPane = new JScrollPane(tblParticipants);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(scrollPane, BorderLayout.CENTER);

        // ----- bottom -----
        lblCount = UIHelper.createLabel("Total: 0");
        btnDelete = UIHelper.createButton("Delete Selected", UIHelper.DANGER);
        btnExport = UIHelper.createButton("Export CSV", UIHelper.SUCCESS);
        btnClose = UIHelper.createButton("Close", UIHelper.GRAY);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(UIHelper.BACKGROUND);
        bottomPanel.add(lblCount);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnExport);
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        // ----- connect buttons with ActionListener -----
        btnSearch.addActionListener(e -> loadParticipants());
        btnClearSearch.addActionListener(e -> clearSearch());
        btnDelete.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> exportCsv());
        btnClose.addActionListener(e -> dispose());
        cmbEventFilter.addActionListener(e -> {
            if (!loading) {
                loadParticipants();
            }
        });
        // search while typing
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchWhileTyping(); }
            @Override
            public void removeUpdate(DocumentEvent e) { searchWhileTyping(); }
            @Override
            public void changedUpdate(DocumentEvent e) { searchWhileTyping(); }
        });
    }

    private void searchWhileTyping() {
        if (!loading) {
            loadParticipants();
        }
    }

    private void loadEventFilter() {
        try {
            cmbEventFilter.removeAllItems();
            cmbEventFilter.addItem(Event.allEvents());
            for (Event ev : eventDAO.getAllEvents()) {
                cmbEventFilter.addItem(ev);
            }
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void loadParticipants() {
        Event selected = (Event) cmbEventFilter.getSelectedItem();
        int eventId = selected == null ? 0 : selected.getEventId();
        try {
            List<Participant> list = participantDAO.search(txtSearch.getText().trim(), eventId);
            tableModel.setRowCount(0);
            for (Participant p : list) {
                tableModel.addRow(new Object[]{
                    p.getParticipantId(), p.getName(), p.getEmail(), p.getPhone(), p.getEventName(),
                    p.getTicketCode() == null ? "-" : p.getTicketCode(),
                    p.isAttended() ? "Entered" : "Not entered"
                });
            }
            lblCount.setText("Total: " + list.size());
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void clearSearch() {
        loading = true;
        txtSearch.setText("");
        if (cmbEventFilter.getItemCount() > 0) {
            cmbEventFilter.setSelectedIndex(0);
        }
        loading = false;
        loadParticipants();
    }

    private void deleteSelected() {
        int row = tblParticipants.getSelectedRow();
        if (row < 0) {
            UIHelper.showError(this, "Please click a participant in the table first.");
            return;
        }
        int participantId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String name = tableModel.getValueAt(row, 1).toString();
        boolean entered = "Entered".equals(tableModel.getValueAt(row, 6).toString());

        String message = "Delete " + name + "?\nTheir ticket will be deleted too.";
        if (entered) {
            message += "\nThis person has already entered, so their attendance record will also be removed.";
        }
        int choice = JOptionPane.showConfirmDialog(this, message, "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            participantDAO.deleteParticipant(participantId);
            UIHelper.showInfo(this, "Participant deleted.");
            loadParticipants();
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            UIHelper.showError(this, "There is no data to export.");
            return;
        }
        Event selected = (Event) cmbEventFilter.getSelectedItem();
        String name = (selected == null || selected.getEventId() == 0)
                ? "all_events" : selected.getEventName().replaceAll("[^A-Za-z0-9]+", "_");

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("participants_" + name + ".csv"));
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
