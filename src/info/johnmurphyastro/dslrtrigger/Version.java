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
package info.johnmurphyastro.dslrtrigger;

import info.johnmurphyastro.dslrtrigger.data.MinTime;

/**
 * @author John Murphy
 */
public class Version {
    /** If true, don't try to filter out COM ports that are in use */
    public static final boolean USB_GPS = false;
    /** Version number (displayed in title and About dialog */
    public static final String VERSION = "3.0.3";
    private static Version version;

    private final String title;
    private final String helpMsg;
    
    private static final String ABOUT_MSG = 
              "DSLR Trigger is designed to control a DSLR camera to providing accurate\n"
            + "start and end exposure times\n\n"
            + "Copyright (C) 2018 - 2019  John Murphy \n"
            + "Email: johnmurphyastro.info@gmail.com\n"
            + "Website: johnmurphyastro.info\n\n"
 
            + "This program is free software: you can redistribute it and/or modify\n"
            + "it under the terms of the GNU General Public License as published by\n"
            + "the Free Software Foundation, either version 3 of the License, or\n"
            + "(at your option) any later version.\n\n"

            + "This program is distributed in the hope that it will be useful,\n"
            + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
            + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
            + "GNU General Public License for more details.\n\n"

            + "You should have received a copy of the GNU General Public License\n"
            + "along with this program.  If not, see <https://www.gnu.org/licenses/>.\n\n"
 
            + "COM port code:\n"
            + "RXTX binary builds provided as a courtesy of Fizzed, Inc. (http://fizzed.com/).\n"
            + "Please see http://fizzed.com/oss/rxtx-for-java for more information.";
    
    private static final String HELP_MSG_START = 
            "Web help pages are also available:\n" +
            "http://www.johnmurphyastro.info/software/dslr-trigger/dslr-trigger-help\n" +
            "http://www.johnmurphyastro.info/software/dslr-trigger/dslr-trigger-help/calibrate-shutter-close-lag\n\n" +
            "- 'Switch:' only lists COM ports that are available. Take a test shot to check it is the correct port.\n";
    
    private static final String HELP_GPS_U_BLOX7 = 
            "If you attempted to use the GPS U-blox7 virtual COM port you must restart DSLR Trigger (it confuses the third party libraries).\n";
    
    private static final String HELP_MSG_END = 
            "\n- Progress is displayed as (number of shots taken) / (total number to take.)\n\n" +
            
            "- The 'Start after:' row is initialized to the current time. If set to the future, after pressing 'Start',\n" +
            "the sequence will start at the specified time. If set to the past the sequence starts straight away.\n\n" +
            
            "- 'Exposure / s:' Set this to exposure length. Minimum value " + (MinTime.EXPOSURE_MS/1000.0) + " seconds. In Bulb mode this sets the exposure time.\n" +
            "In all other camera modes, this field's value must be equal or greater than the camera's shutter speed.\n\n" +
            
            "- 'Bulb mode' Select if the camera is in Bulb Mode. Bulb mode is strongly recommended.\n" +
            "It can improve exposure end time accuracy, and allows a sequence to be aborted without having to wait for a\n" +
            "long exposure to finish.\n\n" +
            
            "- 'Fire interval / s:' is the time from the start of the first shot to the start of the next shot. Note that if the camera\n" +
            "starts buffering images, the shutter lag may become inconsistent. Ensure that:\n" +
            "            'Fire interval' time    >    'Mirror Lock' time + 'Exposure' time + '" + (MinTime.BEFORE_SHOT_MS/1000.0) + " seconds'.\n\n" +
            
            "- 'Shots:' Set to the desired number of shots or set to a high number and use 'Stop' to end the sequence.\n\n" +
            
            "- 'Single test shot': if selected, the 'Start' button will take a single shot straight away.\n" + 
            "The 'Start after' and 'Shots' settings are then ignored.\n\n" +
            
            "- 'Mirror Lock' Tick this option if the camera is in mirror lock mode. Mirror lock can significantly reduce the\n" +
            "shutter open lag and make it more consistent. Look for Mirror Lock in the camera's custom menu.\n" +
            "- 'Mirror Lock / ms:' If the 'Mirror Lock' option is ticked, this field sets the number of seconds to flip the\n" +
            "mirror up before the shot is taken. Minimum mirror up time is " + (MinTime.MIRROR_MOVE_MS/1000.0) + " seconds.\n" +
            "The Mirror lock feature works with cameras that use the first button press to flip up the mirror and the second to take the shot.\n" +
            "Take care that the 'mirror up' / 'take shot' button presses do not get out of sync.\n" +
            "If 'Mirror Lock' is not ticked, the 'Mirror Lock / ms:' value is ignored.\n\n" +
            
