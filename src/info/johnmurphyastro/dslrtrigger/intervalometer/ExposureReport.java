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

import info.johnmurphyastro.dslrtrigger.data.IntervalometerData;
import info.johnmurphyastro.dslrtrigger.data.LogfileData;
import info.johnmurphyastro.dslrtrigger.data.ObserverData;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author John Murphy
 */
class ExposureReport {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'y'LL'm'dd'd'_HH'h'mm'm'ss's'");
    private final SimpleDateFormat imageTime = new SimpleDateFormat("HH.mm.ss.SSS");
    private final SimpleDateFormat observingDate = new SimpleDateFormat("dd MMM YYYY");
    private final ObserverData observerData;
    private final IntervalometerData intervalometerData;
    private final LogfileData logfileData;
    private final String colSep;
    private final int timeStrLength;
    private int nthEntry;
    private int EXPOSURE_LENGTH = 8;
    
    /**
     * @param logFileWriter Writes the log file
     * @param observerData
     * @param columnSeparator 
     */
    ExposureReport(LogfileData logfileData, ObserverData observerData, 
            IntervalometerData intervalometerData){
        this.logfileData = logfileData;
        this.observerData = observerData;
        this.intervalometerData = intervalometerData;
        colSep = logfileData.tabSeparatedData() ? "\t" : ", ";
        nthEntry = 0;
        timeStrLength = formatTime(new Date()).length();
    }
    
    File getLogFile(){
        return logfileData.getLogFile();
    }
    
