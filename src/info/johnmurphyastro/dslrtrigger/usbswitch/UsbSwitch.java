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
package info.johnmurphyastro.dslrtrigger.usbswitch;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

/**
 * USB switch the default communication baud rate: 9600 BPS USB switch
 * communication protocol Data 
 * (1) - startup logo (the default is 0xA0) Data
 * (2) - switch address code (the default is 0x01, identifies the first switch) Data
 * (3) - operation data (0x00 to "off", 0x01 to "on") Data 
 * (4) - check code 
 * For example: Open the USB switch: A0 01 01 A2
 *
 * @author John Murphy
 */
public class UsbSwitch {

    private static final byte CMD = (byte) 0xA0;
    private static final byte SWITCH1 = (byte) 0x01;
    private static final byte OFF = (byte) 0x00;
    private static final byte ON = (byte) 0x01;
    private static final byte OFF_CHECK = (byte) 0xA1;
    private static final byte ON_CHECK = (byte) 0xA2;
    private final byte[] onCmd = new byte[]{CMD, SWITCH1, ON, ON_CHECK};
    private final byte[] offCmd = new byte[]{CMD, SWITCH1, OFF, OFF_CHECK};

    private final String comPortName;
    private final SerialPort serialPort;
    private final OutputStream serialPortWriter;

    /**
     * @param available Available USB Serial Port IDs
     * @param comPortName User selected COM port name
     * @throws PortInUseException
     * @throws UnsupportedCommOperationException
     * @throws IOException
     * @throws GetSerialPortExcepton
     */
    public UsbSwitch(SerialComPortsAvailable available, String comPortName) throws PortInUseException, UnsupportedCommOperationException, IOException, GetSerialPortExcepton {
        this.comPortName = comPortName;
        serialPort = getSerialPort(available, comPortName);
        if (serialPort == null) {
            throw new GetSerialPortExcepton("Failed to get Serial Port " + comPortName);
        }
        serialPortWriter = serialPort.getOutputStream();
    }

    /**
     * @return Serial COM port name
     */
    public String getComPortName() {
        return comPortName;
    }

    /**
     * @return Command to close the USB switch
     */
    private byte[] switchOnCmd() {
        return onCmd;
    }

    /**
     * @return Command to open the USB switch
     */
    private byte[] switchOffCmd() {
        return offCmd;
    }

    /**
     * Close the Serial Port writer and then close the serial port.
     */
    public void close() {
        try {
            serialPortWriter.close();
        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        serialPort.close();
    }

    /**
     * Press the DSLR shutter button
     * @return time when the switch command returns
     * @throws IOException
     */
    public long pressShutterButton() throws IOException {
        serialPortWriter.write(switchOnCmd());
        return System.currentTimeMillis();
    }

    /**
     * Release the DSLR shutter button
     * @return time when the switch command returns
     * @throws IOException
     */
    public long releaseShutterButton() throws IOException {
        serialPortWriter.write(switchOffCmd());
        return System.currentTimeMillis();
    }

    /**
     * Return the specified Serial COM Port.
     *
     * @param serialComPortName Serial COM port name
     * @return Serial COM port
     * @throws PortInUseException
     * @throws UnsupportedCommOperationException
     */
    private SerialPort getSerialPort(SerialComPortsAvailable serialPortIds, String serialComPortName) throws PortInUseException, UnsupportedCommOperationException {
        HashSet<CommPortIdentifier> serialPorts = serialPortIds.getAvailableSerialPortIds();
        for (CommPortIdentifier pid : serialPorts) {
            if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL
                    && pid.getName().equals(serialComPortName)) {
                SerialPort port = (SerialPort) pid.open(SerialComPortsAvailable.COM_PORT_OWNER, 1000);
                if (port != null) {
                    port.setSerialPortParams(
                            9600,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                }
                return port;
            }
        }
        return null;
    }

}
