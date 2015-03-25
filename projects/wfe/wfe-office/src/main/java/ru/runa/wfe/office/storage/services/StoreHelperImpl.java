package ru.runa.wfe.office.storage.services;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreOperation;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.StoreServiceImpl;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.binding.QueryType;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Maps;

public class StoreHelperImpl implements StoreHelper {

    private static final Log log = LogFactory.getLog(StoreHelperImpl.class);

    private Map<QueryType, Method> invocationMap = Maps.newHashMap();

    StoreService storeService;

    DataBindings config;

    VariableFormat format;

    public StoreHelperImpl(DataBindings config) {
        setConfig(config);
        registerHandlers();
        storeService = new StoreServiceImpl();        
    }

    private void setConfig(DataBindings config) {
        this.config = config;
    }

    @Override
    public void setVariableFormat(VariableFormat format) {
        this.format = format;
    }

    @Override
    public ExecutionResult execute(DataBinding binding, WfVariable variable) {
        try {
            Method method = invocationMap.get(config.getQueryType());
            return (ExecutionResult) method.invoke(this, binding, variable);
        } catch (Exception e) {
            log.error("", e);
            return ExecutionResult.EMPTY;
        }
    }

    @StoreOperation(QueryType.CREATE)
    public ExecutionResult save(DataBinding binding, WfVariable variable) throws Exception {
        storeService.save(extractProperties(binding), variable, false);
        return ExecutionResult.EMPTY;
    }

    @StoreOperation(QueryType.READ)
    public ExecutionResult findByFilter(DataBinding binding, WfVariable variable) throws Exception {
        return storeService.findByFilter(extractProperties(binding), binding.getConditions());
    }

    @StoreOperation(QueryType.UPDATE)
    public ExecutionResult update(DataBinding binding, WfVariable variable) throws Exception {
        storeService.update(extractProperties(binding), variable, binding.getConditions());
        return ExecutionResult.EMPTY;
    }

    @StoreOperation(QueryType.DELETE)
    public ExecutionResult delete(DataBinding binding, WfVariable variable) throws Exception {
        storeService.delete(extractProperties(binding), variable, binding.getConditions());
        return ExecutionResult.EMPTY;
    }

    private void registerHandlers() {
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            StoreOperation annotation = method.getAnnotation(StoreOperation.class);
            if (annotation != null) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters == null || parameters.length < 1) {
                    log.warn("wrong parameters");
                    continue;
                }
                if (!invocationMap.containsKey(annotation.value())) {
                    invocationMap.put(annotation.value(), method);
                }
            }
        }
    }

    private Properties extractProperties(DataBinding binding) {
        Properties properties = new Properties();
        properties.setProperty(StoreService.PROP_PATH, config.getInputFilePath());
        properties.put(StoreService.PROP_CONSTRAINTS, binding.getConstraints());
        properties.put(StoreService.PROP_FORMAT, format);
        return properties;
    }
}
