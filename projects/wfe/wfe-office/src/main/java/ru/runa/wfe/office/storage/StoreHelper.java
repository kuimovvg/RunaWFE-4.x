package ru.runa.wfe.office.storage;

import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.binding.QueryType;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;

public interface StoreHelper {

    void setConfig(DataBindings config);

    void setVariableFormat(VariableFormat format);

    Object execute(DataBinding binding, WfVariable variable, QueryType queryType);

    ExecutionResult findAll(DataBinding binding);

    ExecutionResult findByFilter(DataBinding binding);

    ExecutionResult update(DataBinding binding);

    ExecutionResult delete(DataBinding binding);

    ExecutionResult save(DataBinding binding, WfVariable variable);

}
