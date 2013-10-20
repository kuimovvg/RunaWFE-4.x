package ru.cg.runaex.groovy.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.stereotype.Component;

import ru.cg.runaex.database.bean.model.ProcessDbConnection;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.groovy.classimport.GroovyClassImport;
import ru.cg.runaex.groovy.executor.GroovyScriptExecutor;

/**
 * @author Петров А.
 */
@Component
public class GroovyScriptExecutorFactoryImpl implements GroovyScriptExecutorFactory {

  private static final Pattern GROOVY_FUNCTION_DEFINITION_PATTERN = Pattern.compile("(def)\\s+([^\\s]+?)(\\s*?\\()");
  private static final String PREDEFINED_FUNCTIONS_CLASS_NAME = "PredefinedFunctions";

  @Override
  public GroovyScriptExecutor create(String predefinedProjectFunctionsScript, String projectName, Long processDefinitionId) throws IOException, ClassNotFoundException {
    CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
    compilerConfiguration.setSourceEncoding("UTF-8");

    compilerConfiguration.getClasspath().add(getTempDirectory());
    compilerConfiguration.getClasspath().add(GroovyScriptExecutorFactoryStaticResources.COMMON_FUNCTIONS_CLASSPATH);

    compilerConfiguration.addCompilationCustomizers(GroovyScriptExecutorFactoryStaticResources.COMMON_IMPORT_CUSTOMIZER);

    ProcessDbConnection projectDatabaseConnectionInfo = getProjectDatabaseConnectionInfo(processDefinitionId);

    if (predefinedProjectFunctionsScript != null) {
      ImportCustomizer importCustomizer = new ImportCustomizer();
      processPredefinedFunctionsScript(importCustomizer, predefinedProjectFunctionsScript, projectName, projectDatabaseConnectionInfo.getJndiName());
      compilerConfiguration.addCompilationCustomizers(importCustomizer);
    }

    addJdbcDriverToClassPath(compilerConfiguration, projectDatabaseConnectionInfo.getDriverClassName());

    return new GroovyScriptExecutor(compilerConfiguration);
  }

  private static String getTempDirectory() {
    String tmpDir = System.getProperty("java.io.tmpdir");

    if (!tmpDir.endsWith("/") && !tmpDir.endsWith("\\")) {
      tmpDir = tmpDir.concat(File.separator);
    }

    return tmpDir;
  }

  private static void processPredefinedFunctionsScript(ImportCustomizer importCustomizer, String predefinedFunctionScript, String projectName, String dataSourceJndiName) throws IOException {
    addStaticImports(importCustomizer, predefinedFunctionScript, projectName);

    predefinedFunctionScript = prepareScript(predefinedFunctionScript, projectName, dataSourceJndiName);

    writeScriptToTempDirectory(predefinedFunctionScript, projectName);
  }

  private static void addStaticImports(ImportCustomizer importCustomizer, String predefinedFunctionsScript, String projectName) {
    List<String> predefinedFunctionNames = extractFunctions(predefinedFunctionsScript);

    String className = PREDEFINED_FUNCTIONS_CLASS_NAME.concat(projectName);
    for (String predefinedFunctionName : predefinedFunctionNames) {
      importCustomizer.addStaticImport(className, predefinedFunctionName);
    }
  }

  private static List<String> extractFunctions(String predefinedProjectFunctionsCode) {
    List<String> result = new LinkedList<String>();

    Matcher functionNameMatcher = GROOVY_FUNCTION_DEFINITION_PATTERN.matcher(predefinedProjectFunctionsCode);

    while (functionNameMatcher.find()) {
      result.add(functionNameMatcher.group(2));
    }

    return result;
  }

  private static String prepareScript(String predefinedFunctionsScript, String projectName, String dataSourceJndiName) {
    predefinedFunctionsScript = MessageFormat.format(predefinedFunctionsScript, dataSourceJndiName);
    predefinedFunctionsScript = GROOVY_FUNCTION_DEFINITION_PATTERN.matcher(predefinedFunctionsScript).replaceAll("static $2$3");

    StringBuilder stringBuilder = new StringBuilder();

    for (GroovyClassImport groovyClassImport : GroovyScriptExecutorFactoryStaticResources.IMPORTS) {
      stringBuilder.append("import ").append(groovyClassImport.getClassName()).append(";\n");
    }

    stringBuilder.append("class ").append(PREDEFINED_FUNCTIONS_CLASS_NAME.concat(projectName))
        .append(" {\n")
        .append(predefinedFunctionsScript)
        .append("}");

    return stringBuilder.toString();
  }

  private static void writeScriptToTempDirectory(String predefinedFunctionsScript, String projectName) throws IOException {
    String filename = PREDEFINED_FUNCTIONS_CLASS_NAME.concat(projectName).concat(".groovy");

    FileOutputStream fos = new FileOutputStream(getTempDirectory().concat(filename));
    IOUtils.write(predefinedFunctionsScript.getBytes("UTF-8"), fos);
    fos.flush();
    fos.close();
  }

  private ProcessDbConnection getProjectDatabaseConnectionInfo(Long processDefinitionId) {
    return DatabaseSpringContext.getMetadataDao().getProjectDatabaseConnectionInfo(processDefinitionId);
  }

  private void addJdbcDriverToClassPath(CompilerConfiguration compilerConfiguration, String jdbcDriverClassName) throws ClassNotFoundException {
    String pathToDriverClass = Class.forName(jdbcDriverClassName).getProtectionDomain().getCodeSource().getLocation().getPath();
    compilerConfiguration.getClasspath().add(pathToDriverClass);
  }
}
