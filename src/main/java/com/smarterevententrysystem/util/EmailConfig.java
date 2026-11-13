package com.smarterevententrysystem.util;

/**
 * Email settings for sending QR tickets.  >>> EDIT THE TWO LINES BELOW <<<
 *
 * Gmail setup (one time):
 *  1. Turn on 2-Step Verification in your Google account.
 *  2. Open https://myaccount.google.com/apppasswords and create an App Password.
 *  3. Paste your Gmail address and the 16-letter App Password below (no spaces).
 * Use the App Password, NOT your normal Gmail password.
 */
public class EmailConfig {

    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final int SMTP_PORT = 587;

    public static final String SMTP_USER = "your_email@gmail.com";          // <-- your Gmail address
    public static final String SMTP_APP_PASSWORD = "your_app_password";      // <-- your App Password

    private EmailConfig() { }

    /** False while the placeholder values above are still in place. */
    public static boolean isConfigured() {
        return !SMTP_USER.isBlank() && !SMTP_APP_PASSWORD.isBlank()
                && !SMTP_USER.equals("your_email@gmail.com")
                && !SMTP_APP_PASSWORD.equals("your_app_password");
    }
}
