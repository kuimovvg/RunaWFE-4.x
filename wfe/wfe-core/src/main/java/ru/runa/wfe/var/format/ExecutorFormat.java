package ru.runa.wfe.var.format;

import java.util.HashMap;

import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class ExecutorFormat extends VariableFormat implements VariableDisplaySupport {

    @Override
    public Class<? extends Executor> getJavaClass() {
        return Executor.class;
    }

    @Override
    public String getName() {
        return "executor";
    }

    @Override
    protected Executor convertFromStringValue(String source) {
        return TypeConversionUtil.convertTo(Executor.class, source);
    }

    @Override
    protected String convertToStringValue(Object object) {
        return ((Executor) object).getName();
    }

    @Override
    protected Object convertFromJSONValue(Object jsonValue) {
        JSONObject object = (JSONObject) jsonValue;
        if (object.containsKey("name")) {
            return TypeConversionUtil.convertTo(Executor.class, object.get("name"));
        }
        if (object.containsKey("id")) {
            return TypeConversionUtil.convertTo(Executor.class, "ID" + object.get("id"));
        }
        throw new InternalApplicationException("Neither 'id' or 'name' attribute found in " + object);
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        Executor executor = (Executor) value;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", executor.getId());
        jsonObject.put("name", executor.getName());
        jsonObject.put("fullName", executor.getFullName());
        return jsonObject;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        Executor executor = (Executor) object;
        boolean link = false;
        try {
            link = ApplicationContextFactory.getPermissionDAO().isAllowed(user, Permission.READ, executor);
        } catch (Exception e) {
            LogFactory.getLog(getClass()).warn("Unable to determine permission", e);
        }
        if (link) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", executor.getId());
            String href = webHelper.getActionUrl("/manage_executor", params);
            return "<a href=\"" + href + "\">" + executor.getLabel() + "</a>";
        } else {
            return executor.getLabel();
        }
    }
}
