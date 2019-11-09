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

import info.johnmurphyastro.dslrtrigger.MessageListener;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Display error or information message in a dialog
 * @author John Murphy
 */
public class MessageDisplayer implements MessageListener {

    private final Component component;
    
    /**
     * @param component Dialog will be positioned relative to this component
     */
    public MessageDisplayer(Component component){
        this.component = component;
    }
    
    @Override
    public void showErrorMessage(Throwable exception) {
        showErrorMessage(exception.getLocalizedMessage());
    }
    
    @Override
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(component, message, "DSLR Trigger", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(component, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
}
