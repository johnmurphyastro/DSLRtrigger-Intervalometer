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
import info.johnmurphyastro.dslrtrigger.data.MinTime;
import java.util.Date;

/**
 * Calculates when the shutter button should be pressed and released.
 * Immutable class. Create a new instance for each shot.
 * @author John Murphy
 */
class IntervalometerCalc {
    /** Set from userData, open shutter lag in ms */
    private final int shutterOpenDelay;
    /** Set from userData, close shutter lag in ms */
    private final int shutterCloseDelay;
    /** Time the exposure will actually start in ms */
    private final long exposureStartTime;
    /** Button press time to start mirror lock in ms */
    private long mirrorLockButtonDownT;
    /** Mirror lock button release time (does not cancel mirror lock) in ms */
    private long mirrorLockButtonUpT;
    /** Exposure start button press time. Exposure starts after shutter open lag in ms. */
    private long shutterButtonDownT;
    /** Exposure end button release time. In bulb mode, exposure ends after shutter close lag in ms. */
    private long shutterButtonUpT;
    
    /**
     * @param data Intervalometer data
     * @param startAfterTime If in future, try to take first at this time
     */
    IntervalometerCalc(final IntervalometerData data, final long startAfterTime){
        int mirrorUpDuration = data.isMirrorLockSet() ? data.getMirrorUpDuration() : 0;
        
        shutterOpenDelay = data.getShutterOpenDelay();
        shutterCloseDelay = data.getShutterCloseDelay(); // only relevant in bulb mode

        // The requested start time rounded to the nearest second
        long startTime = ((startAfterTime + 500) / 1000) * 1000;
        calculateButtonTimes(startTime, data.getExposure(), mirrorUpDuration);
        
        long firstPressTime = Math.min(shutterButtonDownT, mirrorLockButtonDownT);
        long earliestPossibleTime = System.currentTimeMillis() + MinTime.BEFORE_SHOT_MS;
        if (firstPressTime < earliestPossibleTime){
            // first press was in the past, or too soon
            long delta = earliestPossibleTime - firstPressTime;
            // round delta upto the next whole second
            delta = ((delta / 1000) * 1000) + 1000;
            // Increase startTime by the required whole number of seconds
            startTime += delta;
            calculateButtonTimes(startTime, data.getExposure(), mirrorUpDuration);
        }
        exposureStartTime = startTime;
    }

    /**
     * Calculates shutterButtonDownT, shutterButtonUpT, mirrorLockButtonDownT, mirrorLockButtonUpT.
     * If the mirror lock / take exposure gets out of sync, the exposure will only be 
     * half the mirror lock time
     * 
     * All times are in milliseconds. 
     * @param startTime The exposure should start at this time
     * @param exposure The exposure length in milliseconds
     * @param mirrorUpDuration Duration of mirror lock up before the shot is taken
     */
    private void calculateButtonTimes(long startTime, int exposure, int mirrorUpDuration) {
        shutterButtonDownT = startTime - shutterOpenDelay;
        shutterButtonUpT = startTime + exposure - shutterCloseDelay;
        mirrorLockButtonDownT = shutterButtonDownT - mirrorUpDuration;
        // If mirror lock / exposure gets out of sink, you will know because the images will be half mirror lock time
        mirrorLockButtonUpT = mirrorLockButtonDownT + mirrorUpDuration / 2;
    }
    
    /**
     * @return The time the camera shutter is expected to open. This is after the button press.
     */
    long getStartT() {
        return exposureStartTime;
    }

    long getFirstButtonPressTime(){
        return mirrorLockButtonDownT;
    }
    
    /**
     * @return The number of milliseconds before the mirror lock button needs to be pressed.
     */
    long getMirrorLockButtonPressTime(){
        return mirrorLockButtonDownT;
    }
    
    /**
     * @return The number of milliseconds before the mirror lock button needs to be released.
     */
    long getMirrorLockButtonReleaseTime(){
        return mirrorLockButtonUpT;
    }
    
    /**
     * @return The number of milliseconds before the shutter button needs to be pressed.
     */
    long getShutterButtonPressTime(){
        return shutterButtonDownT;
    }
    
    /**
     * @return The number of milliseconds before the shutter button needs to be released.
     */
    long getShutterButtonReleaseTime(){
        return shutterButtonUpT;
    }

    /**
     * @return The time we believe that the shutter actually opened.
     */
    Date getExposureStartTime(long measuredButtonPressTime) {
        return new Date(measuredButtonPressTime + shutterOpenDelay);
    }
    
    /**
     * @return The time we believe that the shutter actually closed.
     */
    Date getExposureEndTime(long measuredButtonReleaseTime) {
        return new Date(measuredButtonReleaseTime + shutterCloseDelay);
    }
    
}
