package ru.runa.wf.web.datafile.builder;

import java.util.zip.ZipOutputStream;

import org.dom4j.Document;

/**
 * Provide method creating the part of data file.
 * 
 * @author riven
 * 
 */
public interface DataFileBuilder {

    static final String FILE_NAME = "archive";
    static final String FILE_EXT = ".datafile";

    /**
     * put data to zip archive and populate script file xml elements
     * 
     * @param zos
     *            - zip archive
     * @param script
     *            - xml file contains action for invocation
     */
    void build(ZipOutputStream zos, Document script);
}
