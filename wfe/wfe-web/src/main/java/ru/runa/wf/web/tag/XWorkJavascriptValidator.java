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
package ru.runa.wf.web.tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.validation.Validator;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.validation.ValidatorManager;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.base.Charsets;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class XWorkJavascriptValidator {

    public static String getJavascript(byte[] validationXmlBytes) {
        try {
            InputStream is = ClassLoaderUtil.getResourceAsStream("wfform-validate.ftl", XWorkJavascriptValidator.class);
            Configuration cfg = new Configuration();
            Template template = new Template("validate", new InputStreamReader(is), cfg);
            template.setEncoding(Charsets.UTF_8.name());

            Map<String, Object> model = new HashMap<String, Object>();
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("validate", Boolean.TRUE);
            parameters.put("performValidation", Boolean.TRUE);
            parameters.put("id", WFFormTag.FORM_NAME);

            Set<String> tagNames = new HashSet<String>();

            List<Validator> allValidators = ValidatorManager.getInstance().getValidators(new ByteArrayInputStream(validationXmlBytes));
            for (Validator validator : allValidators) {
                if (validator instanceof FieldValidator) {
                    tagNames.add(((FieldValidator) validator).getFieldName());
                }
            }
            parameters.put("tagNames", tagNames);

            model.put("parameters", parameters);
            model.put("tag", new ValidatorCallback(allValidators));

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            template.process(model, new OutputStreamWriter(result, Charsets.UTF_8));

            return new String(result.toByteArray(), Charsets.UTF_8);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static class ValidatorCallback {
        List<Validator> all;

        public ValidatorCallback(List<Validator> all) {
            this.all = all;
            IVariableProvider variableProvider = new MapDelegableVariableProvider(new HashMap<String, Object>(), null);
            ValidatorContext validatorContext = new ValidatorContext(variableProvider);
            for (Validator validator : all) {
                validator.setValidatorContext(validatorContext);
            }
        }

        public List<Validator> getValidators(String name) {
            List<Validator> validators = new ArrayList<Validator>();
            for (Validator validator : all) {
                if (validator instanceof FieldValidator) {
                    if (((FieldValidator) validator).getFieldName().equals(name)) {
                        validators.add(validator);
                    }
                }
            }
            return validators;
        }
    }
}
