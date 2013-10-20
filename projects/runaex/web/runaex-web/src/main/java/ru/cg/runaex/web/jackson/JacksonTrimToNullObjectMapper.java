package ru.cg.runaex.web.jackson;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * @author urmancheev
 */
public class JacksonTrimToNullObjectMapper extends ObjectMapper {

  public JacksonTrimToNullObjectMapper() {
    super();
    SimpleModule module = new SimpleModule("TrimEnablingModule", new Version(1, 0, 0, null));
    module.addDeserializer(String.class, new StringDeserializerWithTrim());
    this.registerModule(module);
    _deserializationConfig = _deserializationConfig.with(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
  }

}
