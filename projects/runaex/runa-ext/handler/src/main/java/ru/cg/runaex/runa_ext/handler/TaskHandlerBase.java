//package ru.cg.runaex.components.handler;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import ru.cg.runaex.database.context.DatabaseSpringContext;
//import ru.runa.af.AuthenticationException;
//import ru.runa.af.AuthorizationException;
//import ru.runa.af.ExecutorOutOfDateException;
//import ru.runa.wf.TaskDoesNotExistException;
//import ru.runa.wf.TaskStub;
//import ru.runa.wf.delegate.DelegateFactory;
//import ru.runa.wf.delegate.ExecutionServiceDelegate;
//import ru.runa.wf.form.ValidationException;
//import ru.runa.wf.logic.bot.TaskHandler;
//import ru.runa.wf.logic.bot.TaskHandlerException;
//
//import javax.security.auth.Subject;
//import java.nio.charset.Charset;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author urmancheev
// */
//public abstract class TaskHandlerBase implements TaskHandler {
//  private final Logger logger = LoggerFactory.getLogger(getClass());
//
//  public void configure(String configurationName) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void configure(byte[] bytes) throws TaskHandlerException {
//    String configuration = new String(bytes, Charset.forName("UTF-8"));
//    setConfiguration(configuration);
//  }
//
//  public abstract void setConfiguration(String configuration) throws TaskHandlerException;
//
//  protected Map<String, Object> getRunaVariables(Subject subject, TaskStub taskStub) throws TaskHandlerException {
////    TODO: Use CORRECT version of RunaWFE!
////    ExecutionServiceDelegate executionService = DatabaseSpringContext.getExecutionServiceDelegate();
////    try {
////      return executionService.getVariables(subject, taskStub.getId());
////    }
////    catch (TaskDoesNotExistException e) {
////      logger.error("Can not get runa variables.", e.getMessage());
////      throw new TaskHandlerException("Can not get runa variables.", e);
////    }
////    catch (AuthorizationException e) {
////      logger.error("Can not get runa variables.", e.getMessage());
////      throw new TaskHandlerException("Can not get runa variables.", e);
////    }
////    catch (AuthenticationException e) {
////      logger.error("Can not get runa variables.", e.getMessage());
////      throw new TaskHandlerException("Can not get runa variables.", e);
////    }
//    return new HashMap<String, Object>();
//  }
//
//  protected void completeTask(Subject subject, TaskStub taskStub) throws TaskHandlerException {
//    Map<String, Object> variables = new HashMap<String, Object>();
//    variables.put("botTaskFailed", false);
//    ExecutionServiceDelegate execution = DelegateFactory.getInstance().getExecutionServiceDelegate();
//
//    try {
//      execution.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variables);
//    }
//    catch (TaskDoesNotExistException e) {
//      logger.error("Failed to complete task.", e);
//      throw new TaskHandlerException("Failed to complete task.", e);
//    }
//    catch (AuthorizationException e) {
//      logger.error("Failed to complete task.", e);
//      throw new TaskHandlerException("Failed to complete task.", e);
//    }
//    catch (AuthenticationException e) {
//      logger.error("Failed to complete task.", e);
//      throw new TaskHandlerException("Failed to complete task.", e);
//    }
//    catch (ExecutorOutOfDateException e) {
//      logger.error("Failed to complete task.", e);
//      throw new TaskHandlerException("Failed to complete task.", e);
//    }
//    catch (ValidationException e) {
//      logger.error("Failed to complete task.", e);
//      throw new TaskHandlerException("Failed to complete task.", e);
//    }
//  }
//
//  protected void completeTaskWithError(Subject subject, TaskStub taskStub, String message) throws TaskHandlerException {
//    Map<String, Object> variables = new HashMap<String, Object>();
//    variables.put("botTaskFailed", true);
//    variables.put("botTaskFailedMessage", message);
//    ExecutionServiceDelegate execution = DelegateFactory.getInstance().getExecutionServiceDelegate();
//
//    try {
//      execution.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variables);
//    }
//    catch (TaskDoesNotExistException e) {
//      logger.error("Failed to complete failed task.", e);
//      throw new TaskHandlerException("Failed to complete failed task.", e);
//    }
//    catch (AuthorizationException e) {
//      logger.error("Failed to complete failed task.", e);
//      throw new TaskHandlerException("Failed to complete failed task.", e);
//    }
//    catch (AuthenticationException e) {
//      logger.error("Failed to complete failed task.", e);
//      throw new TaskHandlerException("Failed to complete failed task.", e);
//    }
//    catch (ExecutorOutOfDateException e) {
//      logger.error("Failed to complete failed task.", e);
//      throw new TaskHandlerException("Failed to complete failed task.", e);
//    }
//    catch (ValidationException e) {
//      logger.error("Failed to complete failed task.", e);
//      throw new TaskHandlerException("Failed to complete failed task.", e);
//    }
//  }
//
//}
