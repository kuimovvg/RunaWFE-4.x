package ru.runa.gpd.office.word;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite.InputOutputModel;
import ru.runa.gpd.util.XmlUtil;

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
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        model.setStrict(Boolean.parseBoolean(root.attributeValue("strict")));
        try {
            Element input = root.element("input");
            Element output = root.element("output");
            model.setInOutModel(InputOutputModel.deserialize(input, output));
        } catch (Exception e) {
        }
        List<Element> tableElements = root.elements("table");
        for (Element tableElement : tableElements) {
            DocxTableModel tableModel = DocxTableModel.deserialize(tableElement);
            model.addNewTable(tableModel);
        }
        return model;
    }

    @Override
    public String toString() {
        try {
            Document document = XmlUtil.createDocument("config");
            Element root = document.getRootElement();
            root.addAttribute("strict", Boolean.toString(strict));
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
