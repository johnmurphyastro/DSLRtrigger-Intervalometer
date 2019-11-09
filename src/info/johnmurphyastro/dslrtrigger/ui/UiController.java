/*
 * DSLR Trigger is designed to control a DSLR camera to providing accurate
 * start and end exposure times
 * Copyright (C) 2018 - 2019  John Murphy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package info.johnmurphyastro.dslrtrigger.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import info.johnmurphyastro.dslrtrigger.InvalidDataException;
import info.johnmurphyastro.dslrtrigger.ProgressListener;
import info.johnmurphyastro.dslrtrigger.WaitTimeListener;
import info.johnmurphyastro.dslrtrigger.data.IntervalometerData;
import info.johnmurphyastro.dslrtrigger.data.LogfileData;
import info.johnmurphyastro.dslrtrigger.data.MinTime;
import info.johnmurphyastro.dslrtrigger.data.ObserverData;
import info.johnmurphyastro.dslrtrigger.intervalometer.IntervalometerController;

/**
 *
 * @author John Murphy
 */
class UiController implements ProgressListener, WaitTimeListener {
    private final static String LOG_FOLDER = "log_folder";
    private final static String FIRE_INTERVAL = "fire_interval";
    private final static String N_SHOTS = "n_shots";
    private final static String SHUTTER_DELAY = "shutter_delay";
    private final static String SHUTTER_CLOSE_DELAY = "shutter_close_delay";
    private final static String LOG_FILE_PREFIX = "log_file_prefix";
    private final static String EXPOSURE = "exposure";
    private final static String BULB_MODE = "bulb_mode";
    private final static String MIRROR_LOCK_FLAG = "mirror_lock_flag";
    private final static String MIRROR_LOCK = "mirror_lock_s";
    private final static String NAME = "name";
    private final static String EMAIL = "email";
    private final static String LOCATION = "location";
    private final static String CAMERA = "camera";
    private final static String LENS = "lens";
    private final static String COMMENT = "comment";
    private final static String USE_TABS = "use_tabs";
    private final static String LOG_ALL_EVENTS = "log_all_events";

    private DslrTriggerControlPanel ui;
    private final JFileChooser fileChooser = new JFileChooser();
    private File logFolder;

    private IntervalometerController intervalometerController;
    private Thread startButtonThread;
    private final static int HOURS_MS = 3_600_000;
    private final static int MINUTES_MS = 60_000;
    private static final int SECONDS_MS = 1_000;
    private final SimpleDateFormat stf = new SimpleDateFormat(" HH:mm:ss");
    private boolean showClock;
    private SettingsDialog settingsDialog;
    private CalibrateShutterDialog calibrateShutterDialog;
    
    private MessageDisplayer messageListener;
    
    /**
     * @return Shutter open lag in milliseconds
     */
    int getShutterOpenDelay() {
        return settingsDialog.getShutterOpenDelay();
    }

    /**
     * @return Shutter close lag in milliseconds
     */    
    int getShutterCloseDelay() {
        return settingsDialog.getShutterCloseDelay();
    }

    boolean isMirrorLockSet(){
        return ui.getMirrorLockFlag();
    }

    private int getNumberOfShots() {
        if (ui.testShotCheckbox.isSelected()){
            return 1;
        }
        return ui.getNumberOfShots();
    }
    
    String getComPort() {
        return (String) ui.serialPortCombo.getSelectedItem();
    }
    
    private ObserverData getObserverData(){
        return new ObserverData(
                settingsDialog.nameTextField.getText(),
                settingsDialog.emailTextField.getText(),
                settingsDialog.locationTextField.getText(),
                settingsDialog.cameraTextField.getText(),
                settingsDialog.lensTextField.getText(),
                settingsDialog.commentTextField.getText()
        );
    }
    
    private LogfileData getLogfileData() throws InvalidDataException{
        return new LogfileData(
                logFolder,
                ui.logFilenameTextfield.getText(),
                settingsDialog.tabSeparatedCheckBox.isSelected(),
                settingsDialog.logAllEventsCheckBox.isSelected()
        );
    }
    
