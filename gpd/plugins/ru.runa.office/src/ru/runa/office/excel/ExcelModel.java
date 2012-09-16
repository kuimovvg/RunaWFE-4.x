package ru.runa.office.excel;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.office.FilesSupplierMode;
import ru.runa.office.InputOutputComposite.InputOutputModel;

public class ExcelModel extends Observable {

    private final FilesSupplierMode mode;
    private final InputOutputModel inOutModel;
    public final List<ConstraintsModel> constraintses = new ArrayList<ConstraintsModel>();

    public ExcelModel(FilesSupplierMode mode) {
        this(mode, new InputOutputModel());
    }

    public ExcelModel(FilesSupplierMode mode, InputOutputModel inOutModel) {
        this.mode = mode;
        this.inOutModel = inOutModel;
    }

    public static ExcelModel fromXml(String xml, FilesSupplierMode mode) throws Exception {
        Document document = XmlUtil.parseDocument(new ByteArrayInputStream(xml.getBytes(PluginConstants.UTF_ENCODING)));
        InputOutputModel inOutModel;
        try {
            Element input = null;
            if (mode.isInSupported()) {
                input = (Element) document.getElementsByTagName("input").item(0);
            }
            Element output = null;
            if (mode.isInSupported()) {
                output = (Element) document.getElementsByTagName("output").item(0);
            }
            inOutModel = InputOutputModel.deserialize(input, output);
        } catch (Exception e) {
            DesignerLogger.logErrorWithoutDialog("Unabe parse config: " + xml, e);
            inOutModel = new InputOutputModel();
        }
        ExcelModel model = new ExcelModel(mode, inOutModel);
        NodeList constraintsElements = document.getElementsByTagName("binding");
        for (int i = 0; i < constraintsElements.getLength(); i++) {
            Element constraintsElement = (Element) constraintsElements.item(i);
            model.constraintses.add(ConstraintsModel.deserialize(constraintsElement));
        }
        return model;
    }

    @Override
    public String toString() {
        try {
            Document document = XmlUtil.createDocument("config", null);
            Element root = document.getDocumentElement();
            inOutModel.serialize(document, root, mode);
            for (ConstraintsModel model : constraintses) {
                model.serialize(document, root);
            }
            return new String(XmlUtil.writeXml(document), PluginConstants.UTF_ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("Unable serialize model to XML", e);
        }
    }

    public InputOutputModel getInOutModel() {
        return inOutModel;
    }
}
