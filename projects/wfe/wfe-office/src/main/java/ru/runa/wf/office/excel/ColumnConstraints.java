package ru.runa.wf.office.excel;

import org.dom4j.Element;

import ru.runa.wf.office.shared.XMLHelper;

public class ColumnConstraints extends OnSheetConstraints {
    private int columnIndex;
    private int rowStartIndex;

    @Override
    public void configure(Element element) {
        super.configure(element);
        columnIndex = XMLHelper.getIntAttribute(element, "column");
        rowStartIndex = XMLHelper.getIntAttribute(element, "rowStart", 0);
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowStartIndex() {
        return rowStartIndex;
    }

}