    /**
     * Validates and returns the Intervalometer related data
     * @return Intervalometer data
     * @throws InvalidDataException 
     */
    private IntervalometerData getIntervalometerData() throws InvalidDataException{
        IntervalometerData ivData = new IntervalometerData(
                ui.getMirrorLockFlag(),
                ui.bulbCheckBox.isSelected(),
                settingsDialog.getShutterOpenDelay(),
                settingsDialog.getShutterCloseDelay(),
                getStartAfterTime(),
                ui.getExposure(),
                ui.getMirrorLockValue(), // mirror up duration
                getNumberOfShots(),
                ui.getFireInterval()
        );
        
        if (ivData.isMirrorLockSet() && ivData.getMirrorUpDuration() < MinTime.MIRROR_MOVE_MS) {
            throw new InvalidDataException(
                    "Minimum '" + ui.mirrorLockLabel.getText() + "' time is " + (MinTime.MIRROR_MOVE_MS / 1000.0) + " s");
        }
        if (ivData.getExposure() < MinTime.EXPOSURE_MS) {
            throw new InvalidDataException(
                    "Minimum '" + ui.exposureLabel.getText() + "' time is " + MinTime.EXPOSURE_MS / 1000.0 + " s");
        }
        if (ivData.getShutterCloseDelay() > ivData.getShutterOpenDelay()){
            throw new InvalidDataException("'Shutter close delay' must not be greater than the 'Shutter open delay'\n(Settings dialog)");
        }
        if (ivData.getRepeatInterval() < ivData.getMirrorUpDuration() + ivData.getExposure() + MinTime.BEFORE_SHOT_MS) {
            StringBuffer strBuf = new StringBuffer()
                    .append("Minimum '").append(ui.timeIntervalLabel.getText()).append("' time is \n")
                    .append("'").append(ui.exposureLabel.getText()).append("' + '");
            if (ivData.isMirrorLockSet()){
                strBuf.append(ui.mirrorLockLabel.getText()).append("' + '");
            }
            strBuf.append(MinTime.BEFORE_SHOT_MS).append(" ms'");
            throw new InvalidDataException(strBuf.toString());
        }
        
        return ivData;
    }
    
    /**
     * @param delay Shutter open lag in milliseconds
     */
    void setShutterOpenDelay(int delay) {
        settingsDialog.setShutterOpenDelay(delay);
    }
    
    /**
     * @param delay Shutter close lag in milliseconds
     */
    void setShutterCloseDelay(int delay) {
        settingsDialog.setShutterCloseDelay(delay);
    }

    @Override
    public void setProgress(final int completed) {
        java.awt.EventQueue.invokeLater(() -> {
            ui.progressTextfield.setText(""
                    + completed + " / " + getNumberOfShots());
        });
    }

    @Override
    public void setWaitTime(long waitTime) {
        java.awt.EventQueue.invokeLater(() -> {
            UiController.this.updateWaitTime(waitTime);
        });
    }

    /**
     * Displays wait time. Example format: "0h 11m 43s"
     * @param waitTime Wait time in milliseconds
     */
    private void updateWaitTime(long waitTime) {
        long hours = waitTime / HOURS_MS;
        long remain = waitTime - hours * HOURS_MS;
        long minutes = remain / MINUTES_MS;
        remain -= minutes * MINUTES_MS;
        long seconds = remain / SECONDS_MS;

        StringBuilder sBuf = new StringBuilder();
        sBuf.append(hours).append("h ").append(minutes).append("m ").append(seconds).append('s');

        ui.progressTextfield.setText(sBuf.toString());
    }

    private void startClock() {
        showClock = true;
        Thread clockThread = new Thread("Clock Thread") {
            @Override
            public void run() {
                while (showClock) {
                    updateClock();
                }
            }
        };
        clockThread.start();
    }

