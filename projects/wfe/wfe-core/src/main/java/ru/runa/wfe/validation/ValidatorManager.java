package ru.runa.wfe.validation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.validation.impl.ValidatorFileParser;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Preconditions;

public class ValidatorManager {
    private static final Log LOG = LogFactory.getLog(ValidatorManager.class);
    private static Map<String, String> validators = new HashMap<String, String>();

    private static ValidatorManager instance;

    public static synchronized ValidatorManager getInstance() {
        if (instance == null) {
            instance = new ValidatorManager();
        }
        return instance;
    }

    static {
        registerValidatorDefinitions("validators.xml");
    }

    private static void registerValidatorDefinitions(String resourceName) {
        try {
            InputStream is = ClassLoaderUtil.getAsStreamNotNull(resourceName, ValidatorManager.class);
            validators.putAll(ValidatorFileParser.parseValidatorDefinitions(is));
        } catch (Exception e) {
            LOG.error("check validator definition " + resourceName, e);
        }
    }

    private static Validator createValidator(ValidatorConfig config) throws Exception {
        String className = validators.get(config.getType());
        Preconditions.checkNotNull(className, "There is no validator class mapped to the name '" + config.getType() + "'");
        Validator validator = ClassLoaderUtil.instantiate(className);
        validator.init(config.getParams());
        validator.setValidatorType(config.getType());
        validator.setMessage(config.getMessage());
        return validator;
    }

    public synchronized List<Validator> getValidators(InputStream is) {
        List<ValidatorConfig> configs = ValidatorFileParser.parseValidatorConfigs(is);
        ArrayList<Validator> validators = new ArrayList<Validator>(configs.size());
        for (ValidatorConfig cfg : configs) {
            try {
                validators.add(createValidator(cfg));
            } catch (Exception e) {
                throw new InternalApplicationException("Unable to load validator class " + cfg, e);
            }
        }
        return validators;
    }

    public ValidatorContext validate(InputStream is, IVariableProvider variableProvider) {
        ValidatorContext validatorContext = new ValidatorContext(variableProvider);
        List<Validator> validators = getValidators(is);
        for (Validator validator : validators) {
            validator.setValidatorContext(validatorContext);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Running validator: " + validator);
            }
            try {
                validator.validate();
            } catch (Exception e) {
                throw new ValidationException(e);
            }
        }
        return validatorContext;
    }

}
