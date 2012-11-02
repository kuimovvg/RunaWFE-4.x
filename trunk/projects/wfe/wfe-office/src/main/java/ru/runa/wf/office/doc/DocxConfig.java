package ru.runa.wf.office.doc;

import java.util.List;
import java.util.Map;

import ru.runa.wf.office.shared.FilesSupplierConfig;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DocxConfig extends FilesSupplierConfig {
    private boolean strictMode;
    private final Map<String, TableConfig> tables = Maps.newHashMap();
    private final Map<String, VariableFormat> typeHints = Maps.newHashMap();

    @Override
    protected String getContentType() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    @Override
    public String getDefaultOutputFileName() {
        return "document.docx";
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public Map<String, TableConfig> getTables() {
        return tables;
    }

    public Map<String, VariableFormat> getTypeHints() {
        return typeHints;
    }

    public static class TableConfig {
        private boolean addBreak;
        private String styleName;
        private final List<String> columns = Lists.newArrayList();

        public void setAddBreak(boolean addBreak) {
            this.addBreak = addBreak;
        }

        public boolean isAddBreak() {
            return addBreak;
        }

        public void setStyleName(String styleName) {
            this.styleName = styleName;
        }

        public String getStyleName() {
            return styleName;
        }

        public List<String> getColumns() {
            return columns;
        }
    }
}
