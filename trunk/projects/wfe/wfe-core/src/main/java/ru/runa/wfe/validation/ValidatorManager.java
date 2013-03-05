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
import ru.runa.wfe.user.User;
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

    private static Validator createValidator(User user, ValidatorConfig config, ValidatorContext validatorContext, IVariableProvider variableProvider) {
        String className = validators.get(config.getType());
        Preconditions.checkNotNull(className, "There is no validator class mapped to the name '" + config.getType() + "'");
        Validator validator = ApplicationContextFactory.createAutowiredBean(className);
        validator.init(user, config, validatorContext, variableProvider);
        return validator;
    }

    public synchronized List<Validator> createValidators(User user, byte[] validationXml, ValidatorContext validatorContext,
            IVariableProvider variableProvider) {
        List<ValidatorConfig> configs = ValidatorFileParser.parseValidatorConfigs(validationXml);
        ArrayList<Validator> validators = new ArrayList<Validator>(configs.size());
        for (ValidatorConfig config : configs) {
            validators.add(createValidator(user, config, validatorContext, variableProvider));
        }
        return validators;
    }

    public ValidatorContext validate(User user, byte[] validationXml, IVariableProvider variableProvider) {
        ValidatorContext validatorContext = new ValidatorContext();
        List<Validator> validators = createValidators(user, validationXml, validatorContext, variableProvider);
        for (Validator validator : validators) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Running validator: " + validator);
            }
            try {
                validator.validate();
            } catch (Throwable th) {
                LOG.error("validator " + validator, th);
                // TODO localize
                validator.addError("Internal error");
            }
        }
        return validatorContext;
    }

}
