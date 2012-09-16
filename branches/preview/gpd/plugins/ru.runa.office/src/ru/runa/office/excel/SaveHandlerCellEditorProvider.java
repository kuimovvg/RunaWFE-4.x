package ru.runa.office.excel;

import ru.runa.office.FilesSupplierMode;
import ru.runa.office.resource.Messages;

public class SaveHandlerCellEditorProvider extends BaseExcelHandlerCellEditorProvider {

    @Override
    protected String getTitle() {
        return Messages.getString("ExportExcelHandlerConfig.title");
    }

    @Override
    protected FilesSupplierMode getMode() {
        return FilesSupplierMode.BOTH;
    }

}
