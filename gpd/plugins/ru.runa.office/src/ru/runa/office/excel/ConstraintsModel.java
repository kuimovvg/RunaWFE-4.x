package ru.runa.office.excel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConstraintsModel {

    public static final int CELL = 0;
    public static final int ROW = 1;
    public static final int COLUMN = 2;
    public static final String CELL_CLASS = "ru.runa.wf.office.excel.CellConstraints";
    public static final String ROW_CLASS = "ru.runa.wf.office.excel.RowConstraints";
    public static final String COLUMN_CLASS = "ru.runa.wf.office.excel.ColumnConstraints";

    public String sheetName = "";
    public int sheetIndex = 1;
    public String variable;
    public final int type;
    public int row = 1;
    public int column = 1;

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getType() {
        return type;
    }

    public ConstraintsModel(int type) {
        this.type = type;
    }

    public static ConstraintsModel deserialize(Element element) {
        ConstraintsModel model = null;
        String className = element.getAttribute("class");
        if (CELL_CLASS.equals(className)) {
            model = new ConstraintsModel(CELL);
        }
        if (ROW_CLASS.equals(className)) {
            model = new ConstraintsModel(ROW);
        }
        if (COLUMN_CLASS.equals(className)) {
            model = new ConstraintsModel(COLUMN);
        }
        if (model == null) {
            return null;
        }
        model.variable = element.getAttribute("variable");
        Element conf = (Element) element.getElementsByTagName("config").item(0);
        model.sheetName = conf.getAttribute("sheetName");
        if (model.sheetName == null || model.sheetName.length() == 0) {
            model.sheetIndex = Integer.parseInt(conf.getAttribute("sheet"));
        }
        if (model.type == CELL || model.type == ROW) {
            model.row = Integer.parseInt(conf.getAttribute("row"));
        } else {
            model.row = Integer.parseInt(conf.getAttribute("rowStart"));
        }
        if (model.type == CELL || model.type == COLUMN) {
            model.column = Integer.parseInt(conf.getAttribute("column"));
        } else {
            model.column = Integer.parseInt(conf.getAttribute("columnStart"));
        }
        return model;
    }

    public void serialize(Document document, Element root) {
        Element el = document.createElement("binding");
        Element conf = document.createElement("config");
        el.setAttribute("variable", variable);
        switch (type) {
        case CELL:
            el.setAttribute("class", CELL_CLASS);
            break;
        case ROW:
            el.setAttribute("class", ROW_CLASS);
            break;
        case COLUMN:
            el.setAttribute("class", COLUMN_CLASS);
            break;
        }
        el.appendChild(conf);
        if (sheetName != null && sheetName.length()>0) {
            conf.setAttribute("sheetName", sheetName);
        } else {
            conf.setAttribute("sheet", "" + sheetIndex);
        }
        if (type == CELL || type == ROW) {
            conf.setAttribute("row", "" + row);
        } else {
            conf.setAttribute("rowStart", "" + row);
        }
        if (type == CELL || type == COLUMN) {
            conf.setAttribute("column", "" + column);
        } else {
            conf.setAttribute("columnStart", "" + column);
        }
        root.appendChild(el);
    }

}
