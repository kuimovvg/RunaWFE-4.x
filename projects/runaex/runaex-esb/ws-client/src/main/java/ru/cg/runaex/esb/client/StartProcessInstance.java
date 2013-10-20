package ru.cg.runaex.esb.client;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import ru.cg.runaex.esb.bean.StartProcessInstanceRequest;
import ru.cg.runaex.esb.bean.StartProcessInstanceResult;

/**
 * @author Петров А.
 */
@WebService(name = "StartProcessInstance", targetNamespace = "http://runaex")
public interface StartProcessInstance {

  @WebMethod
  @WebResult(name = "startProcessInstanceResult", targetNamespace = "http://runaex")
  public StartProcessInstanceResult startProcessInstance(
      @WebParam(name = "startProcessInstanceRequest", targetNamespace = "http://runaex")
      StartProcessInstanceRequest startProcessInstanceRequest);
}
