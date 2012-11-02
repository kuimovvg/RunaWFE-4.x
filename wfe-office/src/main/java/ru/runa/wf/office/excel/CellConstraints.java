package ru.runa.wf.office.excel;

import org.dom4j.Element;

import ru.runa.wf.office.shared.XMLHelper;

public class CellConstraints extends OnSheetConstraints {
    private int rowIndex;
    private int columnIndex;

    @Override
    public void configure(Element element) {
        super.configure(element);
        rowIndex = XMLHelper.getIntAttribute(element, "row");
        columnIndex = XMLHelper.getIntAttribute(element, "column");
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

}
