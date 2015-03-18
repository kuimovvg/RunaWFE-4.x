package ru.runa.wfe.office.storage.handler;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.services.StoreHelperImpl;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class ExternalStorageHandler extends OfficeFilesSupplierHandler<DataBindings> {

    private StoreHelper storeHelper;

    @Override
    protected FilesSupplierConfigParser<DataBindings> createParser() {
        return new StorageBindingsParser();
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider, IFileDataProvider fileDataProvider) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        storeHelper = new StoreHelperImpl();
        storeHelper.setConfig(config);
        for (DataBinding binding : config.getBindings()) {
            WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
            binding.getConstraints().applyPlaceholders(variableProvider);
            storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
            result.put(binding.getVariableName(), storeHelper.execute(binding, variable, config.getQueryType()));
        }
        return result;
    }

}
