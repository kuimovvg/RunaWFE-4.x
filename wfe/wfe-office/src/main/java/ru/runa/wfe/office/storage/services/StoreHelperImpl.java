package ru.runa.wfe.office.storage.services;

import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.StoreServiceImpl;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.binding.QueryType;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;

public class StoreHelperImpl implements StoreHelper {

    private static final Log log = LogFactory.getLog(StoreHelperImpl.class);

    StoreService storeService;

    DataBindings config;
    VariableFormat format;

    public StoreHelperImpl() {
        storeService = new StoreServiceImpl();
    }

    @Override
    public void setConfig(DataBindings config) {
        this.config = config;
    }

    @Override
    public void setVariableFormat(VariableFormat format) {
        this.format = format;
    }

    @Override
    public Object execute(DataBinding binding, WfVariable variable, QueryType queryType) {
        ExecutionResult toReturn = null;
        if (queryType.equals(QueryType.CREATE)) {
            toReturn = save(binding, variable);
        } else if (queryType.equals(QueryType.READ)) {
            toReturn = findByFilter(binding);
        } else if (queryType.equals(QueryType.UPDATE)) {
            toReturn = update(binding);
        } else {
            toReturn = delete(binding);
        }
        if (toReturn == null) {
            return null;
        }
        return toReturn.getValue();
    }

    @Override
    public ExecutionResult findAll(DataBinding binding) {
        try {
            return storeService.findAll(extractProperties(binding));
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    public ExecutionResult findByFilter(DataBinding binding) {
        try {
            return storeService.findByFilter(extractProperties(binding), binding.getConditions());
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    public ExecutionResult update(DataBinding binding) {
        try {
            return storeService.update(extractProperties(binding), binding.getConditions());
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult save(DataBinding binding, WfVariable variable) {
        try {
            List<Object> records = null;
            if (format instanceof ListFormat) {
                records = (List<Object>) variable.getValue();
            } else {
                records = Lists.newArrayList(variable.getValue());
            }
            return storeService.save(extractProperties(binding), records, false);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    public ExecutionResult delete(DataBinding binding) {
        Properties properties = extractProperties(binding);
        try {
            return storeService.delete(properties, binding.getConditions());
        } catch (Exception e) {
            log.error("", e);
            return null;
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
