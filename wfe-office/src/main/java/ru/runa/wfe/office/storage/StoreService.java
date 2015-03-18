package ru.runa.wfe.office.storage;

import java.util.List;
import java.util.Properties;

import ru.runa.wfe.office.storage.binding.ExecutionResult;

public interface StoreService {

    public static final String PROP_CONSTRAINTS = "constaraints";
    public static final String PROP_PATH = "path";
    public static final String PROP_FORMAT = "format";

    void createFile(Properties properties) throws Exception;

    ExecutionResult findAll(Properties properties) throws Exception;

    ExecutionResult findByFilter(Properties properties, List<ConditionItem> conditions) throws Exception;

    ExecutionResult update(Properties properties, List<? extends ConditionItem> conditions) throws Exception;

    ExecutionResult delete(Properties properties, List<ConditionItem> conditions) throws Exception;

    ExecutionResult save(Properties properties, List<?> records, boolean appendTo) throws Exception;

}
