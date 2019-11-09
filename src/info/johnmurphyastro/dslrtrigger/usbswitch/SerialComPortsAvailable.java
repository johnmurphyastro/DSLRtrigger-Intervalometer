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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import info.johnmurphyastro.dslrtrigger.Version;

/**
 * @author John Murphy
 */
public class SerialComPortsAvailable {
    /** COM port owner name */
    public static final String COM_PORT_OWNER = "DSLRtrigger";
    private HashSet<CommPortIdentifier> availableSerialPortIds;

    /**
     * @return Array of COM serial port names (eg COM3)
     */
    public String[] getComPortNames() {
        ArrayList<String> portNames = new ArrayList<>();
        HashSet<CommPortIdentifier> serialPorts = getAvailableSerialPortIds();
        for (CommPortIdentifier pid : serialPorts) {
            if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portNames.add(pid.getName());
            }
        }
        return portNames.toArray(new String[1]);
    }

    /**
     * @return A HashSet containing the CommPortIdentifier for all serial ports
     * that are not currently being used.
     */
    synchronized HashSet<CommPortIdentifier> getAvailableSerialPortIds() {
        if (availableSerialPortIds == null) {
            HashSet<CommPortIdentifier> serialPortIds = new HashSet<>();
            Enumeration<?> thePorts = CommPortIdentifier.getPortIdentifiers();
            while (thePorts.hasMoreElements()) {
                CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
                switch (com.getPortType()) {
                    case CommPortIdentifier.PORT_SERIAL:
                        if (Version.USB_GPS) {
                            // USB GPS U-blox7 create a virtual COM port that crashes the RXTXcomm library
                            // It is OK as long as we don't attempt to open this virtual COM port
                            serialPortIds.add(com);
                        } else {
                            try {
                                // Attempt to open each COM port to find out if it is in use.
                                // Add all COM ports that are not in use to our set.
                                CommPort thePort = com.open(COM_PORT_OWNER, 1000);
                                thePort.close();
                                serialPortIds.add(com);
                            } catch (PortInUseException ex) {
                                // Port is in use, so don't add it to the HashSet
                            }
                        }

                }
            }
            availableSerialPortIds = serialPortIds;
        }
        return availableSerialPortIds;
    }

}
