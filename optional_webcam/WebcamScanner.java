package com.smarterevententrysystem.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.NotFoundException;
import com.smarterevententrysystem.util.QRGenerator;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * OPTIONAL webcam scanner. Needs the extra dependency from the guide (webcam-capture).
 * Usage:  String code = WebcamScanner.scan(this);   // returns null if cancelled
 */
public class WebcamScanner extends JDialog {

    private Webcam webcam;
    private WebcamPanel panel;
    private volatile boolean running = true;
    private volatile String result = null;

    private WebcamScanner(Frame owner) {
        super(owner, "Scan QR Code with Webcam - close this window to cancel", true);
        webcam = Webcam.getDefault();
        if (webcam == null) {
            return;
        }
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        panel = new WebcamPanel(webcam);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Thread scanThread = new Thread(this::scanLoop, "qr-scan-thread");
        scanThread.setDaemon(true);
        scanThread.start();
    }

    public static String scan(Frame owner) {
        WebcamScanner dialog = new WebcamScanner(owner);
        if (dialog.webcam == null) {
            JOptionPane.showMessageDialog(owner, "No webcam was found.", "Webcam", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        dialog.setVisible(true);   // blocks until the dialog closes
        return dialog.result;
    }

    private void scanLoop() {
        while (running) {
            try {
                if (webcam.isOpen()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        result = QRGenerator.decodeQR(image);
                        SwingUtilities.invokeLater(this::dispose);
                        return;
                    }
                }
                Thread.sleep(200);
            } catch (NotFoundException e) {
                // no QR code in this frame yet - keep trying
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    @Override
    public void dispose() {
        running = false;
        if (panel != null) {
            panel.stop();
        }
        if (webcam != null) {
            webcam.close();
        }
        super.dispose();
    }
}
