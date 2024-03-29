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

import java.text.NumberFormat;
import javax.swing.text.NumberFormatter;
import javax.swing.JFormattedTextField;

/**
 * This dialog can be configured for calibrating open or close shutter lag
 * @author John Murphy
 */
class CalibrateShutterDialog extends javax.swing.JDialog {

    private CalibrateShutterDialogConfig calibrateShutterDialogConfig;

    /**
     * Creates new form CalibrateShutterDialog
     * @param parent
     * @param modal
     */
    CalibrateShutterDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    /**
     * Configures the dialog to measure shutter open lag or shutter close lag.
     * @param controller Use CalibrateShutterDialogOpenLagImpl or CalibrateShutterDialogCloseLagImpl
     */
    void setController(CalibrateShutterDialogConfig controller){
        this.calibrateShutterDialogConfig = controller;
        this.setTitle(controller.getTitle());
        this.helpTextArea.setText(controller.getHelpText());
        this.shutterDelayLabel.setText(controller.getShutterDelayLabel());
        this.mirrorLockCheckBox.setSelected(controller.isMirrorLockOn());
        this.shutterDelayTextfield.setValue(controller.getShutterDelayValue());
        this.pack();
    }
    
    /**
     * Text field will only be able to store valid positive integers
     * @return TextField positive integer formatter
     */
    private NumberFormatter getFormatter(){
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        return formatter;
    }
    
    /**
    * @return Integer value from shutter calibration delay text field
    */
    int getShutterCalibrationDelay(){
        Object obj = shutterDelayTextfield.getValue();
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }
    
    /**
     * @return True if 'Mirror lock' toggle is selected
     */
    boolean isMirrorLockSelected(){
        return mirrorLockCheckBox.isSelected();
    }
    
    void enableUi(boolean enable){
        applyButton.setEnabled(enable);
        cancelButton.setEnabled(enable);
        mirrorLockCheckBox.setEnabled(enable);
        shutterDelayTextfield.setEnabled(enable);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        helpTextArea = new javax.swing.JTextArea();
        testButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        shutterDelayLabel = new javax.swing.JLabel();
        shutterDelayTextfield = new JFormattedTextField(getFormatter());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        helpTextArea.setEditable(false);
        helpTextArea.setColumns(20);
        helpTextArea.setLineWrap(true);
        helpTextArea.setRows(5);
        helpTextArea.setToolTipText("");
        helpTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(helpTextArea);

        mirrorLockCheckBox.setText("Mirror Lock");

        testButton.setText("Test");
        testButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        shutterDelayLabel.setText("Shutter open delay (ms)");
        shutterDelayLabel.setToolTipText("");

        shutterDelayTextfield.setText("101");
        shutterDelayTextfield.setMinimumSize(new java.awt.Dimension(40, 30));
        shutterDelayTextfield.setPreferredSize(new java.awt.Dimension(40, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(shutterDelayLabel)
                        .addGap(18, 18, 18)
                        .addComponent(shutterDelayTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                        .addComponent(mirrorLockCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(testButton)
                        .addGap(18, 18, 18)
                        .addComponent(applyButton)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shutterDelayLabel)
                    .addComponent(mirrorLockCheckBox)
                    .addComponent(testButton)
                    .addComponent(applyButton)
                    .addComponent(cancelButton)
                    .addComponent(shutterDelayTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
        calibrateShutterDialogConfig.testButtonAction();
    }//GEN-LAST:event_testButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        calibrateShutterDialogConfig.applyButtonAction();
    }//GEN-LAST:event_applyButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextArea helpTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    final javax.swing.JCheckBox mirrorLockCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JLabel shutterDelayLabel;
    private javax.swing.JFormattedTextField shutterDelayTextfield;
    private javax.swing.JButton testButton;
    // End of variables declaration//GEN-END:variables
}
