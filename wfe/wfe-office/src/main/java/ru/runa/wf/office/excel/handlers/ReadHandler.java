package ru.runa.wf.office.excel.handlers;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ru.runa.wf.office.excel.ExcelDataStore;
import ru.runa.wf.office.excel.ExcelStorable;
import ru.runa.wf.office.shared.FilesSupplierConfigParser;
import ru.runa.wf.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.IVariableProvider;

public class ReadHandler extends OfficeFilesSupplierHandler<ExcelBindings> {

    @Override
    protected FilesSupplierConfigParser<ExcelBindings> createParser() {
        return new ExcelBindingsParser();
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        ExcelDataStore dataStore = new ExcelDataStore();
        Workbook workbook = dataStore.loadWorkbook(config.getFileInputStream(variableProvider, true), config.isInputFileXLSX(variableProvider, false));
        for (ExcelBinding binding : config.getBindings()) {
            ExcelStorable storable = dataStore.load(workbook, binding.getConstraints());
            result.put(binding.getVariableName(), storable.getData());
        }
        return result;
    }

}
