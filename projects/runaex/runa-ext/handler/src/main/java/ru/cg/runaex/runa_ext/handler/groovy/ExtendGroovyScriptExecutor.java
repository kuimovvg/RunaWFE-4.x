package ru.cg.runaex.runa_ext.handler.groovy;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Throwables;
import groovy.lang.Binding;
import org.codehaus.groovy.GroovyExceptionInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.IVariableProvider;

import ru.cg.runaex.groovy.util.GroovySpringContext;


/**
 * Расширенный GroovyScriptExecutor с поддержкой import
 * <p/>
 * Для того чтобы добавить новый импорт надо
 * 1. Создать GroovyClassImport, пример new GroovyClassImport("Sql", "groovy.sql.Sql")
 * 2. Убедится, что добавленный класс есть либо в runawfe.ear\lib\ или в модулях jboss(если в модулях jboss то
 * зависмость на этот модуль должен быть прописан в runawfe.ear\META-INF\jboss-deployment-structure.xml по аналогии
 * org.postgresql)
 * <jboss-deployment-structure>
 * <deployment>
 * <dependencies>
 * <module name="org.postgresql"/>
 * <module name="org.jboss.ironjacamar.jdbcadapters"/>
 * </dependencies>
 * </deployment>
 * </jboss-deployment-structure>
 *
 * @author Abdulin
 */
public class ExtendGroovyScriptExecutor extends GroovyScriptExecutor {
  private static final Logger log = LoggerFactory.getLogger(GroovyScriptExecutor.class);

  private ExecutionContext executionContext;

  public ExtendGroovyScriptExecutor(ExecutionContext executionContext) {
    super();
    this.executionContext = executionContext;
  }

  @Override
  public Map<String, Object> executeScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script) {
    try {
      Binding binding = createBinding(processDefinition, variableProvider);
      GroovySpringContext.getGroovyExecutorCache()
          .getExecutor(getProjectName(), getProcessDefinitionId())
          .executeScript(script, binding, getProcessId());

      return removeUnnecessaryVariables(binding.getVariables(), variableProvider);
    }
    catch (Exception e) {
      if (e instanceof GroovyExceptionInterface) {
        log.error("Groovy", e);
        throw new InternalApplicationException(e.getMessage());
      }
      throw Throwables.propagate(e);
    }
  }

  @Override
  public Object evaluateScript(ProcessDefinition processDefinition, IVariableProvider variableProvider, String script) {
    try {
      Binding binding = createBinding(processDefinition, variableProvider);
      return GroovySpringContext.getGroovyExecutorCache()
          .getExecutor(getProjectName(), getProcessDefinitionId())
          .executeScriptWithResult(script, binding, getProcessId());
    }
    catch (Exception e) {
      if (e instanceof GroovyExceptionInterface) {
        log.error("Groovy", e);
        throw new InternalApplicationException(e.getMessage());
      }
      throw Throwables.propagate(e);
    }
  }

  private String getProjectName() {
    return executionContext.getProcess().getDeployment().getCategory();
  }

  private Long getProcessDefinitionId() {
    return executionContext.getProcessDefinition().getId();
  }

  private Long getProcessId() {
    return executionContext.getProcess().getId();
  }

  private Map<String, Object> removeUnnecessaryVariables(Map<String, Object> map, IVariableProvider variableProvider) {
    Map<String, Object> processedResult = new HashMap<String, Object>();

    for (Map.Entry<String, Object> resultEntry : map.entrySet()) {
      if (variableProvider.getValue(resultEntry.getKey()) != null) {
        processedResult.put(resultEntry.getKey(), resultEntry.getValue());
      }
    }

    return processedResult;
  }
}