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

import info.johnmurphyastro.dslrtrigger.data.MinTime;
import javax.swing.JOptionPane;

/**
 * Used to configure CalibrateShutterDialog to measure shutter open lag
 * @author John Murphy
 */
class CalibrateShutterDialogOpenLagImpl implements CalibrateShutterDialogConfig {

    private final CalibrateShutterDialog calibrateShutterDialog;
    private final UiController uiController;
    
    CalibrateShutterDialogOpenLagImpl(CalibrateShutterDialog dialog, UiController controller){
        this.calibrateShutterDialog = dialog;
        this.uiController = controller;
    }
    
    private static final String HELP_MSG = 
            "The shutter open lag can be determined by photographing the LEDs on the USB relay switch.\n\n"
            + "Camera settings:\n"
            + "Image format: RAW only. Focus mode: Manual. Exposure mode: Manual. Shutter speed: 1/1000 or faster. "
            + "Aperture: the aperture you intend to use in the field. Mirror Lock: set if available. "
            + "Set ISO to ensure LEDs are visible.\n\n"
            + "Dialog settings:\n"
            + "Set Mirror Lock to match camera setting. "
            + "Set Shutter open delay to an estimage of the shutter open lag. 100 - 300 ms is a good starting point. "
            + "Press 'Test' to take a photo of the LEDs on the USB switch. This will take the shot and "
            + "illuminate the switch LED for the specified number of milliseconds.\n\n"
            + "Analysing the results:\n"
            + "If both the power and switch LED is visible, decrease the value.\n"
            + "If only the power LED is visible, increase the value.\n"
            + "Once you have the correct setting, both LEDs should be visible about half the time. Press 'Apply'.";

    @Override
    public String getTitle() {
        return "Calibrate shutter open delay";
    }

    @Override
    public String getHelpText() {
        return HELP_MSG;
    }

    @Override
    public String getShutterDelayLabel() {
        return "Shutter open delay (ms)";
    }

    @Override
    public boolean isMirrorLockOn() {
        return uiController.isMirrorLockSet();
    }

    @Override
    public int getShutterDelayValue() {
        int openShutterDelay = uiController.getShutterOpenDelay();
        if (openShutterDelay == 0){
            // It has not been set, so default to good starting value
            openShutterDelay = 100;
        } else if (openShutterDelay < MinTime.EXPOSURE_MS){
            // If too small, the camera might not fire
            openShutterDelay = MinTime.EXPOSURE_MS;
        }
        return openShutterDelay;
    }

    @Override
    public void testButtonAction() {
        Thread calibrate = new Thread("Calibrate shutter open thread") {
            @Override
            public void run() {
                int delay = calibrateShutterDialog.getShutterCalibrationDelay();
                if (validateShutterDelay(delay)){
                    calibrateShutterDialog.enableUi(false);
                    uiController.takeShutterOpenCalibrationShot(
                            delay, calibrateShutterDialog.isMirrorLockSelected());
                    java.awt.EventQueue.invokeLater(() -> {
                        calibrateShutterDialog.enableUi(true);
                    });
                }
            }
        };
        calibrate.start();
    }
    
    /**
     * @param delay Estimated shutter delay time. If this is too short the camera might not shoot.
     * @return True if the delay was greater or equal to 10 milliseconds
     */
    private boolean validateShutterDelay(int delay) {
        if (delay < MinTime.EXPOSURE_MS) {
            JOptionPane.showMessageDialog(calibrateShutterDialog,
                    "Minimum 'Shutter open delay' is " + MinTime.EXPOSURE_MS + " ms", "Shutter open delay calibration",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public void applyButtonAction() {
        int delay = calibrateShutterDialog.getShutterCalibrationDelay();
        int ok = JOptionPane.showConfirmDialog(calibrateShutterDialog, "Replace existing shutter open delay?",
                "Shutter open delay calibration", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            uiController.setShutterOpenDelay(delay);
        }
        calibrateShutterDialog.setVisible(false);
    }
    
}
