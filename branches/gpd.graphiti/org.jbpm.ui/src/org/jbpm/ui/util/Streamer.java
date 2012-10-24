package org.jbpm.ui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jbpm.ui.DesignerLogger;

/**
 * Helper for problem in executing external processes
 */
public class Streamer extends Thread {
    private InputStream inputStream;

    public Streamer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                DesignerLogger.logInfo(line);
            }
        } catch (IOException ioe) {
            DesignerLogger.logError(ioe);
        }
    }
}