            "- Log Folder: Use the '...' button to navigate to a new log file folder.\n" +
            "'Filename:' The log file prefix. For example 'RemoveDebris', 'ISS', 'DARK', 'BIAS'\n" +
            "Its only affect is to prefix the log file name. The log file also uses the current time to make the filename unique.\n\n" +
            
            "- 'Settings' Displays the 'DSLR Trigger Settings' dialog.\n" +
            "---- 'Shutter open delay compensation' Press the shutter button early by this number of ms to compensate for shutter lag.\n" +
            "---- 'Shutter close delay compensation' Release the shutter button early by this number of ms to compensate for shutter lag.\n" +
            "The 'Shutter close delay' only has an effect in Bulb mode.\n" +
            "---- 'Tab separated data' If selected, the log file columns will be separated with a tab instead of a comma character\n" +
            "---- 'Log all events' If selected, all button press and release times will be logged.\n\n" +
            "- Log file columns include:\n" +
            "    'ML press' Time the button was pressed; instructs DSLR to lock up mirror.\n" +
            "    'ML release' Time the button was released; mirror stays locked up.\n" +
            "    'SB press' Time the button was pressed; instructs DSLR to take an image.\n" +
            "    'SB release' Time the button was released; if in bulb mode, instruct DSLR to end exposure.\n" +
            "    'Start time' The exposure start time; equal to 'SB press' + 'Shutter open delay'.\n" +
            "    'End time' The exposure end time; equal to 'SB release' + 'Shutter close delay'.\n" +
            "    'Exposure' The exposure time; equal to 'End time' - 'Start time'.\n" +
            "    'Comment' BST / GMT, aborted image details.\n\n" +
            
            "DSLRtrigger trys to ensure that the exposure 'Start time' starts at an exact second.\n" +
            "To do this, the shutter button is pressed early (by 'Shutter open delay'). However, due to OS multitasking, there may be\n" +
            "a delay. The actual times the buttons are pressed are recorded in the log file.\n\n" +
            
            "Shutter lag can be reduced by using Mirror Lock and either 'Manual' or 'Bulb' mode, with auto focus off.\n\n" +
            
            "- FAQ:\n\n" +
            "(1) Why are all my exposures equal to half the mirror up time instead of the exposure time?\n" +
            "This can occur when using mirror lock if the 'mirror lock' / 'shutter button' presses have got out of sync.\n" +
            "The image will be taken at the 'ML press' time instead of the 'SB press' time, so the logged times will also be wrong.\n\n" +
            "(2) I asked for 5 shots, but 10 were taken. The odd number shots were incorrectly exposed for half the mirror lock time.\n" +
            "This can occur if DSLTrigger is set to use 'Mirror Lock' but 'Mirror Lock' has not been set on the camera.\n\n" +
            "(3) How can I take a series of shots with exposures less than " + (MinTime.EXPOSURE_MS/1000.0) + " seconds?\n" +
            "Set the camera to use 'Manual' instead of 'Bulb' mode. In DSLR Trigger, untick 'Bulb mode'.\n" +
            "Set the DSLR Trigger exposure to " + (MinTime.EXPOSURE_MS/1000.0) + " seconds. Set a shorter exposure on the camera.\n" +
            "Note that it is essential that the camera shutter speed is never longer than the exposure time set in DSLR Trigger.";
       
    private Version(){
        if (USB_GPS){
            title = "DSLR Trigger " + VERSION + " for USB GPS U-blox7";
            helpMsg = HELP_MSG_START + HELP_GPS_U_BLOX7 + HELP_MSG_END;
        } else {
            title = "DSLR Trigger Intervalometer " + VERSION;
            helpMsg = HELP_MSG_START + HELP_MSG_END;
        }
    }
    
    /**
     * @return Return the program title with version number
     */
    public static synchronized String getTitle(){
        if (version == null){
            version = new Version();
        }
        return version.title;
    }
    
    /**
     * @return Return the text for the About dialog
     */
    public static synchronized String getAboutMsg(){
        return ABOUT_MSG;
    }
    
    /**
     * @return return the text for the Help dialog
     */
    public static synchronized String getHelpMsg(){
        if (version == null){
            version = new Version();
        }
        return version.helpMsg;
    }
    
}
