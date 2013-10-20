package ru.cg.runaex.groovy.factory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.cg.runaex.groovy.classimport.GroovyClassImport;
import ru.cg.runaex.groovy.classimport.NonLoadGroovyClassImport;

/**
 * @author Петров А.
 */
public final class GroovyScriptExecutorFactoryStaticResources {

  private static Logger LOGGER = LoggerFactory.getLogger(GroovyScriptExecutorFactoryStaticResources.class);

  protected static final ImportCustomizer COMMON_IMPORT_CUSTOMIZER;
  protected static final String COMMON_FUNCTIONS_CLASSPATH;

  protected static GroovyClassImport[] IMPORTS = new GroovyClassImport[] {
      new NonLoadGroovyClassImport("Sql", "groovy.sql.Sql"),
      new NonLoadGroovyClassImport("Driver", "org.postgresql.Driver"),
      new NonLoadGroovyClassImport("Savepoint", "java.sql.Savepoint"),
      new NonLoadGroovyClassImport("DatabaseSpringContext", "ru.cg.runaex.database.context.DatabaseSpringContext"),
      new NonLoadGroovyClassImport("TxDataSourceProxyProvider", "ru.cg.runaex.database.provider.TxDataSourceProxyProvider"),
      new NonLoadGroovyClassImport("Context", "javax.naming.Context"),
      new NonLoadGroovyClassImport("InitialContext", "javax.naming.InitialContext"),
      new NonLoadGroovyClassImport("NamingException", "javax.naming.NamingException"),
      new NonLoadGroovyClassImport("DataSource", "javax.sql.DataSource"),
      new NonLoadGroovyClassImport("Connection", "java.sql.Connection")
  };

  //Init common predefined functions and classes
  static {
    COMMON_IMPORT_CUSTOMIZER = new ImportCustomizer();

    String classpath;
    try {
      classpath = getClassPath(IMPORTS);
    }
    catch (ClassNotFoundException e) {
      LOGGER.error(e.getMessage(), e);
      throw Throwables.propagate(e);
    }
    catch (SecurityException e) {
      LOGGER.error(e.getMessage(), e);
      throw Throwables.propagate(e);
    }

    COMMON_FUNCTIONS_CLASSPATH = classpath != null ? classpath : "";

    for (GroovyClassImport groovyClassImport : IMPORTS) {
      COMMON_IMPORT_CUSTOMIZER.addImport(groovyClassImport.getAlias(), groovyClassImport.getClassName());
    }
  }

  private GroovyScriptExecutorFactoryStaticResources() {

  }

  private static String getClassPath(GroovyClassImport[] imports) throws ClassNotFoundException {
    List<String> classes = new ArrayList<String>();
    for (GroovyClassImport groovyClassImport : imports) {
      if (groovyClassImport.loadClass() != null) {
        String path = groovyClassImport.loadClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (!classes.contains(path)) {
          classes.add(path);
        }
      }
    }

    StringBuilder path = new StringBuilder();
    for (String classPath : classes) {
      path.append(classPath).append(";");
    }

    return path.toString();
  }
}
