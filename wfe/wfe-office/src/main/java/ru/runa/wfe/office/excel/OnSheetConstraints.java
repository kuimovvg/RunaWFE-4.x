package ru.runa.wfe.office.excel;

import org.dom4j.Element;

import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.office.shared.XMLHelper;
import ru.runa.wfe.var.IVariableProvider;

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

    @Override
    public void applyPlaceholders(IVariableProvider variableProvider) {
        if (sheetName != null) {
            sheetName = ExpressionEvaluator.substitute(sheetName, variableProvider);
        }
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public String getSheetName() {
        return sheetName;
    }

}
