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
package info.johnmurphyastro.dslrtrigger.intervalometer;

import info.johnmurphyastro.dslrtrigger.MessageListener;
import info.johnmurphyastro.dslrtrigger.ProgressListener;
import info.johnmurphyastro.dslrtrigger.WaitTimeListener;
import info.johnmurphyastro.dslrtrigger.data.IntervalometerData;
import info.johnmurphyastro.dslrtrigger.data.MinTime;
import info.johnmurphyastro.dslrtrigger.usbswitch.UsbSwitch;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

/**
 * An intervalometer which instructs a DSLR to take a sequence of shots.
 * @author John Murphy
 */
class Intervalometer extends Thread {
    /** USB switch */
    private final UsbSwitch usbSwitch;
    private final IntervalometerData intervalometerData;
    private final ExposureReport report;
    /** Tells UI how many images have been taken */
    private final ProgressListener progressListener;
    /** Tells UI time before first shot of the sequence */
    private final WaitTimeListener waitTimeListener;
    private final MessageListener msgListener;

    /**
     * After construction, use Thread.start() base class method to run the sequence of shots.
     * To abort a sequence, interrupt the Intervalometer thread.
     * @param usbSwitch USB switch
     * @param userData Data entered by observer
     * @param listener Update UI with number of shots taken
     * @param waitTimeListener Update UI with time until sequence starts
     */
    Intervalometer(UsbSwitch usbSwitch, IntervalometerData intervalometerData,
            ExposureReport report, ProgressListener listener, 
            WaitTimeListener waitTimeListener, MessageListener msgListener) {
        this.usbSwitch = usbSwitch;
        this.intervalometerData = intervalometerData;
        this.report = report;
        this.progressListener = listener;
        this.waitTimeListener = waitTimeListener;
        this.msgListener = msgListener;
    }
    
