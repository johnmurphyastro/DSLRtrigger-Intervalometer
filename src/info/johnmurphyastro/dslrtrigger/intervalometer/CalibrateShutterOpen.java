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
import info.johnmurphyastro.dslrtrigger.data.MinTime;
import info.johnmurphyastro.dslrtrigger.usbswitch.UsbSwitch;
import java.io.IOException;

/**
 * Take an image of the USB Switch LED to determine shutter open time delay
 * @author John Murphy
 */
class CalibrateShutterOpen {

    private final UsbSwitch usbSwitch;

    /**
     * @param usbSwitch USB Switch
     */
    CalibrateShutterOpen(UsbSwitch usbSwitch) {
        this.usbSwitch = usbSwitch;
    }

    /**
     * Take a test shot to help determine the shutter open lag time.
     * The DSLR should be set to use a high shutter speed (eg 1/2000th sec)
     * The USB switch is pressed for shutterOpenDelay milliseconds. Hence the
     * switch's 'Pressed' LED will be illuminated during this time.
     * Due to the shutter open lag, the DSLR will not take the photo straight
     * away. If the captured image shows the 'Pressed' LED, the shutterOpenDelay
     * is too long. If not, the shutterOpenDelay is too short. The aim is to 
     * find the transition.
     * 
     * @param shutterOpenDelay Keep the switch pressed for this number of milliseconds
     * @param mirrorLock True if the camera is using mirror lock mode
     * @param listener Display messages to the user
     * @throws IOException 
     */
    void takeCalibrationImage(int shutterOpenDelay, boolean mirrorLock, MessageListener listener) throws IOException {
        if (mirrorLock){
            // The first press will flip up the mirror
            usbSwitch.pressShutterButton();
            try {
                // Make this first press long enough to ensure that the camera wakes up
                Thread.sleep(MinTime.MIRROR_MOVE_MS / 2);
            } catch (InterruptedException ex) {}
            // The mirror stays up when the button is released.
            usbSwitch.releaseShutterButton();
            try {
                // Wait enough time so that the camera detects the button release
                Thread.sleep(MinTime.MIRROR_MOVE_MS / 2);
            } catch (InterruptedException ex) {}
        }
        // Press the switch for shutterOpenDelay milliseconds
        long pressTime = usbSwitch.pressShutterButton();
        try {
            Intervalometer.waitUntilTime(pressTime + shutterOpenDelay, null);
        } catch (InterruptedException ex) {}
        long releaseTime = usbSwitch.releaseShutterButton();
        // Tell the user how long the button was actually pressed for.
        listener.showMessage(
                "LED was illuminated for " + (releaseTime - pressTime) + " ms",
                "Calibrate shutter open delay");
    }
}
