package ru.runa.bpm.ui.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.DOMParser;
import org.eclipse.core.resources.IFile;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.infopath.TypeMapper;
import ru.runa.bpm.ui.infopath.ValidationMapper;
import ru.runa.bpm.ui.validation.ValidatorConfig;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class InfoPathSupport extends Thread {
    private static final String TEMPLATE_XML_FILE_NAME = "template.xml";

    private static final Pattern NAME_ATTR_PATTERN = Pattern.compile("name=\"(.[^\"]*)\"");

    private static final Pattern PUBLISH_URL_ATTR_PATTERN = Pattern.compile("publishUrl=\"(.[^\"]*)\"");

    private static final File ASSEMBLY_TMP_FOLDER = new File(DesignerPlugin.getDefault().getStateLocation().toFile(), "infopathassembly");

    private static final String MYSCHEMA_XSD_FILE_NAME = "myschema.xsd";

    private static final String MANIFEST_FILE_NAME = "manifest.xsf";

    private static final int BUFFER_SIZE = 4096;

    static {
        System.loadLibrary("RunaGPDInfoPathSupport");
        if (!ASSEMBLY_TMP_FOLDER.exists()) {
            ASSEMBLY_TMP_FOLDER.mkdirs();
        }
    }

    private static Object nativeDllLock = new Object();

    private static native boolean isXSNFileValid(String fullPath) throws Exception;

    private static native void createInfoPathXSNFile(String templatesFolderPath, String xsnFilePath) throws Exception;

    private static native void extractFileFromXSN(String xsnFolderPath, String xsnFileName, String extractedFolderPath, String extractedFileName)
            throws Exception;

    private static native void extractAllFilesFromXSN(String xsnFolderPath, String xsnFileName, String extractedFolderPath) throws Exception;

    private final File xsnFormFile;

    private final FormNode formNode;

    private final IFile file;

    private final File tmpFolder;

    private Process process;

    public InfoPathSupport(FormNode formNode, IFile file, String xsnFilePath) {
        this.formNode = formNode;
        this.file = file;
        this.tmpFolder = new File(ASSEMBLY_TMP_FOLDER, formNode.getName());
        this.xsnFormFile = new File(xsnFilePath);
        setName(PluginConstants.NON_GUI_THREAD_NAME);
    }

    public boolean init() throws Exception {
        if (this.tmpFolder.exists()) {
            this.tmpFolder.delete();
        }
        this.tmpFolder.mkdir();
        synchronized (nativeDllLock) {
            if (!xsnFormFile.exists() || !isXSNFileValid(xsnFormFile.getAbsolutePath())) {
                createXSN(xsnFormFile);
            } else {
                republishXSN(xsnFormFile);
            }
            Thread.sleep(700);
        }
        return true;
    }

    public boolean rewriteXsnFileWithAnotherTemplateId() throws Exception {
        if (this.tmpFolder.exists()) {
            return false;
        }
        try {
            this.tmpFolder.mkdir();
            synchronized (nativeDllLock) {
                if (xsnFormFile.exists()) {
                    republishXSN(xsnFormFile);
                }
            }
            return true;
        } finally {
            File[] files = tmpFolder.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            deleteFile(tmpFolder);
        }
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @Override
    public void run() {
        try {
            process.waitFor();
            synchronized (nativeDllLock) {
                transform();
            }
        } catch (Throwable e) {
            DesignerLogger.logErrorWithoutDialog("InfoPath process error", e);
        }
        try {
            File[] files = tmpFolder.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            deleteFile(tmpFolder);
        } catch (Throwable e) {
            DesignerLogger.logErrorWithoutDialog("InfoPath, remove files error", e);
        }
    }

    private void republishXSN(File xsnFile) throws Exception {
        extractAllFilesFromXSN(xsnFile.getParent() + "\\", xsnFile.getName(), tmpFolder.getAbsolutePath() + "\\");
        xsnFile.delete();
        publishXSN(xsnFile);
    }

    private void createXSN(File xsnFormFile) throws Exception {
        String[] fileNames = { "myschema.xsd", "sampledata.xml", TEMPLATE_XML_FILE_NAME, "view1.xsl", MANIFEST_FILE_NAME };
        for (int i = 0; i < fileNames.length; i++) {
            InputStream is = InfoPathSupport.class.getResourceAsStream("/infopathform/" + fileNames[i]);
            if (is != null) {
                copyFile(is, new File(tmpFolder, fileNames[i]));
            }
        }
        publishXSN(xsnFormFile);
    }

    private void publishXSN(File xsnFile) throws Exception {
        xsnFile.createNewFile();

        // "urn:schemas-microsoft-com:office:infopath:ID:-myXSD-2008-01-19T14-43-23"
        StringBuffer templateIdBuffer = new StringBuffer("urn:office:infopath:");
        templateIdBuffer.append(String.valueOf(System.currentTimeMillis())).append(":-myXSD-");
        templateIdBuffer.append(new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date()));
        String infoPathFormTemplateId = templateIdBuffer.toString().replaceAll(" ", "");
        logDebug("Using INFOPATHFORMTEMPLATEID: " + infoPathFormTemplateId);

        byte[] xsfManifestData = readBytesFromStream(new FileInputStream(new File(tmpFolder, MANIFEST_FILE_NAME)));
        String xsfManifest = new String(xsfManifestData, PluginConstants.UTF_ENCODING);

        Matcher m = NAME_ATTR_PATTERN.matcher(xsfManifest);
        StringBuffer buffer = new StringBuffer();
        if (m.find()) {
            m.appendReplacement(buffer, Matcher.quoteReplacement("name=\"" + infoPathFormTemplateId + "\""));
        }
        m.appendTail(buffer);
        String content = buffer.toString();

        // delete publishUrl attribute
        m = PUBLISH_URL_ATTR_PATTERN.matcher(content);
        buffer = new StringBuffer();
        if (m.find()) {
            logDebug("Found attribute 'publishUrl', deleting");
            m.appendReplacement(buffer, "");
        }
        m.appendTail(buffer);
        content = buffer.toString();
        //

        copyFile(new ByteArrayInputStream(content.getBytes(PluginConstants.UTF_ENCODING)), new File(tmpFolder, MANIFEST_FILE_NAME));

        byte[] xmlTemplateData = readBytesFromStream(new FileInputStream(new File(tmpFolder, "template.xml")));
        String xmlTemplate = new String(xmlTemplateData, PluginConstants.UTF_ENCODING);

        m = NAME_ATTR_PATTERN.matcher(xmlTemplate);
        buffer = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(buffer, Matcher.quoteReplacement("name=\"" + infoPathFormTemplateId + "\""));
        }
        m.appendTail(buffer);
        xmlTemplate = buffer.toString();
        copyFile(new ByteArrayInputStream(xmlTemplate.getBytes(PluginConstants.UTF_ENCODING)), new File(tmpFolder, "template.xml"));

        createInfoPathXSNFile(tmpFolder.getAbsolutePath() + "\\", xsnFile.getAbsolutePath());
    }

    private void copyFile(InputStream inputStream, File destFile) throws IOException {
        destFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(destFile);
        byte[] buff = new byte[32 * 1024];
        int length;
        while (inputStream.available() > 0) {
            length = inputStream.read(buff);
            if (length > 0)
                fos.write(buff, 0, length);
        }
        inputStream.close();
        closeStream(fos);
    }

    private byte[] readXsnFile(File xsnFile, String fileName) throws Exception {
        extractFileFromXSN(xsnFile.getParent() + "\\", xsnFile.getName(), tmpFolder.getAbsolutePath() + "\\", fileName);
        return readBytesFromStream(new FileInputStream(new File(tmpFolder, fileName)));
    }

    private byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        byte[] data = new byte[BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream bos = new BufferedOutputStream(baos, BUFFER_SIZE);
        try {
            int bytesRead;
            while ((bytesRead = inputStream.read(data)) != -1) {
                bos.write(data, 0, bytesRead);
            }
        } finally {
            closeStream(baos);
            closeStream(bos);
            inputStream.close();
        }
        return baos.toByteArray();
    }

    private void closeStream(OutputStream outputStream) throws IOException {
        if (outputStream != null) {
            outputStream.flush();
            outputStream.close();
        }
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            if (!file.delete()) {
                DesignerLogger.logInfo("WARNING: File " + file.getAbsolutePath() + " was not deleted.");
            }
        }
    }

    private void logDebug(String str) {
        DesignerLogger.logInfo("InfoPath [DEBUG]: " + str);
    }

    @SuppressWarnings("unchecked")
	private void transform() throws Exception {
        byte[] xsdData = readXsnFile(xsnFormFile, MYSCHEMA_XSD_FILE_NAME);
        Document xsd = (Document) new DOMParser().parseXML(new ByteArrayInputStream(xsdData));
        byte[] xsfData = readXsnFile(xsnFormFile, MANIFEST_FILE_NAME);
        Document xsf = (Document) new DOMParser().parseXML(new ByteArrayInputStream(xsfData));

        List<Variable> formVariables = new ArrayList<Variable>();

        String namespace = "my";
        String rootGroup = "myFields";

        JXPathContext xsdDocContext = JXPathContext.newContext(xsd);
        JXPathContext xsfDocContext = JXPathContext.newContext(xsf);

        // extracting xsf properties
        String namespacePropValue = (String) xsfDocContext.getValue("//xsf:property[@name=\"namespace\"]/@value");
        NamedNodeMap attrs = xsf.getElementsByTagName("xsf:xDocumentClass").item(0).getAttributes();
        for (int j = 0; j < attrs.getLength(); j++) {
            Node attr = attrs.item(j);
            if (namespacePropValue.equals(attr.getNodeValue())) {
                namespace = attr.getNodeName().toLowerCase().substring(6);
                break;
            }
        }
        rootGroup = (String) xsfDocContext.getValue("//xsf:property[@name=\"rootElement\"]/@value");
        logDebug("Using namespace property: " + namespace);
        logDebug("Using rootElement property: " + rootGroup);

        // extracting all form variables
        List<String> rootGroupElementNames = new ArrayList<String>();

        Node rootNode = (Node) xsdDocContext.selectSingleNode("/xsd:schema/xsd:element[@name=\"" + rootGroup + "\"]");
        JXPathContext rootGroupContext = JXPathContext.newContext(xsdDocContext, rootNode);
        List<Node> nodes = rootGroupContext.selectNodes("//xsd:element");
        for (Node node : nodes) {
            JXPathContext elementContext = JXPathContext.newContext(xsdDocContext, node);
            String globalName = (String) elementContext.getValue("@ref");
            rootGroupElementNames.add(globalName.substring(namespace.length() + 1));
        }
        nodes = xsdDocContext.selectNodes("/xsd:schema/xsd:element[@type]");
        for (Node node : nodes) {
            String varName = (String) JXPathContext.newContext(xsdDocContext, node).getValue("@name");
            String varType = (String) JXPathContext.newContext(xsdDocContext, node).getValue("@type");
            if ((varType != null) && (varType.length() > 0)) {
                // if (rootGroupElementNames.contains(varName)) { // this check
                // for single elements
                logDebug("Found variable: " + varName + " of type " + varType);
                Variable variable = new Variable(varName);
                variable.setFormat(TypeMapper.getWfeTypeForInfopathType(varType));
                formVariables.add(variable);
            }
        }

        List<String> variableNames = formNode.getProcessDefinition().getVariableNames(true);

        // Base Validation, do not remove
        Map<String, Map<String, ValidatorConfig>> validators = new HashMap<String, Map<String, ValidatorConfig>>();
        for (Variable var : formVariables) {
            validators.put(var.getName(), new HashMap<String, ValidatorConfig>());
            if (!variableNames.contains(var.getName())) {
                formNode.getProcessDefinition().addVariable(var);
            }
        }

        // Additional validation starts here
        ValidationMapper.init(validators, namespace, rootGroup);
        // extracting form validation
        nodes = xsfDocContext.selectNodes("//xsf:errorCondition");
        for (Node node : nodes) {
            JXPathContext context = JXPathContext.newContext(xsfDocContext, node);
            String match = (String) context.getValue("@match");
            String expressionContext = (String) context.getValue("@expressionContext");
            String expression = (String) context.getValue("@expression");

            String shortErrorMessage = (String) context.getValue("xsf:errorMessage/@shortMessage");
            String errorMessage = (String) context.getValue("xsf:errorMessage");
            if (shortErrorMessage.length() > errorMessage.length()) {
                errorMessage = shortErrorMessage;
            }

            logDebug("Found validation rule: " + match + " | " + expressionContext + " | " + expression + " | " + errorMessage);
            ValidationMapper.apply(match, expressionContext, expression, errorMessage);
        }
        // Additional validation ends here

        // creating validation for form do not remove
        ValidationUtil.rewriteValidation(file, formNode.getValidationFileName(), validators);
    }

}
