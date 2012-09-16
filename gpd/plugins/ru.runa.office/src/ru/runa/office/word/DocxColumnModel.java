package ru.runa.office.word;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DocxColumnModel {

    protected String variable;

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void serialize(Document document, Element parent) throws Exception {
        Element el = document.createElement("column");
        parent.appendChild(el);
        el.setAttribute("variable", variable);
    }

    public static DocxColumnModel deserialize(Element element) throws Exception {
        DocxColumnModel model = new DocxColumnModel();
        model.variable = element.getAttribute("variable");
        return model;
    }

}