    /**
     * Run the sequence of shots
     * Invoke via the Thread.start() base class method
     */
    @Override
    public void run() {
        try (BufferedWriter reportWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(report.getLogFile())))) {
            // If mirror up is being used, make the first mirror up at least 1000 ms to help ensure the camera wakes up
            IntervalometerCalc times = new IntervalometerCalc(intervalometerData, intervalometerData.getStartAfterTime());
            
            // Write the log file header information
            //ExposureReport report = new ExposureReport(reportWriter, intervalometerData);
            report.writeHeader(reportWriter,new Date(times.getStartT()));
            report.writeKey(reportWriter);
            report.writeColumnHeaders(reportWriter);

            // If the user has set a start after time, wait until a half second before this time
            // The actual shot will then be aligned with the second boundary
            try {
                // Show progress on GUI while we wait for first shot, until it is half second to go
                waitUntilTime(times.getFirstButtonPressTime() - 500, waitTimeListener);
            } catch (InterruptedException ex) {
                return;
            }
        
            // Take the shots
            for (int n = 0; n < intervalometerData.getNumberOfShots(); n++) {
                updateProgress(n);
                boolean pleaseStop = TakeImage(times, reportWriter);
                if (pleaseStop){
                    // The thread has been interupted; the user pressed stop or exit
                    return;
                }
                long nextTime = times.getStartT() + intervalometerData.getRepeatInterval();
                times = new IntervalometerCalc(intervalometerData, nextTime);
            }
        } catch (IOException ex) {
            msgListener.showErrorMessage("Failed to write to logfile:\n" + ex.getLocalizedMessage());
        }
    }
    
    /**
     * Take an image
     * @param times Calculated button press times
     * @param report Write the event times to this log
     * @param reportWriter Append to the log using this writer
     * @return If the thread has been cancelled, return true to indicate 'please stop'
     * @throws IOException 
     */
    private boolean TakeImage(IntervalometerCalc times, BufferedWriter reportWriter) throws IOException {
        boolean isMirrorLockButtonDown = false;
        boolean isShutterButtonDown = false;
        boolean isMirrorLockedUp = false;

        long mirrorLockButtonPressTimeMs = 0;
        long mirrorLockButtonReleaseTimeMs = 0;
        long shutterButtonPressTimeMs = 0;
        long shutterButtonReleaseTimeMs = 0;
        
        try {
            if (intervalometerData.isMirrorLockSet() && intervalometerData.getMirrorUpDuration() > 0) {
                // Flip the mirror up
                waitUntilTime(times.getMirrorLockButtonPressTime(), null);
                mirrorLockButtonPressTimeMs = usbSwitch.pressShutterButton();
                isMirrorLockButtonDown = true;
                isMirrorLockedUp = true;

                // Get ready for taking the shot (release the shutter button so we can press it again)
                // Note that the mirror stays locked up
                waitUntilTime(times.getMirrorLockButtonReleaseTime(), null);
                mirrorLockButtonReleaseTimeMs = usbSwitch.releaseShutterButton();
                isMirrorLockButtonDown = false;
            }

            // Take the shot
            waitUntilTime(times.getShutterButtonPressTime(), null);
            shutterButtonPressTimeMs = usbSwitch.pressShutterButton();
            // Write the shutter button press time to the log file
            Date exposureStartTime = times.getExposureStartTime(shutterButtonPressTimeMs);
            isMirrorLockedUp = false;
            isShutterButtonDown = true;

            // If in bulb mode, this ends the shot. 
            waitUntilTime(times.getShutterButtonReleaseTime(), null);
            shutterButtonReleaseTimeMs = usbSwitch.releaseShutterButton();
            Date exposureEndTime = intervalometerData.inBulbMode() ? times.getExposureEndTime(shutterButtonReleaseTimeMs): null;
            
            report.logExposureTime(reportWriter, exposureStartTime, exposureEndTime, 
                    mirrorLockButtonPressTimeMs, mirrorLockButtonReleaseTimeMs,
                    shutterButtonPressTimeMs, shutterButtonReleaseTimeMs);

        } catch (InterruptedException ex) {
            String comment = null;
            // If we were taking a shot we must finish it.
            // If we are using mirror lock, it is vital we don't get out of step
            if (isMirrorLockButtonDown) {
                // The mirror is probably already locked up. An extra wait makes sure.
                deepSleep(System.currentTimeMillis() + MinTime.MIRROR_MOVE_MS / 2);
                // Release the mirror lock button press. This leaves the mirror up.
                mirrorLockButtonReleaseTimeMs = usbSwitch.releaseShutterButton();
                isMirrorLockedUp = true;
            }
            if (isMirrorLockedUp){
                deepSleep(System.currentTimeMillis() + MinTime.MIRROR_MOVE_MS / 2);
                // We have flipped the mirror up, so we must take a shot
                // to avoid getting out of step
                shutterButtonPressTimeMs = usbSwitch.pressShutterButton();
                isShutterButtonDown = true;
                comment = "Cancelled. Exposure taken to cancel mirror lock";
            }
            
            if (isShutterButtonDown) {
                deepSleep(System.currentTimeMillis() + MinTime.BEFORE_SHOT_MS);
                // Finish taking the last shot
                shutterButtonReleaseTimeMs = usbSwitch.releaseShutterButton();
                if (!intervalometerData.inBulbMode()) {
                    // Releasing the shutter button has not stopped the exposure
                    // Hence wait until exposure ends
                    deepSleep(shutterButtonPressTimeMs + intervalometerData.getExposure());
                }
                if (comment == null){
                    comment = "Cancelled. Exposure had not finished";
                }
            }
            if (comment != null){
                Date exposureStartTime = times.getExposureStartTime(shutterButtonPressTimeMs);
                Date exposureEndTime = intervalometerData.inBulbMode() ? times.getExposureEndTime(shutterButtonReleaseTimeMs): null;
                report.logExposureTime(reportWriter, exposureStartTime, exposureEndTime,
                        mirrorLockButtonPressTimeMs, mirrorLockButtonReleaseTimeMs,
                        shutterButtonPressTimeMs, shutterButtonReleaseTimeMs, comment);
            }
            return true; // Interrupted (Stop or Exit)
        }
        return false; // Shot completed
    }

    /**
     * A sleep that should not be interrupted. If it is, this is an error.
     * @param sleep until this time
     */
    private void deepSleep(long time) {
        try {
            waitUntilTime(time, null);
        } catch (InterruptedException ex1) {
            msgListener.showErrorMessage("Alert, error occurred while cancelling exposure");
        }
    }
    
    /**
     * Sleep until the specified time.
     * @param time Sleep until this time
     * @param listener Tell UI number of seconds until wait is over
     * @throws InterruptedException 
     */
    static void waitUntilTime(long time, WaitTimeListener listener) throws InterruptedException{
        for (long delta = time - System.currentTimeMillis(); delta > 0; 
                delta = time - System.currentTimeMillis()){
            if (delta < 200){
                // Sleep is not very precise. Fine tune the end of the sleep
                Thread.sleep(1);
            } else if (delta < 1500){
                if (listener != null){
                    listener.setWaitTime(delta);
                }
                System.gc();
                Thread.sleep(100);
            } else {
                if (listener != null){
                    listener.setWaitTime(delta);
                }
                // Sleep for 1 second so we can give UI feedback every second
                System.gc();
                Thread.sleep(1000);
            }
        }
    }
    
    /**
     * Update UI with progress. eg 5 / 50
     *
     * @param i Number of shots taken
     */
    private synchronized void updateProgress(int i) {
        if (progressListener != null) {
            progressListener.setProgress(i);
        }
    }

}
