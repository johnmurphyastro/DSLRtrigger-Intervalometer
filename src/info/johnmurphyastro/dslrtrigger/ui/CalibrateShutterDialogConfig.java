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

/**
 * Allows same dialog code to be used for shutter open delay and shutter close delay.
 * The dialog accesses the implementation of this class, and quires it for values
 * and actions.
 * @author John Murphy
 */
public interface CalibrateShutterDialogConfig {

    /**
     * @return The dialog calls this to determine the title to display
     */
    public String getTitle();

    /**
     * @return The dialog calls this to get the help text
     */
    public String getHelpText();

    /**
     * @return The dialog calls this to determine the initial shutter delay value
     */
    public String getShutterDelayLabel();

    /**
     * @return The dialog calls this to determine if the mirror lock toggle should be set
     */
    public boolean isMirrorLockOn();

    /**
     * @return Dialog calls this to determine the initial shutter (open / close) lag
     */
    public int getShutterDelayValue();

    /**
     * Action to perform when test button is pressed
     */
    public void testButtonAction();

    /**
     * Action to perform when apply button is pressed
     */
    public void applyButtonAction();
}
