package ru.runa.bp.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.webservice.types.NamedValue;

import ru.runa.alfresco.AlfSerializerDesc;
import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.WSObjectAccessor;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.extension.handler.ParamDef;

public class AlfGetObjectPropertiesAsStringsByUuid extends AlfHandler {
    private static final String DEFAULT_FORMAT_CLASS = String.class.getName();

    @Override
    protected void executeAction(AlfSession session, AlfHandlerData alfHandlerData) throws Exception {
        Map<String, ParamDef> outputParams = alfHandlerData.getOutputParams();
        String uuid = alfHandlerData.getInputParamValue(String.class, "uuid");
        if (uuid != null) {
            NamedValue[] props = session.loadObjectProperties(uuid);
            for (NamedValue namedValue : props) {
                int index = namedValue.getName().lastIndexOf('}');
                String keyName = namedValue.getName().substring(index + 1);
                if (outputParams.containsKey(keyName)) {
                    String targetClassName = alfHandlerData.getInputParamValue(String.class, keyName, DEFAULT_FORMAT_CLASS);
                    Object value = getProperty(namedValue, null, targetClassName);
                    String outputVarName = alfHandlerData.getOutputParams().get(keyName).getVariableName();
                    alfHandlerData.setOutputVariable(outputVarName, value);
                }
            }
        } else {
            log.warn("uuid is null in " + this);
        }
    }

    /**
     * @see WSObjectAccessor
     */
    private Object getProperty(NamedValue prop, AlfSerializerDesc desc, String targetClassName) throws Exception {
        String stringValue = prop.getValue();
        Class<?> targetClass = Class.forName(targetClassName);
        if (stringValue == null) {
            return " ";
        }
        if (targetClass == Date.class) {
            Calendar c = Calendar.getInstance();
            c.setTime(ISO8601DateFormat.parse(stringValue));
            return CalendarUtil.formatDateTime(c);
        }
        return stringValue;
    }

}