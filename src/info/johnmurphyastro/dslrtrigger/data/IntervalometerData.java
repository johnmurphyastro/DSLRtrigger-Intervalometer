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

/**
 * @author John Murphy
 */
public class IntervalometerData {
    // DSLR settings
    private final boolean isMirrorLockSet;
    private final boolean inBulbMode;
    
    // Camera calibration data
    private final int shutterOpenDelay;
    private final int shutterCloseDelay;
    
    // Intervalometer data
    private final long startAfterTime;
    private final int exposure;
    private final int mirrorUpDuration;
    private final int nExposures;
    private final int repeatInterval;
    
    /**
     * @param isMirrorLockSet Set to true if the camera is in mirror lock mode
     * @param inBulbMode Set to true if the camera is in bulb mode
     * @param shutterOpenDelayMs Shutter open lag in milliseconds
     * @param shutterCloseDelayMs Shutter close lag in milliseconds
     * @param startAfterTime Try to take the first shot at this time. Ignored if in past.
     * @param exposureSec Length of exposure in seconds
     * @param mirrorUpDurationSec Mirror up duration in seconds
     * @param nExposures Number of shots to take
     * @param repeatIntervalSec Time from the start of one shot to the start of the next one.
     * @throws InvalidDataException 
     */
    public IntervalometerData(
            final boolean isMirrorLockSet,
            final boolean inBulbMode,
            final int shutterOpenDelayMs,
            final int shutterCloseDelayMs,
            final long startAfterTime,
            final float exposureSec,
            final float mirrorUpDurationSec,
            final int nExposures,
            final int repeatIntervalSec
    ) throws InvalidDataException {
        this.isMirrorLockSet = isMirrorLockSet;
        this.inBulbMode = inBulbMode;
        
        this.shutterOpenDelay = shutterOpenDelayMs;
        if (inBulbMode){
            this.shutterCloseDelay = shutterCloseDelayMs;
        } else { // We only have control of the shutter close time in bulb mode
            this.shutterCloseDelay = 0;
        }
        
        this.startAfterTime = startAfterTime;
        this.exposure = (int) (1000 * exposureSec + 0.5);
        this.nExposures = nExposures;
        this.repeatInterval = 1000 * repeatIntervalSec;
        if (isMirrorLockSet){
            this.mirrorUpDuration = (int)(1000 * mirrorUpDurationSec + 0.5F);
        } else {
            this.mirrorUpDuration = 0;
        }
    }
    
    /**
     * @return True if mirror lock is set
     */
    public boolean isMirrorLockSet(){
        return isMirrorLockSet;
    }
    
    /**
     * @return True if in bulb mode
     */
    public boolean inBulbMode() {
        return inBulbMode;
    }
    
    /**
     * @return The shutter open lag in milliseconds
     */
    public int getShutterOpenDelay(){
        return shutterOpenDelay;
    }
    
    /**
     * @return The shutter close lag in milliseconds
     */
    public int getShutterCloseDelay(){
        return shutterCloseDelay;
    }
    
    /**
     * @return Try to take the first shot at this time. Ignored if in the past
     */
    public long getStartAfterTime() {
        return startAfterTime;
    }
    
    /**
     * @return The number of shots to take
     */
    public int getNumberOfShots() {
        return nExposures;
    }
    
    /**
     * @return Time in milliseconds from the start of one shot to the start of the next one
     */
    public int getRepeatInterval() {
        return repeatInterval;
    }

    /**
     * @return Time the mirror is locked up before the shot in milliseconds
     */    
    public int getMirrorUpDuration(){
        return mirrorUpDuration;
    }
    
    /**
     * @return The exposure time in milliseconds
     */
    public int getExposure(){
        return exposure;
    }
}
