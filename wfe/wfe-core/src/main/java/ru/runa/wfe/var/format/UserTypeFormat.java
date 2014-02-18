package ru.runa.wfe.var.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableDefinitionAware;
import ru.runa.wfe.var.VariableUserType;

public class UserTypeFormat extends VariableFormat implements VariableDefinitionAware, VariableDisplaySupport {
    private static final Log log = LogFactory.getLog(UserTypeFormat.class);
    private VariableDefinition variableDefinition;

    @Override
    public Class<?> getJavaClass() {
        return ComplexVariable.class;
    }

    @Override
    public String getName() {
        return buildFormatDescriptor(variableDefinition).toString();
    }

    private static JSONObject buildFormatDescriptor(VariableDefinition variableDefinition) {
        JSONObject result = new JSONObject();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            Object value;
            if (attributeDefinition.isComplex()) {
                value = buildFormatDescriptor(attributeDefinition);
            } else {
                value = FormatCommons.create(attributeDefinition).getName();
            }
            result.put(attributeDefinition.getName(), value);
        }
        return result;
    }

    @Override
    protected ComplexVariable convertFromStringValue(String source) {
        throw new UnsupportedOperationException("complex variable cannot be deserializes from string");
    }

    @Override
    protected String convertToStringValue(Object obj) {
        // TODO
        return String.valueOf(obj);
    }

    @Override
    protected ComplexVariable convertFromJSONValue(Object jsonValue) {
        JSONObject object = (JSONObject) jsonValue;
        ComplexVariable result = new ComplexVariable();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            try {
                VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
                Object attributeValue = object.get(attributeDefinition.getName());
                if (attributeValue != null) {
                    attributeValue = attributeFormat.convertFromJSONValue(attributeValue);
                    result.put(attributeDefinition.getName(), attributeValue);
                }
            } catch (Exception e) {
                log.warn(attributeDefinition.toString(), e);
            }
        }
        return result;
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        ComplexVariable complexVariable = (ComplexVariable) value;
        JSONObject object = new JSONObject();
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
            Object attributeValue = complexVariable.get(attributeDefinition.getName());
            if (attributeValue != null) {
                attributeValue = TypeConversionUtil.convertTo(attributeFormat.getJavaClass(), attributeValue);
                attributeValue = attributeFormat.convertToJSONValue(attributeValue);
                object.put(attributeDefinition.getName(), attributeValue);
            }
        }
        return object;
    }

    @Override
    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    @Override
    public void setVariableDefinition(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        ComplexVariable complexVariable = (ComplexVariable) object;
        StringBuffer b = new StringBuffer();
        b.append("<table class=\"list\">");
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            b.append("<tr>");
            b.append("<td class=\"list\">").append(attributeDefinition.getName()).append("</td>");
            // TODO make option?
            // b.append("<td class=\"list\">").append(attributeDefinition.getFormatLabel()).append("</td>");
            VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
            Object attributeValue = complexVariable.get(attributeDefinition.getName());
            b.append("<td class=\"list\">");
            if (attributeValue != null) {
                Object value;
                if (attributeFormat instanceof VariableDisplaySupport) {
                    String childName = name + VariableUserType.DELIM + attributeDefinition.getName();
                    value = ((VariableDisplaySupport) attributeFormat).formatHtml(user, webHelper, processId, childName, attributeValue);
                } else {
                    value = attributeFormat.format(attributeValue);
                }
                b.append(value);
            }
            b.append("</td>");
            b.append("</tr>");
        }
        b.append("</table>");
        return b.toString();
    }

}
