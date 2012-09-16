package ru.runa.bpm.ui.resource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import ru.runa.bpm.ui.PluginConstants;

public class MessagesRefactoringUtil {
    private static Map<String, String> processedJavaFiles = new HashMap<String, String>();
    private static int level = 1; // 1 - search string "s", 2 - search string Messages.getString("s")

    private static void processDirectory(File dir) throws Exception {
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().endsWith("java");
            }
        });
        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file);
            } else {
                FileInputStream fis = new FileInputStream(file);
                byte[] buf = new byte[fis.available()];
                fis.read(buf);
                fis.close();
                processedJavaFiles.put(file.getPath(), new String(buf, PluginConstants.UTF_ENCODING));
            }
        }
    }
    
	public static void main(String[] args) throws Exception {
		Map<String, String> dupls = new HashMap<String, String>();
		
		Properties properties = new Properties();
		properties.load(MessagesRefactoringUtil.class.getResourceAsStream("messages_ru.properties"));
		File srcDir = new File("E:\\Dofs\\eclipse-gpd\\workspace\\ru.runa.bpm.ui\\src");
		if (!srcDir.exists()) {
		    System.err.println("Source dir doen't exist: " + srcDir.getAbsolutePath());
		    System.exit(1);
		}
		
		processDirectory(srcDir);

		for (Object objKey : properties.keySet()) {
			String key = (String) objKey;
			String value = properties.getProperty(key);
			if (dupls.containsKey(value)) {
				System.out.println("Duplicated keys: " + key + " .. " + dupls.get(value));
			}
			dupls.put(value, key);
			if (key.startsWith("model.validation")) {
				continue;
			}
			if (key.startsWith("FormTypeForm.description")) {
				continue;
			}
			if(!findMsgKey(key)) {
				System.out.println("Unused: " + key);
			}
		}

        Pattern p = Pattern.compile("Messages.getString\\(\"([^\"]*)\"\\)");
        for (String fileName : processedJavaFiles.keySet()) {
            String source = processedJavaFiles.get(fileName);
            java.util.regex.Matcher m = p.matcher(source);
            while (m.find()) {
                String key = m.group(1);
                if (!properties.containsKey(key)) {
                    System.out.println("Missed: " + key + " (in " + fileName + ")");
                }
            }
        }
	}
	
	private static boolean findMsgKey(String msgKey) throws Exception {
	    String searchPattern = null;
	    if (level == 1)
	        searchPattern = "\"" + msgKey + "\"";
        if (level == 2)
            searchPattern = "Messages.getString(\"" + msgKey + "\")";
		for (String source : processedJavaFiles.values()) {
			if (source.contains(searchPattern)) {
			    return true;
			}
		}
		return false;
	}
}
