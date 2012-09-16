package ru.runa.bpm.par;

import ru.runa.wf.ProcessDefinitionFileDoesNotExistException;

import com.google.common.base.Preconditions;

public abstract class FileDataProvider {
    public static final String PROCESSDEFINITION_XML_FILE_NAME = "processdefinition.xml";
    public static final String FORMS_XML_FILE_NAME = "forms.xml";
    public static final String GPD_XML_FILE_NAME = "gpd.xml";
    public static final String VARIABLES_XML_FILE_NAME = "variables.xml";
    public static final String ORGFUNCTIONS_XML_FILE_NAME = "orgfunctions.xml";
    public static final String GRAPH_IMAGE_OLD_FILE_NAME = "graph.gif";
    public static final String GRAPH_IMAGE_NEW_FILE_NAME = "processimage.jpg";
    public static final String INDEX_FILE_NAME = "index.html";
    public static final String START_DISABLED_IMAGE_FILE_NAME = "start-disabled.png";
    public static final String START_IMAGE_FILE_NAME = "start.png";
    public static final String FORM_CSS_FILE_NAME = "form.css";
    public static final String SUBSTITUTION_EXCEPTIONS_FILE_NAME = "substitutionExceptions.xml";

    public abstract byte[] getFileData(String fileName);

    public byte[] getFileDataNotNull(String fileName) {
        byte[] data = getFileData(fileName);
        if (data == null) {
            throw new ProcessDefinitionFileDoesNotExistException(fileName);
        }
        Preconditions.checkNotNull(data, "no '" + fileName + "' inside process archive");
        return data;
    }

}
