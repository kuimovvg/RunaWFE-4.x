package ru.cg.runaex.wsdl_analyzer.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * @author urmancheev
 */
public class WsdlAnalyzerGui {

  public static void main(String[] args) {
    WSClientGUI gui = new WSClientGUI();
    gui.setSize(640, 480);
    gui.setTitle("Web Services Client");
    gui.addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent evt) {
        System.exit(0);
      }
    });
    gui.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        System.exit(0);
      }
    });

    if (args.length > 0)
      gui.setWsdlURI(args[0]);

    gui.show();
  }

}
