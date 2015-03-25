package ru.runa.wfe.office.storage;

import java.util.List;
import java.util.Properties;

import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.dto.WfVariable;

public interface StoreService {

    public static final String PROP_CONSTRAINTS = "constaraints";
    public static final String PROP_PATH = "path";
    public static final String PROP_FORMAT = "format";

    void createFileIfNotExist(String path) throws Exception;

    ExecutionResult findAll(Properties properties) throws Exception;

    ExecutionResult findByFilter(Properties properties, List<ConditionItem> conditions) throws Exception;

    void update(Properties properties, WfVariable variable, List<? extends ConditionItem> conditions) throws Exception;

    void delete(Properties properties, WfVariable variable, List<ConditionItem> conditions) throws Exception;

    void save(Properties properties, List<?> records, boolean appendTo) throws Exception;

    void save(Properties properties, WfVariable variable, boolean appendTo) throws Exception;

}
