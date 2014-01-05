package ru.runa.wfe.var.format;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Executor;

public class ExecutorFormat implements VariableFormat {

    @Override
    public Class<? extends Executor> getJavaClass() {
        return Executor.class;
    }

    @Override
    public String getName() {
        return "executor";
    }
    
    @Override
    public Executor parse(String source) throws Exception {
        if (source != null && source.startsWith("{")) {
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(source);
            if (object.containsKey("name")) {
                return TypeConversionUtil.convertTo(Executor.class, object.get("name"));
            }
            if (object.containsKey("id")) {
                return TypeConversionUtil.convertTo(Executor.class, "ID" + object.get("id"));
            }
            throw new InternalApplicationException("Neither 'id' or 'name' attribute found in " + source);
        }
        return TypeConversionUtil.convertTo(Executor.class, source);
    }

    @Override
    public String format(Object object) {
        if (object == null) {
            return null;
        }
        Executor executor = (Executor) object;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", executor.getId());
        jsonObject.put("name", executor.getName());
        jsonObject.put("fullName", executor.getFullName());
        return jsonObject.toString();
        //return ((Executor) object).getName();
    }

    public static void main(String[] args) {
        String json = "{\"test\":55,\"fileName\": \"sample.doc\"}";
        try {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(json);
            System.out.println(object.getClass());
            System.out.println(object);
        } catch (Exception e) {
            System.out.println("Unable to parse '" + json + "'");
            e.printStackTrace();
        }

    }
}
