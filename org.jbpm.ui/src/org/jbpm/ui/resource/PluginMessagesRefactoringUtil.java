package org.jbpm.ui.resource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jbpm.ui.PluginConstants;

public class PluginMessagesRefactoringUtil {

    public static void main(String[] args) throws Exception {
        Map<String, String> dupls = new HashMap<String, String>();

        File pluginDir = new File("E:\\Dofs\\eclipse-gpd\\workspace\\org.jbpm.ui");
        if (!pluginDir.exists()) {
            System.err.println("Plug-In dir doen't exist: " + pluginDir.getAbsolutePath());
            System.exit(1);
        }

        File pluginXml = new File(pluginDir, "plugin.xml");
        String pluginXmlString = readFile(pluginXml);

        File[] propertyFiles = pluginDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().startsWith("plugin") && file.getPath().endsWith("properties");
            }
        });
        for (File propertyFile : propertyFiles) {
            System.out.println(" --- " + propertyFile.getName() + " --- ");
            
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertyFile));

            for (Object objKey : properties.keySet()) {
                String key = (String) objKey;
                String value = properties.getProperty(key);
                if (dupls.containsKey(value)) {
                    System.out.println("Duplicated keys: " + key + " .. " + dupls.get(value));
                }
                dupls.put(value, key);
                if (!findInString(pluginXmlString, key)) {
                    System.out.println("Unused: " + key);
                }
            }

            Pattern p = Pattern.compile("\"%([^\"]*)\"");
            java.util.regex.Matcher m = p.matcher(pluginXmlString);
            while (m.find()) {
                String key = m.group(1);
                if (!properties.containsKey(key)) {
                    System.out.println("Missed: " + key);
                }
            }
        }
    }

    private static String readFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[fis.available()];
        fis.read(buf);
        fis.close();
        return new String(buf, PluginConstants.UTF_ENCODING);
    }

    private static boolean findInString(String string, String msgKey) throws Exception {
        return string.contains("\"%" + msgKey + "\"");
    }
}
