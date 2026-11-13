# Update guide: new features (v2)

## What is new
| Feature | Where you see it |
|---|---|
| Live count (Registered / Entered / Remaining) | Top of the Dashboard; pick an event or "All events"; refreshes every 5 seconds |
| Participants list + search + delete | Dashboard -> **Participants List** |
| Event-wise attendance report (CSV, opens in Excel) | Dashboard -> **Attendance** -> choose event -> **Export CSV** |
| Absentee report (who did NOT come) | **Participants List** -> choose event -> **Export CSV** (column "Entry Status") |
| Email QR ticket | **Generate QR Ticket** window -> **Email QR Ticket** |
| BCrypt passwords | Automatic. Old SHA-256 passwords are upgraded to BCrypt at the next successful login |
| Login attempt limit | 3 wrong passwords lock that username for 30 seconds |

## How to update your NetBeans project (step by step)
1. Close the running app. Extract `SmartEventEntrySystem_v2.zip` to a NEW folder (for example `C:\Projects\v2`).
2. Open your current project in File Explorer. Go to `src\main\java\com\smarterevententrysystem`.
3. From the v2 folder copy these folders: `dao`, `model`, `ui`, `util`. Paste them into your project's `smarterevententrysystem` folder and choose **Replace the files**.
4. **Do NOT copy `database\DatabaseConnection.java`** and leave your `database` folder alone, because your MySQL password is written in it.
5. Replace your project's `pom.xml` with the v2 `pom.xml` (it only adds three dependencies: jbcrypt and Jakarta Mail).
6. In NetBeans: right-click the project -> **Reload POM**, then **Clean and Build** (internet needed). Wait for BUILD SUCCESS.
7. Right-click the project -> **Run** (not "Run File").
No database change is needed. Your existing data stays.

## Email setup (one time, only if you want the email feature)
1. Use a Gmail account. Turn on **2-Step Verification** in your Google account.
2. Open https://myaccount.google.com/apppasswords and create an **App Password** (16 letters).
3. Open `util/EmailConfig.java` and replace the two placeholders:
   `SMTP_USER = "yourname@gmail.com"` and `SMTP_APP_PASSWORD = "abcdefghijklmnop"` (no spaces).
4. Save, run the project, open **Generate QR Ticket**, choose a ticket, click **Email QR Ticket**, confirm the address.
The mail contains the QR picture inside it, plus the ticket details. Until you edit EmailConfig, the button only shows "Email is not set up yet", and nothing else is affected.

## Test table
| # | Do this | Expected |
|---|---|---|
| 1 | Log in, look at the Dashboard top | Three cards: Registered, Entered, Remaining |
| 2 | Verify a ticket in Scan QR, then look at Dashboard | Entered +1, Remaining -1 within 5 seconds (or press Refresh) |
| 3 | Dashboard -> Participants List, type `rahul` | Only matching rows; clear with Clear |
| 4 | Pick an event in the Event box | Only that event's participants |
| 5 | Select a row -> Delete Selected -> Yes | Row disappears; their ticket is deleted too |
| 6 | Attendance -> choose an event -> Export CSV | A .csv file; open it in Excel |
| 7 | Log out; type a wrong password 3 times | Message shows attempts left; after the 3rd: locked 30 seconds |
| 8 | Log in correctly | Works. In Workbench: `SELECT password_hash FROM admins;` now starts with `$2a$` |
| 9 | Email QR Ticket with EmailConfig edited | "Ticket sent to ..." and the mail arrives (check Spam) |

## Viva additions
- **Live count:** one SQL query joins participants, tickets and attendance; `COUNT(DISTINCT participant_id)` = registered, `COUNT(attendance_id)` = entered; remaining = registered - entered. A Swing `Timer` refreshes it every 5 seconds.
- **BCrypt:** salted and deliberately slow, so guessing passwords is hard. SHA-256 alone is fast, so it is weaker. Old hashes are upgraded automatically at login.
- **Attempt limit:** `LoginAttemptTracker` counts wrong passwords per username and locks for 30 seconds after 3.
- **CSV export:** `CsvExporter` writes the JTable rows as UTF-8 CSV, with quotes escaped and formula-looking text neutralised, so Excel opens it safely.
- **Email:** `EmailSender` uses Jakarta Mail over SMTP (STARTTLS). It runs in a background `SwingWorker`, so the window never freezes.

## Honest limitations
- The email, BCrypt and mail libraries could not be downloaded in my test environment, so I compile-checked the code against stand-ins. Please run the test table above on your PC and send me any error.
- The login lock is kept in memory: closing and reopening the app resets it. It is also per username, not per computer.
- The Gmail App Password sits in the source file `EmailConfig.java`. Never share that file or upload it publicly.
- Gmail may limit how many emails you can send per day.
