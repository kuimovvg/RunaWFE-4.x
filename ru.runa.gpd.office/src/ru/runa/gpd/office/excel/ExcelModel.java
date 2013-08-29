package ru.runa.gpd.office.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite.InputOutputModel;
import ru.runa.gpd.util.XmlUtil;

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

    public static ExcelModel fromXml(String xml, FilesSupplierMode mode) {
        Document document = XmlUtil.parseWithoutValidation(xml);
        InputOutputModel inOutModel;
        try {
            Element input = null;
            if (mode.isInSupported()) {
                input = document.getRootElement().element("input");
            }
            Element output = null;
            if (mode.isInSupported()) {
                output = document.getRootElement().element("output");
            }
            inOutModel = InputOutputModel.deserialize(input, output);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Unabe parse config: " + xml, e);
            inOutModel = new InputOutputModel();
        }
        ExcelModel model = new ExcelModel(mode, inOutModel);
        List<Element> constraintsElements = document.getRootElement().elements("binding");
        for (Element constraintsElement : constraintsElements) {
            model.constraintses.add(ConstraintsModel.deserialize(constraintsElement));
        }
        return model;
    }

    @Override
    public String toString() {
        Document document = XmlUtil.createDocument("config");
        Element root = document.getRootElement();
        inOutModel.serialize(document, root, mode);
        for (ConstraintsModel model : constraintses) {
            model.serialize(document, root);
        }
        return XmlUtil.toString(document);
    }

    public InputOutputModel getInOutModel() {
        return inOutModel;
    }
}