    /**
     * Write the observer's details at the top of the log file
     * @param startTime The time of the first shot provides the report date
     * @throws IOException 
     */
    void writeHeader(BufferedWriter logFileWriter, Date startTime) throws IOException{
        logFileWriter.write(rightPadding("Name:", 10) + observerData.getName());
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Email:", 10) + observerData.getEmail());
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Date:", 10) + observingDate.format(startTime));
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Location:", 10) + observerData.getLocation());
        logFileWriter.newLine();
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("CCD/camera:", 20) + observerData.getCamera());
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Telescope/lens:", 20) + observerData.getLens());
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Image exposure (s):", 20) + (intervalometerData.getExposure() / 1000.0));
        logFileWriter.newLine();
        logFileWriter.write("Image link (if applicable):");
        logFileWriter.newLine();
        logFileWriter.newLine();
        logFileWriter.write("DSLRtrigger parameters");
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Shots:", 26) + intervalometerData.getNumberOfShots());
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Shutter open delay (ms):", 26) + intervalometerData.getShutterOpenDelay());
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Shutter close delay (ms):", 26) + intervalometerData.getShutterCloseDelay());
        logFileWriter.newLine();
        if (intervalometerData.isMirrorLockSet()) {
            logFileWriter.write(rightPadding("Mirror Lock (ms):", 26) + intervalometerData.getMirrorUpDuration());
        } else {
            logFileWriter.write(rightPadding("Mirror Lock:", 26) + "off");
        }
        logFileWriter.newLine();
        String bulbMode = intervalometerData.inBulbMode() ? "on" : "off";
        logFileWriter.write(rightPadding("Bulb mode:", 26) + bulbMode);
        logFileWriter.newLine();
        if (intervalometerData.inBulbMode()) {
            logFileWriter.write(rightPadding("Bulb Exposure (ms):", 26) + intervalometerData.getExposure());
        } else {
            logFileWriter.write(rightPadding("Exposure (ms):     ", 26) + intervalometerData.getExposure());
        }
        logFileWriter.newLine();
        logFileWriter.write(rightPadding("Fire interval (ms):", 26) + intervalometerData.getRepeatInterval());
        logFileWriter.newLine();
        logFileWriter.newLine();
    }
    
    void writeKey(BufferedWriter logFileWriter) throws IOException{
        final boolean allEvents = logfileData.logAllEvents();
        logFileWriter.write("Key");
        logFileWriter.newLine();
        if (allEvents){
            if (intervalometerData.isMirrorLockSet()){
                logFileWriter.write(rightPadding("ML press:", 12) + "Shutter button pressed; instructs DSLR to lock up mirror.");
                logFileWriter.newLine();
                logFileWriter.write(rightPadding("ML release:", 12) + "Shutter button released; mirror stays up, but the camera is now ready for the next shot.");
                logFileWriter.newLine();
            }
            logFileWriter.write(rightPadding("SB press:", 12) + "Shutter button pressed; instructs DSLR to take an image.)");
            logFileWriter.newLine();
            logFileWriter.write(rightPadding("SB release:", 12) + "Shutter button released; if in bulb mode, instruct DSLR to end exposure.");
            logFileWriter.newLine();
        }
        logFileWriter.write(rightPadding("Start time:", 12) + "The exposure start time; equal to 'Shutter button press time' + 'Shutter open delay'.");
        logFileWriter.newLine();
        if (intervalometerData.inBulbMode()){
            logFileWriter.write(rightPadding("End time:", 12) + "The exposure end time; equal to 'Shutter button release time' + 'Shutter close delay'.");
            logFileWriter.newLine();
        }
        logFileWriter.newLine();
    }
    
    void writeColumnHeaders(BufferedWriter logFileWriter) throws IOException {
        int filenameLength = createFilename(new Date()).length();
        final boolean allEvents = logfileData.logAllEvents();
        StringBuilder columnHeaders = new StringBuilder()
                .append("N")
                .append(colSep).append(rightPadding("Filename", filenameLength));
        if (allEvents) {
            if (intervalometerData.isMirrorLockSet()){
                columnHeaders
                    .append(colSep).append(rightPadding("ML press", timeStrLength))
                    .append(colSep).append(rightPadding("ML release", timeStrLength));
            }
            columnHeaders.append(colSep).append(rightPadding("SB press", timeStrLength));
            columnHeaders.append(colSep).append(rightPadding("SB release", timeStrLength));
        }
        columnHeaders.append(colSep).append(rightPadding("Start time", timeStrLength));
        if (intervalometerData.inBulbMode()){
            columnHeaders.append(colSep).append(rightPadding("End time", timeStrLength));
            columnHeaders.append(colSep).append(rightPadding("Exposure", EXPOSURE_LENGTH));
        }
        columnHeaders.append(colSep).append("Comment");

        logFileWriter.write(columnHeaders.toString());
        logFileWriter.newLine();
    }
    
    /**
     * Write an exposure time data row to the log file
     * @param n nth shot
     * @param filename Suggested filename (user will rename image saved by camera)
     * @param startTime
     * @param endTime
     * @param mirrorLockButtonPressTimeMs USB button press to start mirror lock time
     * @param mirrorLockButtonReleaseTimeMs USB button release time (mirror still locked up)
     * @param shutterButtonPressTimeMs USB button press time (tells camera to start exposure)
     * @param shutterButtonReleaseTimeMs USB button release time (tells camera to finish exposure)
     * @param comment User comment, or aborted shot information.
     * @throws IOException 
     */
    private void writeColumnData(BufferedWriter logFileWriter, int n,
            String filename, Date startTime, Date endTime,
            long mirrorLockButtonPressTimeMs, long mirrorLockButtonReleaseTimeMs,
            long shutterButtonPressTimeMs, long shutterButtonReleaseTimeMs,
            String comment) throws IOException {
        
        final boolean allEvents = logfileData.logAllEvents();
        StringBuilder columnData = new StringBuilder()
                .append(Integer.toString(n)).append(colSep)
                .append(filename);
        if (allEvents) {
            if (intervalometerData.isMirrorLockSet()){
                columnData
                    .append(colSep).append(formatTime(mirrorLockButtonPressTimeMs))
                    .append(colSep).append(formatTime(mirrorLockButtonReleaseTimeMs));
            }   
            columnData.append(colSep).append(formatTime(shutterButtonPressTimeMs));
            columnData.append(colSep).append(formatTime(shutterButtonReleaseTimeMs));
        }
        columnData.append(colSep).append(formatTime(startTime));
        if (intervalometerData.inBulbMode()){
            columnData.append(colSep).append(formatTime(endTime));
            columnData.append(colSep).append(String.format("%8d", endTime.getTime() - startTime.getTime()));
        }
        columnData.append(colSep).append(comment);

        logFileWriter.write(columnData.toString());
        logFileWriter.newLine();
    }
    
    /**
     * Write an exposure time data row to the log file
     * @param logFileWriter
     * @param startTime Time that the exposure actually started (shutterButtonPressTimeMs + shutter open lag)
     * @param endTime Time that the exposure actually finished (shutterButtonReleaseTimeMs + shutter close lag)
     * @param mirrorLockButtonPressTimeMs USB button press to start mirror lock time
     * @param mirrorLockButtonReleaseTimeMs USB button release time (mirror still locked up)
     * @param shutterButtonPressTimeMs USB button press time (tells camera to start exposure)
     * @param shutterButtonReleaseTimeMs USB button release time (tells camera to finish exposure)
     * @throws IOException 
     */
    void logExposureTime(final BufferedWriter logFileWriter, Date startTime, Date endTime,
            long mirrorLockButtonPressTimeMs, long mirrorLockButtonReleaseTimeMs,
            long shutterButtonPressTimeMs, long shutterButtonReleaseTimeMs) throws IOException {
        
        logExposureTime(logFileWriter, startTime, endTime,
            mirrorLockButtonPressTimeMs, mirrorLockButtonReleaseTimeMs,
            shutterButtonPressTimeMs, shutterButtonReleaseTimeMs, observerData.getComment());
    }
    
    /**
     * Write an exposure time data row to the log file
     * @param logFileWriter
     * @param startTime Time that the exposure actually started (shutterButtonPressTimeMs + shutter open lag)
     * @param endTime Time that the exposure actually finished (shutterButtonReleaseTimeMs + shutter close lag)
     * @param mirrorLockButtonPressTimeMs USB button press to start mirror lock time
     * @param mirrorLockButtonReleaseTimeMs USB button release time (mirror still locked up)
     * @param shutterButtonPressTimeMs USB button press time (tells camera to start exposure)
     * @param shutterButtonReleaseTimeMs USB button release time (tells camera to finish exposure)
     * @param comment User comment, or aborted shot information.
     * @throws IOException 
     */
    void logExposureTime(final BufferedWriter logFileWriter, Date startTime, Date endTime,
            long mirrorLockButtonPressTimeMs, long mirrorLockButtonReleaseTimeMs,
            long shutterButtonPressTimeMs, long shutterButtonReleaseTimeMs,
            String comment) throws IOException {
        
        nthEntry++;
        String filename = createFilename(startTime);
        
        writeColumnData(logFileWriter, nthEntry, filename, startTime, endTime, 
                mirrorLockButtonPressTimeMs, mirrorLockButtonReleaseTimeMs,
                shutterButtonPressTimeMs, shutterButtonReleaseTimeMs, comment);
    }

    /**
     * Pad the end of a string with blank spaces
     * @param str String to pad
     * @param num Pad until the string contains this number of characters
     * @return The padded string
     */
    static String rightPadding(String str, int num) {
        return String.format("%1$-" + num + "s", str);
    }
    
    /**
     * Create a formatted time in "HH.mm.ss.SSS" format
     * @param eventTime convert this time to a string
     * @return The time as a formatted string
     */
    String formatTime(long eventTime){
        if (eventTime == 0){
            return "00.00.00.000";
        }
        return formatTime(new Date(eventTime));
    }
    
    /**
     * Create a formatted time in "HH.mm.ss.SSS" format
     * @param eventTime convert this time to a string
     * @return The time as a formatted string
     */
    private String formatTime(Date eventTime) {
        if (eventTime == null){
            return "00.00.00.000";
        }
        String time = imageTime.format(eventTime);
        return time;
    }

    /**
     * Create the suggested filename for the image taken at startTime.
     * This will be used when writing the image times to the log file
     * The user will rename each image saved from the camera to the corresponding name
     * @param userData Data entered by observer
     * @param startTime The time the exposure actually started.
     * @return 
     */
    private String createFilename(Date startTime) {
        String filename = logfileData.getFilenamePrefix() + '_' + sdf.format(startTime);
        return filename;
    }
}
