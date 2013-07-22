package ru.runa.wfe.office.excel.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ru.runa.wfe.office.excel.ExcelDataStore;
import ru.runa.wfe.office.excel.ExcelStorable;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class ExcelSaveHandler extends OfficeFilesSupplierHandler<ExcelBindings> {

    @Override
    protected FilesSupplierConfigParser<ExcelBindings> createParser() {
        return new ExcelBindingsParser();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        ExcelDataStore dataStore = new ExcelDataStore();
        Workbook workbook = dataStore.loadWorkbook(config.getFileInputStream(variableProvider, false),
                config.isInputFileXLSX(variableProvider, false));
        for (ExcelBinding binding : config.getBindings()) {
            WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
            if (variable.getValue() != null) {
                ExcelStorable storable = dataStore.createStorable(binding.getConstraints());
                storable.setFormat(variable.getFormatNotNull());
                storable.setData(variable.getValue());
                dataStore.save(workbook, storable);
            } else {
                log.warn("Omitted binding as variable was null: " + binding.getVariableName());
            }
        }
        dataStore.saveWorkbook(workbook, config.getFileOutputStream(result, true));
        return result;
    }

}
