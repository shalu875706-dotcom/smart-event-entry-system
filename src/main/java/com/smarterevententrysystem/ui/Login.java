package com.smarterevententrysystem.ui;

import com.smarterevententrysystem.dao.AdminDAO;
import com.smarterevententrysystem.model.Admin;
import com.smarterevententrysystem.util.LoginAttemptTracker;
import com.smarterevententrysystem.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** First screen of the application: the admin login. */
public class Login extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;

    private final AdminDAO adminDAO = new AdminDAO();

    public Login() {
        setTitle("Smart Event Entry System - Login");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(UIHelper.createHeader("SMART EVENT ENTRY SYSTEM"), BorderLayout.NORTH);

        // ----- form in the middle -----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));

        txtUsername = new JTextField(18);
        txtPassword = new JPasswordField(18);
        UIHelper.addFormRow(formPanel, 0, "Username:", txtUsername);
        UIHelper.addFormRow(formPanel, 1, "Password:", txtPassword);
        add(formPanel, BorderLayout.CENTER);

        // ----- buttons at the bottom -----
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        buttonPanel.setBackground(UIHelper.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 130, 35, 130));

        btnLogin = UIHelper.createButton("LOGIN", UIHelper.PRIMARY);
        btnExit = UIHelper.createButton("EXIT", UIHelper.DANGER);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        add(buttonPanel, BorderLayout.SOUTH);

        // ----- connect buttons with ActionListener -----
        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnLogin);   // pressing Enter = clicking LOGIN
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please enter both username and password.");
            return;
        }

        long waitSeconds = LoginAttemptTracker.secondsLeft(username);
        if (waitSeconds > 0) {
            UIHelper.showError(this, "Too many failed attempts.\nPlease wait " + waitSeconds
                    + " seconds and try again.");
            return;
        }

        try {
            Admin admin = adminDAO.login(username, password);
            if (admin != null) {
                LoginAttemptTracker.reset(username);
                JOptionPane.showMessageDialog(this, "Login Successful", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                new Dashboard(admin).setVisible(true);
                dispose();
            } else {
                int attemptsLeft = LoginAttemptTracker.recordFailure(username);
                String message = "Invalid Username or Password";
                if (attemptsLeft > 0) {
                    message += "\nAttempts left: " + attemptsLeft;
                } else {
                    message += "\nToo many failed attempts. Login is locked for "
                            + LoginAttemptTracker.LOCK_SECONDS + " seconds.";
                }
                UIHelper.showError(this, message);
                txtPassword.setText("");
            }
        } catch (SQLException ex) {
            UIHelper.showDatabaseError(this, ex);
        }
    }
}
