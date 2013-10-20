package ru.cg.runaex.database.util;

import java.lang.reflect.Type;

import com.google.gson.*;

/**
 * @author Kochetkov
 */
public class GsonUtil {
  /**
   * Made for backward compatible with database.
   * Long values was written as "null" string in db, to parse them we need to use this method
   *
   * @return the Gson object
   */
  public static Gson getGsonObject() {
    return new GsonBuilder().registerTypeAdapter(Long.class, new JsonDeserializer() {
      @Override
      public Object deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
          if ("null".equals(element.getAsString())) {
            return null;
          }
          return Long.parseLong(element.getAsString());
        }
        catch (NumberFormatException e) {
          throw new JsonParseException(e);
        }
      }
    }).create();
  }

  public static <T> T getObjectFromJson(String json, Class<T> type) {
    return getGsonObject().fromJson(json, type);
  }

  public static String toJson(Object o) {
    return getGsonObject().toJson(o);
  }
}
