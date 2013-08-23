package ru.runa.wfe.office.excel.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ru.runa.wfe.office.excel.ExcelDataStore;
import ru.runa.wfe.office.excel.ExcelStorable;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.IVariableProvider;

public class ExcelReadHandler extends OfficeFilesSupplierHandler<ExcelBindings> {

    @Override
    protected FilesSupplierConfigParser<ExcelBindings> createParser() {
        return new ExcelBindingsParser();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        ExcelDataStore dataStore = new ExcelDataStore();
        Workbook workbook = dataStore
                .loadWorkbook(config.getFileInputStream(variableProvider, true), config.isInputFileXLSX(variableProvider, false));
        for (ExcelBinding binding : config.getBindings()) {
            ExcelStorable storable = dataStore.load(workbook, binding.getConstraints());
            result.put(binding.getVariableName(), storable.getData());
        }
        return result;
    }

}
