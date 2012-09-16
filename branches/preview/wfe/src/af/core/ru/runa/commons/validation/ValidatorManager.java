/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.commons.validation;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Preconditions;

import ru.runa.InternalApplicationException;
import ru.runa.commons.ClassLoaderUtil;

public class ValidatorManager {
    private static final Log LOG = LogFactory.getLog(ValidatorManager.class);
    private static ValidatorManager instance = new ValidatorManager();
    protected static final String VALIDATION_CONFIG_SUFFIX = "-validation.xml";
    private static final Map<String, SoftReference<List<ValidatorConfig>>> validatorCache = new HashMap<String, SoftReference<List<ValidatorConfig>>>();
    private static Map<String, String> validators = new HashMap<String, String>();

    static {
        registerValidatorDefinitions("ru/runa/validators/default.xml");
        registerValidatorDefinitions("validators.xml");
    }

    public static ValidatorManager getInstance() {
        return instance;
    }

    private static void registerValidatorDefinitions(String resourceName) {
        try {
            InputStream is = ClassLoaderUtil.getResourceAsStream(resourceName, ValidatorManager.class);
            if (is != null) {
                Map<String, String> validators = ValidatorFileParser.parseValidatorDefinitions(is);
                for (Map.Entry<String, String> mapping : validators.entrySet()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Registering validator of class " + mapping.getValue() + " with name " + mapping.getKey());
                    }
                    validators.put(mapping.getKey(), mapping.getValue());
                }
            }
        } catch (Exception e) {
            LOG.error("check validator definition " + resourceName, e);
        }
    }

    private static Validator createValidator(ValidatorConfig config) throws ValidationException {
        String className = validators.get(config.getType());
        Preconditions.checkNotNull(className, "There is no validator class mapped to the name '" + config.getType() + "'");
        try {
            Class<? extends Validator> clazz = ClassLoaderUtil.loadClass(className, ValidatorManager.class);
            Validator validator = clazz.newInstance();
            validator.init(config.getParams());
            validator.setValidatorType(config.getType());
            validator.setMessage(config.getMessage());
            return validator;
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to load validator class " + className, e);
        }
    }

    public synchronized List<Validator> getValidators(String validatorKey, InputStream is) throws ValidationException {
        if (!validatorCache.containsKey(validatorKey) || validatorCache.get(validatorKey).get() == null) {
            validatorCache.put(validatorKey, new SoftReference<List<ValidatorConfig>>(ValidatorFileParser.parseValidatorConfigs(is)));
        }
        List<ValidatorConfig> configs = validatorCache.get(validatorKey).get();
        ArrayList<Validator> validators = new ArrayList<Validator>(configs.size());
        for (ValidatorConfig cfg : configs) {
            validators.add(createValidator(cfg));
        }
        return validators;
    }

    public ValidatorContext validate(String key, InputStream is, Map<String, ? extends Object> variables) throws ValidationException {
        ValidatorContext validatorContext = new ValidatorContext(variables);
        List<Validator> validators = getValidators(key, is);

        for (Validator validator : validators) {
            validator.setValidatorContext(validatorContext);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Running validator: " + validator + " for object " + variables);
            }
            validator.validate();
        }
        return validatorContext;
    }

}
