package ru.runa.wfe.office.excel;

import org.dom4j.Element;

import ru.runa.wfe.office.shared.XMLHelper;

public class OnSheetConstraints implements IExcelConstraints {
    private int sheetIndex;
    private String sheetName;

    @Override
    public void configure(Element element) {
        sheetName = element.attributeValue("sheetName");
        if (sheetName == null) {
            sheetIndex = XMLHelper.getIntAttribute(element, "sheet");
        }
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public String getSheetName() {
        return sheetName;
    }

}
