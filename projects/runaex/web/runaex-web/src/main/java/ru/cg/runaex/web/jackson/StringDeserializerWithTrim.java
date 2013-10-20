package ru.cg.runaex.web.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;

/**
 * @author urmancheev
 */
public class StringDeserializerWithTrim extends org.codehaus.jackson.map.deser.std.StringDeserializer {
  protected boolean enableTrim = true;

  public void setEnableTrim(boolean enableTrim) {
    this.enableTrim = enableTrim;
  }

  @Override
  public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    String value = super.deserialize(jp, ctxt);
    if (enableTrim && value != null && !value.isEmpty()) {
      value = value.trim();
      if (value.isEmpty())
        value = getEmptyValue();
    }
    return value;
  }
}
