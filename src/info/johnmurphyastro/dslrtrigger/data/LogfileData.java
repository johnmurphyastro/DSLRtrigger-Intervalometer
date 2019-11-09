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
package info.johnmurphyastro.dslrtrigger.data;

import info.johnmurphyastro.dslrtrigger.InvalidDataException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author John Murphy
 */
public class LogfileData {
    private final File logFolder;
    private final String logFilenamePrefix;
    private final boolean tabSeparatedData;
    private final boolean logAllEvents;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy'y'LL'm'dd'd'_HH'h'mm'm'ss.SSS's'");
    
    /**
     * @param logFolder Create log file in this folder
     * @param logFilenamePrefix Log file prefix eg ISS or RD
     * @param tabSeparatedData If true use tab separator instead of comma
     * @param logAllEvents If true log all button press and release times
     * @throws InvalidDataException 
     */
    public LogfileData(File logFolder, String logFilenamePrefix,
            boolean tabSeparatedData, boolean logAllEvents) throws InvalidDataException{
        this.logFolder = logFolder;
        this.logFilenamePrefix = logFilenamePrefix;
        if (!logFolder.exists()) {
            throw new InvalidDataException("Log file folder does not exist");
        }
        this.tabSeparatedData = tabSeparatedData;
        this.logAllEvents = logAllEvents;
    }
    
    /**
     * Create a log File prefixed with the current time
     *
     * @return Log file
     */
    public File getLogFile() {
        String logFilename = sdf.format(new Date());
        return new File(logFolder, logFilenamePrefix + "_" + logFilename + ".txt");
    }
    
    /**
     * @return filename prefix eg ISS or RD
     */
    public String getFilenamePrefix() {
        return logFilenamePrefix;
    }
    
    /**
     * @return true if using tab separator, false if using comma
     */
    public boolean tabSeparatedData(){
        return tabSeparatedData;
    }
    
    /**
     * @return true if logging all button press and release events
     */
    public boolean logAllEvents(){
        return logAllEvents;
    }
}
