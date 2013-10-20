/*
 * All sample code contained herein is provided to you "AS IS" without any warranties of any kind.
 */
package ru.cg.runaex.wsdl_analyzer.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * This class provides a dialog that can show a raw response from a web service
 *
 * @author Jim Winfield
 */
public class ResponseDialog extends JDialog {
  // The UI components ...
  JPanel mainPanel = new JPanel();
  JLabel responseLabel = new JLabel();
  JTextArea responseText = new JTextArea();
  JScrollPane responseScrollPane = new JScrollPane();
  JButton disposeButton = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * Constructor
   */
  public ResponseDialog(Dialog owner) {
    super(owner);

    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initializes the user interface components
   *
   * @throws Exception
   */
  private void jbInit() throws Exception {
    // Initialize display properties
    mainPanel.setLayout(gridBagLayout1);
    responseLabel.setText("Response:");
    responseText.setBorder(BorderFactory.createEtchedBorder());
    responseText.setEditable(false);
    responseText.setText("");
    responseScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    responseScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    responseScrollPane.getViewport().add(responseText);
    disposeButton.setText("Done");

    // Color stuff ...
    responseText.setBackground(Color.white);
    responseText.setForeground(Color.black);

    // Layout...
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(responseLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(responseScrollPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(disposeButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    // Add an action listener for WSDL Analysis button
    disposeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        dispose();
      }
    });
  }

  /**
   * Sets the wsdl uri displayed in the text control
   *
   * @param uri The URI of the WSDL to set
   */
  public void setResponseText(String value) {
    responseText.setText(value);
    responseText.select(0, 0);
  }
}