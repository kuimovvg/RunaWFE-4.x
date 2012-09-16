package ru.runa.office.word;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.office.FilesSupplierMode;
import ru.runa.office.InputOutputComposite.InputOutputModel;

public class DocxModel extends Observable {

    private boolean strict = true;
    protected InputOutputModel inOutModel = new InputOutputModel();

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public List<DocxTableModel> tables = new ArrayList<DocxTableModel>();

    public void addNewTable(DocxTableModel t) {
        tables.add(t);
    }

    public static DocxModel fromXml(String xml) throws Exception {
        DocxModel model = new DocxModel();
        Document document = XmlUtil.parseDocument(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        model.setStrict(Boolean.parseBoolean(document.getDocumentElement().getAttribute("strict")));
        try {
            Element input = (Element) document.getElementsByTagName("input").item(0);
            Element output = (Element) document.getElementsByTagName("output").item(0);
            model.setInOutModel(InputOutputModel.deserialize(input, output));
        } catch (Exception e) {
        }
        NodeList tableElements = document.getElementsByTagName("table");
        for (int i = 0; i < tableElements.getLength(); i++) {
            Element tableElement = (Element) tableElements.item(i);
            DocxTableModel tableModel = DocxTableModel.deserialize(tableElement);
            model.addNewTable(tableModel);
        }
        return model;
    }

    @Override
    public String toString() {
        try {
            Document document = XmlUtil.createDocument("config", null);
            Element root = document.getDocumentElement();
            root.setAttribute("strict", Boolean.toString(strict));
            inOutModel.serialize(document, root, FilesSupplierMode.BOTH);
            for (DocxTableModel model : tables) {
                model.serialize(document, root);
            }
            return new String(XmlUtil.writeXml(document), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Unable serialize model to XML", e);
        }
    }

    public void setInOutModel(InputOutputModel inOutModel) {
        this.inOutModel = inOutModel;
    }

    public InputOutputModel getInOutModel() {
        return inOutModel;
    }

}
