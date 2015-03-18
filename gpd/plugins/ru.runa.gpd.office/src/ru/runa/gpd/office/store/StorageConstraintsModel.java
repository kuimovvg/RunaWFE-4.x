package ru.runa.gpd.office.store;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.util.BackCompatibilityUtils;

public class StorageConstraintsModel {
    public static final int CELL = 0;
    public static final int ROW = 1;
    public static final int COLUMN = 2;
    public static final String CELL_CLASS = "ru.runa.wfe.office.excel.CellConstraints";
    public static final String ROW_CLASS = "ru.runa.wfe.office.excel.RowConstraints";
    public static final String COLUMN_CLASS = "ru.runa.wfe.office.excel.ColumnConstraints";
    public String sheetName = "";
    public int sheetIndex = 1;
    public String variableName = "";
    public final int type;
    public int row = 1;
    public int column = 1;
    private ConditionModel conditionModel;
    private QueryType queryType;

    public StorageConstraintsModel(int type, QueryType queryType) {
        this.type = type;
        this.queryType = queryType;
        this.conditionModel = new ConditionModel();
    }

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

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variable) {
        this.variableName = variable;
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

    public ConditionModel getConditionModel() {
        return conditionModel;
    }

    public void setConditionModel(ConditionModel conditionModel) {
        this.conditionModel = conditionModel;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public static StorageConstraintsModel deserialize(Element element) {
        StorageConstraintsModel model;
        String className = element.attributeValue("class");
        className = BackCompatibilityUtils.getClassName(className);
        Element conditions = element.element("conditions");
        QueryType queryType = null;
        if (conditions != null) {
            queryType = QueryType.valueOf(conditions.attributeValue("type"));
        }
        if (CELL_CLASS.equals(className)) {
            model = new StorageConstraintsModel(CELL, queryType);
        } else if (ROW_CLASS.equals(className)) {
            model = new StorageConstraintsModel(ROW, queryType);
        } else if (COLUMN_CLASS.equals(className)) {
            model = new StorageConstraintsModel(COLUMN, queryType);
        } else {
            throw new RuntimeException("Invaid class '" + className + "'");
        }
        model.variableName = element.attributeValue("variable");
        Element conf = element.element("config");
        model.sheetName = conf.attributeValue("sheetName");
        if (model.sheetName == null || model.sheetName.length() == 0) {
            model.sheetIndex = Integer.parseInt(conf.attributeValue("sheet"));
        }
        if (model.type == CELL || model.type == ROW) {
            model.row = Integer.parseInt(conf.attributeValue("row"));
        } else {
            model.row = Integer.parseInt(conf.attributeValue("rowStart"));
        }
        if (model.type == CELL || model.type == COLUMN) {
            model.column = Integer.parseInt(conf.attributeValue("column"));
        } else {
            model.column = Integer.parseInt(conf.attributeValue("columnStart"));
        }
        if (model.type == ROW || model.type == COLUMN) {
            if (conditions != null) {
                model.setConditionModel(new ConditionModel());
                for (Object condition : conditions.elements()) {
                    Element el = (Element) condition;
                    String is = el.attributeValue("is");
                    String val = el.attributeValue("val");
                    ConditionItem conditionItem = null;
                    if (queryType.equals(QueryType.UPDATE)) {
                        String newVal = el.attributeValue("newVal");
                        conditionItem = new UpdateConditionItem(Op.valueOf(is), val, newVal);
                    } else {
                        conditionItem = new ConditionItem(Op.valueOf(is), val);
                    }
                    model.getConditionModel().getConditions().add(conditionItem);
                }
            }
        }
        return model;
    }

    public void serialize(Document document, Element root) {
        Element binding = root.addElement("binding");
        Element config = binding.addElement("config");
        binding.addAttribute("variable", variableName);
        switch (type) {
        case CELL:
            binding.addAttribute("class", CELL_CLASS);
            break;
        case ROW:
            binding.addAttribute("class", ROW_CLASS);
            break;
        case COLUMN:
            binding.addAttribute("class", COLUMN_CLASS);
            break;
        }
        if (sheetName != null && sheetName.length() > 0) {
            config.addAttribute("sheetName", sheetName);
        } else {
            config.addAttribute("sheet", "" + sheetIndex);
        }
        if (type == CELL || type == ROW) {
            config.addAttribute("row", "" + row);
        } else {
            config.addAttribute("rowStart", "" + row);
        }
        if (type == CELL || type == COLUMN) {
            config.addAttribute("column", "" + column);
        } else {
            config.addAttribute("columnStart", "" + column);
        }
        if (conditionModel != null) {
            Element conditions = binding.addElement("conditions");
            conditions.addAttribute("type", getQueryType().toString());
            if (QueryType.CREATE.equals(getQueryType())) {
                return;
            }
            for (ConditionItem item : conditionModel.getConditions()) {
                Element condition = conditions.addElement("condition");
                if (item.getCondition() != null && item.getValue() != null) {
                    condition.addAttribute("is", item.getCondition().toString());
                    condition.addAttribute("val", item.getValue().toString());
                    if (item instanceof UpdateConditionItem) {
                        condition.addAttribute("newVal", ((UpdateConditionItem) item).getNewValue().toString());
                    }
                }
            }
        }
    }
}
