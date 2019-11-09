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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import info.johnmurphyastro.dslrtrigger.Version;
import info.johnmurphyastro.dslrtrigger.intervalometer.IntervalometerController;
import javax.swing.JOptionPane;

/**
 * @author John Murphy
 */
public class DslrTriggerFrame extends javax.swing.JFrame {
    private static final long serialVersionUID = 1L;
    private info.johnmurphyastro.dslrtrigger.ui.DslrTriggerControlPanel controlPanel;

    /**
     * Creates new form DSLRtriggerFrame
     */
    public DslrTriggerFrame() {
        initComponents();
        // Set up window close action and give UI panel access to control class
        init();
    }
    
    private void initComponents() {
        controlPanel = new info.johnmurphyastro.dslrtrigger.ui.DslrTriggerControlPanel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Version.getTitle());
        getContentPane().add(controlPanel, java.awt.BorderLayout.CENTER);
        pack();
    }
    
    private void init() {
        try {
            final UiController uiController = new UiController();
            controlPanel.setUiController(uiController);
            uiController.init(controlPanel, new SettingsDialog(this, true));

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    uiController.exit();
                }
            });

            uiController.setDSLRtrigger(new IntervalometerController());
        } catch (Throwable t){
            ScrolledErrorPanel scrolledErrorPanel = new ScrolledErrorPanel();
            scrolledErrorPanel.setText(t.getLocalizedMessage());
            scrolledErrorPanel.appendText(t.toString());
            StackTraceElement[] stackTrace = t.getStackTrace();
            scrolledErrorPanel.appendText(stackTrace[0].toString());
            JOptionPane.showMessageDialog(controlPanel, scrolledErrorPanel, "DSLR Trigger Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
