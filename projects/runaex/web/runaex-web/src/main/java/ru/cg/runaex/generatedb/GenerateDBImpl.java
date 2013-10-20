package ru.cg.runaex.generatedb;

import java.util.Properties;

import com.cg.jul.core.resources.properties.dao.CachedPropertiesSource;

/**
 * Date: 14.08.12
 * Time: 9:41
 *
 * @author Sabirov
 */
public abstract class GenerateDBImpl implements GenerateDB {

  private static Properties sqlTemplateCode;

  protected GenerateDBImpl() {
    if (sqlTemplateCode == null) {
      String propertiesPath = "/sql_template_code.xml";
      sqlTemplateCode = new CachedPropertiesSource().getProperties(propertiesPath);
    }
  }

  /**
   * Get simple class name
   * @return simple class name
   */
  protected String getSimpleName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public String getSQL() {
    return sqlTemplateCode.getProperty(getSimpleName());
  }

  public String getSQLFieldComment() {
    return "";
  }

  public String getSQLReferences() {
    return "";
  }

  public String getSQLTemplate(String key) {
    return sqlTemplateCode.getProperty(key);
  }
}