    /**
     * Update clock display. Format: " HH:mm:ss"
     */
    private void updateClock() {
        java.awt.EventQueue.invokeLater(() -> {
            if (showClock) {
                ui.progressTextfield.setText(stf.format(new Date()));
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            showClock = false;
        }
    }

    /**
     * Convert Start After Time from a date and time to milliseconds since epoch
     * @return Time in milliseconds
     * @throws InvalidDataException 
     */
    private long getStartAfterTime() throws InvalidDataException {
        long startAfterTime;
        int year = ui.getYear();
        int month = ui.getMonth() - 1;
        int day = ui.getDay();
        int hour = ui.getHour();
        int minute = ui.getMinute();
        int second = ui.getSeconds();
        Calendar calendarTime = new GregorianCalendar();
        calendarTime.set(year, month, day, hour, minute, second);
        startAfterTime = calendarTime.getTimeInMillis();

        if (ui.testShotCheckbox.isSelected()) {
            return System.currentTimeMillis();
        } else {
            return startAfterTime;
        }
    }
    
    /**
     * Add one minute to the 'Start After Time' text fields
     */
    void plus1minute() {
        int year = ui.getYear();
        int month = ui.getMonth() - 1;
        int day = ui.getDay();
        int hour = ui.getHour();
        int minute = ui.getMinute();
        int second = ui.getSeconds();
        Calendar calendarTime = new GregorianCalendar();
        calendarTime.set(year, month, day, hour, minute + 1, second);
        setStartAfterTime(calendarTime);
    }
    
    private void setStartAfterTime(Calendar calendar) {
        ui.setYear(calendar.get(Calendar.YEAR));
        ui.setMonth(calendar.get(Calendar.MONTH) + 1);
        ui.setDay(calendar.get(Calendar.DATE));
        ui.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        ui.setMinute(calendar.get(Calendar.MINUTE));
        ui.setSeconds(0);
    }

    /**
     * Initialise UI fields. Set the log folder to the user's Documents folder.
     * Set the start after time to now. Set progress to zero
     */
    void init(DslrTriggerControlPanel dslrControlPanel, SettingsDialog dialog) {
        ui = dslrControlPanel;
        messageListener = new MessageDisplayer(ui);
        settingsDialog = dialog;
        settingsDialog.setController(this);
        
        ui.stopButton.setEnabled(false);
        File docFolder = new File(System.getProperty("user.home"), "Documents");
        logFolder = new File(docFolder, "DSLRtrigger");

        Calendar calendar = new GregorianCalendar();
        setStartAfterTime(calendar);

        setProgress(0);
        setWaitTime(0);

        loadValues();

        if (!logFolder.exists()) {
            if (!logFolder.mkdir()) {
                // Failed to create IntervalometerController folder. Use Documents folder instead
                logFolder = docFolder;
            }
        }

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(logFolder);

        ui.logFolderTextfield.setText(logFolder.getPath());

        this.startClock();
    }

    /**
     * @param intervalometerController Provides UI access to the application control class
     */
    void setDSLRtrigger(IntervalometerController intervalometerController) {
        this.intervalometerController = intervalometerController;
        setComPorts(intervalometerController.getUSBSerialPortNames());
    }

    /**
     * @param comPorts Populate combo box with list of COM port names
     */
    private void setComPorts(String[] comPorts) {
        ui.serialPortCombo.setModel(new javax.swing.DefaultComboBoxModel<>(comPorts));
    }

    void startButtonAction() {
        ui.enableUi(false);
        showClock = false;
        startButtonThread = new Thread("Start Button Thread") {
            @Override
            public void run() {
                try {
                    intervalometerController.start(getComPort(), getIntervalometerData(), 
                            getObserverData(), getLogfileData(),
                            UiController.this, UiController.this, messageListener);
                } catch (InvalidDataException ex) {
                    messageListener.showErrorMessage(ex);
                }
                java.awt.EventQueue.invokeLater(() -> {
                    ui.enableUi(true);
                    startClock();
                });
            }
        };
        startButtonThread.start();
    }

    void stopButtonAction() {
        ui.stopButton.setEnabled(false);
        Thread thread = new Thread("Stop Button Thread") {
            @Override
            public void run() {
                intervalometerController.stop(messageListener);
                java.awt.EventQueue.invokeLater(() -> {
                    UiController.this.setWaitTime(0);
                    ui.enableUi(true);
                    startClock();
                });
            }
        };
        thread.start();
    }

    void exitButtonAction() {
        ui.exitButton.setEnabled(false);
        Thread thread = new Thread("Exit Button Thread") {
            @Override
            public void run() {
                exit();
            }
        };
        thread.start();
    }
    
    void exit(){
        saveValues();
        try {
            intervalometerController.exit(messageListener);
        } catch (Throwable t){
            System.exit(1);
        }
    }

    void chooseLogFolderAction() {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(ui)) {
            logFolder = fileChooser.getSelectedFile();
            ui.logFolderTextfield.setText(logFolder.getPath());
        }
    }
    
    void showSettingsDialog() {
        settingsDialog.setLocationRelativeTo(ui);
        settingsDialog.setVisible(true);
    }
    
    void showShutterOpenDelayDialog(){
        if (calibrateShutterDialog == null) {
            calibrateShutterDialog = new CalibrateShutterDialog(null, true);
        }
        calibrateShutterDialog.setController(
                new CalibrateShutterDialogOpenLagImpl(calibrateShutterDialog, this));
        calibrateShutterDialog.validate();
        calibrateShutterDialog.setLocationRelativeTo(ui);
        calibrateShutterDialog.setVisible(true);
    }
    
    void takeShutterOpenCalibrationShot(int shutterOpenDelay, boolean mirrorLock){
        intervalometerController.takeShutterOpenCalibrationShot(getComPort(), 
                shutterOpenDelay, mirrorLock, messageListener);
    }
    
    void updateSettings() {
        saveValues();
    }

    /**
     * Save user values to registry.
     */
    private void saveValues() {
        Preferences userPref = Preferences.userNodeForPackage(this.getClass());
        userPref.put(LOG_FOLDER, ui.logFolderTextfield.getText());
        userPref.put(LOG_FILE_PREFIX, ui.logFilenameTextfield.getText());
        userPref.putInt(FIRE_INTERVAL, ui.getFireInterval());
        userPref.putInt(N_SHOTS, ui.getNumberOfShots());
        userPref.putFloat(EXPOSURE, ui.getExposure());
        userPref.putFloat(MIRROR_LOCK, ui.getMirrorLockValue());
        userPref.putBoolean(MIRROR_LOCK_FLAG, ui.getMirrorLockFlag());
        userPref.putBoolean(BULB_MODE, ui.bulbCheckBox.isSelected());
        
        userPref.putInt(SHUTTER_DELAY, settingsDialog.getShutterOpenDelay());
        userPref.putInt(SHUTTER_CLOSE_DELAY, settingsDialog.getShutterCloseDelay());
        
        userPref.put(NAME, settingsDialog.nameTextField.getText());
        userPref.put(EMAIL, settingsDialog.emailTextField.getText());
        userPref.put(LOCATION, settingsDialog.locationTextField.getText());
        userPref.put(CAMERA, settingsDialog.cameraTextField.getText());
        userPref.put(LENS, settingsDialog.lensTextField.getText());
        userPref.put(COMMENT, settingsDialog.commentTextField.getText());
        userPref.putBoolean(USE_TABS, settingsDialog.tabSeparatedCheckBox.isSelected());
        userPref.putBoolean(LOG_ALL_EVENTS, settingsDialog.logAllEventsCheckBox.isSelected());
    }

    /** 
     * Load values from registry or from supplied defaults.
     */
    private void loadValues() {
        Preferences userPref = Preferences.userNodeForPackage(this.getClass());
        String logFilePath = userPref.get(LOG_FOLDER, logFolder.getPath());
        
        this.logFolder = new File(logFilePath);
        
        ui.logFilenameTextfield.setText(userPref.get(LOG_FILE_PREFIX, "RemoveDebris"));
        ui.setFireInterval(userPref.getInt(FIRE_INTERVAL, 5));
        ui.setNumberOfShots(userPref.getInt(N_SHOTS, 999));
        ui.setExposure(userPref.getFloat(EXPOSURE, 1.0F));
        ui.setMirrorLockValue(userPref.getFloat(MIRROR_LOCK, 0.5F));
        ui.setMirrorLockFlag(userPref.getBoolean(MIRROR_LOCK_FLAG, true));
        ui.bulbCheckBox.setSelected(userPref.getBoolean(BULB_MODE, true));
        
        // Canon 5Dmk2 requires 75ms open and 30ms close shutter compensation to match Lodestar / SXcon
        // The Lodestar might be 10 or 20 ms late, so these are minimum values.
        settingsDialog.setShutterOpenDelay(userPref.getInt(SHUTTER_DELAY, 75));
        settingsDialog.setShutterCloseDelay(userPref.getInt(SHUTTER_CLOSE_DELAY, 30));
        
        settingsDialog.nameTextField.setText(userPref.get(NAME, "John Murphy"));
        settingsDialog.emailTextField.setText(userPref.get(EMAIL, "email@gmail.com"));
        settingsDialog.locationTextField.setText(userPref.get(LOCATION, "W 000 00.000' / N 51 00.000' / 82 m"));
        settingsDialog.cameraTextField.setText(userPref.get(CAMERA, "Canon 300D"));
        settingsDialog.lensTextField.setText(userPref.get(LENS, "50mm F1.8"));
        settingsDialog.commentTextField.setText(userPref.get(COMMENT, "BST"));
        settingsDialog.tabSeparatedCheckBox.setSelected(userPref.getBoolean(USE_TABS, false));
        settingsDialog.logAllEventsCheckBox.setSelected(userPref.getBoolean(LOG_ALL_EVENTS, false));
    }
}
