/*
 * All sample code contained herein is provided to you "AS IS" without any warranties of any kind.
 */
package ru.cg.runaex.wsdl_analyzer.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import javax.swing.*;

import org.jdom2.Document;

import ru.cg.runaex.wsdl_analyzer.WebServiceClient;
import ru.cg.runaex.wsdl_analyzer.WebServiceRequestException;
import ru.cg.runaex.wsdl_analyzer.XMLSupport;
import ru.cg.runaex.wsdl_analyzer.bean.OperationInfo;
import ru.cg.runaex.wsdl_analyzer.bean.ServiceInfo;
import ru.cg.runaex.wsdl_analyzer.builder.ComponentBuilder;

/**
 * This class provides a simple GUI that uses the Web Services client classes
 *
 * @author Jim Winfield
 */
public class WSClientGUI extends JDialog {
  // The UI components ...
  JPanel mainPanel = new JPanel();
  JLabel wsdlLabel = new JLabel();
  JTextField wsdlURI = new JTextField();
  JLabel servicesLabel = new JLabel();
  JComboBox servicesCombo = new JComboBox();
  JLabel requestLabel = new JLabel();
  JTextArea messageText = new JTextArea();
  JScrollPane messageScrollPane = new JScrollPane();
  JButton sendButton = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JButton analyzeButton = new JButton();
  JLabel operationsLabel = new JLabel();
  JComboBox operationsCombo = new JComboBox();

  // UI support ...
  DefaultComboBoxModel serviceModel = new DefaultComboBoxModel();
  DefaultComboBoxModel operationModel = new DefaultComboBoxModel();

  ServiceInfo currentService = null;
  OperationInfo currentOperation = null;

  /**
   * Constructor
   */
  public WSClientGUI() {
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
    wsdlLabel.setText("WSDL URI:");
    wsdlURI.setText("");
    servicesLabel.setText("Available Services:");
    requestLabel.setText("Request Message:");
    messageText.setBorder(BorderFactory.createEtchedBorder());
    messageText.setText("");
    messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    messageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    analyzeButton.setText("Analyze");
    operationsLabel.setText("Defined Operations:");
    messageScrollPane.getViewport().add(messageText);
    sendButton.setText("Send Request");

    // Color stuff ...
    messageText.setBackground(Color.white);
    messageText.setForeground(Color.black);
    servicesCombo.setBackground(Color.white);
    servicesCombo.setForeground(Color.black);
    operationsCombo.setBackground(Color.white);
    operationsCombo.setForeground(Color.black);
    wsdlURI.setBackground(Color.white);
    wsdlURI.setForeground(Color.black);

    // Layout...
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(wsdlLabel, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(wsdlURI, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(analyzeButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    mainPanel.add(servicesLabel, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(servicesCombo, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(operationsLabel, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(operationsCombo, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(requestLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
    mainPanel.add(messageScrollPane, new GridBagConstraints(0, 7, 4, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    mainPanel.add(sendButton, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    // Associate models ...
    servicesCombo.setModel(serviceModel);
    operationsCombo.setModel(operationModel);

    // Add an action listener for WSDL Analysis button
    analyzeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        analyzeWsdl();
      }
    });

    // Add selection listeners for the comboboxes
    servicesCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        ServiceInfo selectedService = (ServiceInfo) serviceModel.getSelectedItem();

        if (selectedService != currentService) {
          showServiceInfo(selectedService);
          currentService = selectedService;
        }
      }
    });

    operationsCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        OperationInfo selectedOperation = (OperationInfo) operationModel.getSelectedItem();

        if (selectedOperation != currentOperation) {
          showOperationInfo(selectedOperation);
          currentOperation = selectedOperation;
        }
      }
    });

    sendButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendRequest(currentOperation, messageText.getText());
      }
    });
  }

  /**
   * Sets the wsdl uri displayed in the text control
   *
   * @param uri The URI of the WSDL to set
   */
  public void setWsdlURI(String uri) {
    wsdlURI.setText(uri);
  }

  /**
   * Invokes the current operation sending to it the current request message.
   *
   * @return The response generated will be returned
   */
  private void sendRequest(OperationInfo operationInfo, String messageXml) {
    // Send the request and get the response
    String response = null;
    try {
      Document responseDocument = WebServiceClient.sendRequest(operationInfo, messageXml);
      response = XMLSupport.outputString(responseDocument);
    }
    catch (WebServiceRequestException e) {
      e.printStackTrace();  //TODO: handle exception correctly
    }

    // Show the response
    ResponseDialog responseDlg = new ResponseDialog(this);
    responseDlg.setTitle("Web Services Client");
    responseDlg.setResponseText(response);
    responseDlg.setSize(550, 400);
    responseDlg.show();
  }

  /**
   * Shows the definition of a ServiceInfo
   */
  private void showServiceInfo(ServiceInfo serviceInfo) {
    // Clear UI components
    operationModel.removeAllElements();
    messageText.setText("");

    if (serviceInfo == null) {
      // Nothing to do
      return;
    }

    for (OperationInfo operInfo : serviceInfo.getOperationsByName().values()) {
      operationModel.addElement(operInfo);
    }
  }

  /**
   * Shows the definition of an OperationInfo
   */
  private void showOperationInfo(OperationInfo operationInfo) {
    if (operationInfo != null) {
      // Show the input message for this operation
      messageText.setText(operationInfo.getInputMessageStub());
    }
    else {
      // Just clear the request message text control
      messageText.setText("");
    }
  }

  /**
   * Analyzes the WSDL and displays information in the UI components.
   */
  private void analyzeWsdl() {
    try {
      // Clear UI components
      serviceModel.removeAllElements();
      operationModel.removeAllElements();
      messageText.setText("");

      // Create the in memory model of services and operations
      // defined in the current WSDL
      ComponentBuilder builder = new ComponentBuilder();
      Collection services = builder.buildComponents(wsdlURI.getText()).values();

      // List all the services defined in the current WSDL

      for (Object service : services) {
        // Load each service into the services combobox model
        ServiceInfo serviceInfo = (ServiceInfo) service;
        serviceModel.addElement(serviceInfo);
      }
    }
    catch (Exception e) {
      // Report the error to the user
      System.err.println(e.getMessage());
      e.printStackTrace();

      JOptionPane.showMessageDialog(this, e.getMessage(), "WSDL Analysis Failed", JOptionPane.ERROR_MESSAGE);
    }
  }
}