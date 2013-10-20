package ru.cg.runaex.database.util;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.cg.jul.core.net.HostPortSource;

/**
 * @author urmancheev
 */
public class JndiUrlSource extends HostPortSource implements UrlSource {
  private String jndiName;
  private String path;

  public JndiUrlSource() {
  }

  public JndiUrlSource(String jndiName) {
    this.jndiName = jndiName;
  }

  public String getJndiName() {
    return jndiName;
  }

  @Override
  public String getPath() {
    return path;
  }

  public void setJndiName(String jndiName) throws NamingException {
    this.jndiName = jndiName;

    Context initCtx = new InitialContext();
    Object obj = initCtx.lookup(jndiName);
    if (obj instanceof Properties) {
      Properties properties = (Properties) obj;
      setHost(properties.getProperty("host"));
      setPort(Integer.valueOf(properties.getProperty("port")));
      path = properties.getProperty("path");
    }
    else {
      throw new NamingException("object must be " + Properties.class.getName());
    }
  }
}
