package ru.runa.wfe.var.format;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class UserTypeFormat extends VariableFormat implements VariableDisplaySupport {
    private static final Log log = LogFactory.getLog(UserTypeFormat.class);
    private final VariableUserType userType;

    public UserTypeFormat(VariableUserType userType) {
        Preconditions.checkNotNull(userType);
        this.userType = userType;
    }

    @Override
    public Class<?> getJavaClass() {
        return ComplexVariable.class;
    }

    @Override
    public String getName() {
        return JSONValue.toJSONString(buildFormatDescriptor(userType));
    }

    private static Map<String, Object> buildFormatDescriptor(VariableUserType userType) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            Object value;
            if (attributeDefinition.isComplex()) {
                value = buildFormatDescriptor(attributeDefinition.getUserType());
            } else {
                value = FormatCommons.create(attributeDefinition).getName();
            }
            map.put(attributeDefinition.getName(), value);
        }
        return map;
    }

    @Override
    protected ComplexVariable convertFromStringValue(String source) {
        return convertFromJSONValue(JSONValue.parse(source.replaceAll("&quot;", "\"")));
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return JSONValue.toJSONString(convertToJSONValue(obj));
    }

    @Override
    protected ComplexVariable convertFromJSONValue(Object jsonValue) {
        JSONObject object = (JSONObject) jsonValue;
        ComplexVariable result = new ComplexVariable(userType);
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            try {
                VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
                Object attributeValue = object.get(attributeDefinition.getName());
                if (attributeValue != null) {
                    attributeValue = attributeFormat.convertFromJSONValue(attributeValue);
                    result.put(attributeDefinition.getName(), attributeValue);
                }
            } catch (Exception e) {
                log.error(attributeDefinition.toString(), e);
            }
        }
        return result;
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        ComplexVariable complexVariable = (ComplexVariable) value;
        Map<Object, Object> map = Maps.newLinkedHashMap();
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
            Object attributeValue = complexVariable.get(attributeDefinition.getName());
            if (attributeValue != null) {
                attributeValue = TypeConversionUtil.convertTo(attributeFormat.getJavaClass(), attributeValue);
                attributeValue = attributeFormat.convertToJSONValue(attributeValue);
                map.put(attributeDefinition.getName(), attributeValue);
            }
        }
        return map;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        ComplexVariable complexVariable = (ComplexVariable) object;
        StringBuffer b = new StringBuffer();
        b.append("<table class=\"list usertype\">");
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
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

    public VariableUserType getUserType() {
        return userType;
    }

    @Override
    public String toString() {
        return userType.getName();
    }
}
