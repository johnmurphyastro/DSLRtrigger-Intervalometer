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

/**
 * Display a message to the user
 * @author John Murphy
 */
public interface MessageListener {
    /**
     * @param exception Display the exception message in an error dialog
     */
    public void showErrorMessage(Throwable exception);
    /**
     * @param message Show this message in an error dialog
     */
    public void showErrorMessage(String message);
    /**
     * @param message Message to show in an information dialog
     * @param title Information dialog title
     */
    public void showMessage(String message, String title);
}
