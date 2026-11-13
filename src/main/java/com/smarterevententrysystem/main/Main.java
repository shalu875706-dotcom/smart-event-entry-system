package com.smarterevententrysystem.main;

import com.smarterevententrysystem.ui.Login;
import javax.swing.SwingUtilities;

/**
 * Starting point of the application. Opens the Login screen.
 */
public class Main {

    public static void main(String[] args) {
        // Swing screens must be created on the "Event Dispatch Thread"
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
