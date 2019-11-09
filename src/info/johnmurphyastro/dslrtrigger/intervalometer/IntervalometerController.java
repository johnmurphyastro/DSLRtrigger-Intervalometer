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

import info.johnmurphyastro.dslrtrigger.usbswitch.GetSerialPortExcepton;
import info.johnmurphyastro.dslrtrigger.usbswitch.InvalidSerialPortNameException;
import info.johnmurphyastro.dslrtrigger.usbswitch.SerialComPortsAvailable;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import info.johnmurphyastro.dslrtrigger.MessageListener;
import info.johnmurphyastro.dslrtrigger.ProgressListener;
import info.johnmurphyastro.dslrtrigger.WaitTimeListener;
import info.johnmurphyastro.dslrtrigger.data.IntervalometerData;
import info.johnmurphyastro.dslrtrigger.data.LogfileData;
import info.johnmurphyastro.dslrtrigger.data.ObserverData;
import info.johnmurphyastro.dslrtrigger.usbswitch.UsbSwitch;

/**
 * This controller class orchestrates the events
 *
 * @author John Murphy
 */
public class IntervalometerController {
    
    private final SerialComPortsAvailable availableSerialPorts = new SerialComPortsAvailable();
    private UsbSwitch usbSwitch;
    private Intervalometer intervalometer;

    private synchronized Intervalometer getIntervalometer() {
        return intervalometer;
    }

    private synchronized void setIntervalometer(Intervalometer intervalometer) {
        this.intervalometer = intervalometer;
    }

    /**
     * @return list of Serial COM port names
     */
    public String[] getUSBSerialPortNames() {
        return availableSerialPorts.getComPortNames();
    }
    
    /**
     * Start capturing images
     *
     * @param comPort
     * @param ivData
     * @param obsData
     * @param logData
     * @param listener Provide progress feedback to the user interface (number of shots taken)
     * @param waitTimeListener Provide count down time progress until first shot
     * @param msgListener
     */
    public void start(String comPort, IntervalometerData ivData, ObserverData obsData, LogfileData logData,
            ProgressListener listener, WaitTimeListener waitTimeListener, MessageListener msgListener) {
        try {
            if (null == comPort || comPort.trim().isEmpty()) {
                msgListener.showErrorMessage("No COM port specified");
                return;
            }

            try {
                // This might take a while, so do before waiting for the start time
                setUsbSerialPort(comPort);
            } catch (InvalidSerialPortNameException | PortInUseException | UnsupportedCommOperationException
                    | IOException | GetSerialPortExcepton ex) {
                msgListener.showErrorMessage(ex);
                return;
            }

            // The intervalometer runs in its own thread
            ExposureReport report = new ExposureReport(logData, obsData, ivData);
            Intervalometer iv = new Intervalometer(getUsbSwitch(), ivData,
                    report, listener, waitTimeListener, msgListener);
            iv.start();
            // The stop action will need to access the intervalometer inorder to stop it.
            setIntervalometer(iv);
            // Wait until either the sequence has finished or has been aborted
            iv.join();
            
        } catch (Throwable t) {
            msgListener.showErrorMessage(t);
        }
    }

    /**
     * Stop background thread, clean up and exit
     * @param msgListener Report error messages to user
     */
    public synchronized void exit(MessageListener msgListener) {
        // Abort any sequence currently in progress.
        // If the camera is not in bulb mode, we have to wait for the current
        // exposure to finish. In bulb mode we can stop the current exposure early.
        stop(msgListener);
        try {
            if (usbSwitch != null) {
                usbSwitch.close();
            }
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        System.exit(0);
    }

    /**
     * Abort any sequence of shots currently in progress.If necessary, cancel mirror lock.
     * If in bulb mode, the current shot can be finished early.
     * If not, wait for the current exposure to finish.
     * This method waits until the Intervalometer thread finishes.
     * @param msgListener Report error messages to user
     */
    public synchronized void stop(MessageListener msgListener) {
        try {
            Intervalometer iv = getIntervalometer();
            if (iv != null && iv.isAlive() && !iv.isInterrupted()){
                iv.interrupt();
                iv.join();
            }

            // The shutter button should already have been released, but no harm making sure.
            UsbSwitch usbSwitch = getUsbSwitch();
            if (usbSwitch != null) {
                try {
                    usbSwitch.releaseShutterButton();
                } catch (IOException ex) {
                    msgListener.showErrorMessage("Failed to release shutter button during Stop\n"
                            + ex.getLocalizedMessage());
                }
            }
        } catch (InterruptedException t) {
            msgListener.showErrorMessage(t);
        }
    }
    
    /**
     * Take a test shot to determine the DSLR shutter open lag
     * @param comPort The USB switch COM port name
     * @param shutterOpenDelay Estimated shutter open lag
     * @param mirrorLock True if the DSLR is in mirror lock mode
     * @param listener Report error messages to user
     */
    public synchronized void takeShutterOpenCalibrationShot( 
            String comPort, int shutterOpenDelay, boolean mirrorLock, MessageListener listener) {
        try {
            setUsbSerialPort(comPort);
            CalibrateShutterOpen calibrate = new CalibrateShutterOpen(getUsbSwitch());
            calibrate.takeCalibrationImage(shutterOpenDelay, mirrorLock, listener);
        } catch (PortInUseException | UnsupportedCommOperationException | GetSerialPortExcepton | InvalidSerialPortNameException | IOException ex) {
            listener.showErrorMessage("Calibrate shutter open delay\n" + ex.getLocalizedMessage());
        } 
    }
    
    /**
     * If the USB Serial Port is currently set up, do nothing. If usbSerialPort
     * is connected to a different port, close the serial port and construct a
     * new usbSerialPort If usbSerialPort is null, construct it.
     *
     * @param comPort
     * @throws PortInUseException
     * @throws UnsupportedCommOperationException
     * @throws IOException
     * @throws GetSerialPortExcepton
     */
    private synchronized void setUsbSerialPort(String comPort) throws PortInUseException, UnsupportedCommOperationException, IOException, GetSerialPortExcepton, InvalidSerialPortNameException {
        if (comPort == null || comPort.trim().isEmpty()) {
            throw new InvalidSerialPortNameException("COM port name is invalid");
        }
        if (usbSwitch != null && usbSwitch.getComPortName().equals(comPort)) {
            // reuse existing USB Serial Port
            return;
        }
        if (usbSwitch != null) {
            // Close the previously used Serial Port
            usbSwitch.close();
        }
        usbSwitch = new UsbSwitch(availableSerialPorts, comPort);
    }

    /**
     * @return returns USB Serial Port.
     */
    private synchronized UsbSwitch getUsbSwitch() {
        return usbSwitch;
    }
}