package ru.runa.wfe.service;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class AdminScriptUtils {

    public static Document createScriptDocument() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("workflowScript");
        root.addNamespace("", "http://runa.ru/xml");
        // root.addAttribute("xmlns", "http://runa.ru/xml");
        root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.addAttribute("xsi:schemaLocation", "http://runa.ru/xml workflowScript.xsd");
        return document;
    }

}
