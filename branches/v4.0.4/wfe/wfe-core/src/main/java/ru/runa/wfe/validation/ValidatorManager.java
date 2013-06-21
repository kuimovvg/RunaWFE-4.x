package ru.runa.wfe.validation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.impl.ValidatorFileParser;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Preconditions;

public class ValidatorManager {
    private static final Log log = LogFactory.getLog(ValidatorManager.class);
    private static Map<String, String> validators = new HashMap<String, String>();
    private static final String CONFIG = "validators.xml";

    private static ValidatorManager instance;

    public static synchronized ValidatorManager getInstance() {
        if (instance == null) {
            instance = new ValidatorManager();
        }
        return instance;
    }

    static {
        registerDefinitions(CONFIG, true);
        registerDefinitions(SystemProperties.RESOURCE_EXTENSION_PREFIX + CONFIG, false);
    }

    private static void registerDefinitions(String resourceName, boolean required) {
        try {
            InputStream is;
            if (required) {
                is = ClassLoaderUtil.getAsStreamNotNull(resourceName, ValidatorManager.class);
            } else {
                is = ClassLoaderUtil.getAsStream(resourceName, ValidatorManager.class);
            }
            if (is != null) {
                validators.putAll(ValidatorFileParser.parseValidatorDefinitions(is));
            }
        } catch (Exception e) {
            log.error("check validator definition " + resourceName, e);
        }
    }

    private static Validator createValidator(User user, ProcessDefinition processDefinition, ValidatorConfig config,
            ValidatorContext validatorContext, Map<String, Object> variables, IVariableProvider variableProvider) {
        String className = validators.get(config.getType());
        Preconditions.checkNotNull(className, "There is no validator class mapped to the name '" + config.getType() + "'");
        Validator validator = ApplicationContextFactory.createAutowiredBean(className);
        validator.init(user, processDefinition, config, validatorContext, variables, variableProvider);
        return validator;
    }

    public List<Validator> createValidators(User user, ProcessDefinition processDefinition, byte[] validationXml, ValidatorContext validatorContext,
            Map<String, Object> variables, IVariableProvider variableProvider) {
        List<ValidatorConfig> configs = ValidatorFileParser.parseValidatorConfigs(validationXml);
        ArrayList<Validator> validators = new ArrayList<Validator>(configs.size());
        for (ValidatorConfig config : configs) {
            validators.add(createValidator(user, processDefinition, config, validatorContext, variables, variableProvider));
        }
        return validators;
    }

    public ValidatorContext validate(User user, ProcessDefinition processDefinition, byte[] validationXml, Map<String, Object> variables,
            IVariableProvider variableProvider) {
        ValidatorContext validatorContext = new ValidatorContext();
        List<Validator> validators = createValidators(user, processDefinition, validationXml, validatorContext, variables, variableProvider);
        for (Validator validator : validators) {
            if (log.isDebugEnabled()) {
                log.debug("Running validator: " + validator);
            }
            try {
                validator.validate();
            } catch (Throwable th) {
                log.error("validator " + validator, th);
                // TODO localize
                validator.addError("Internal error");
            }
        }
        return validatorContext;
    }

}
