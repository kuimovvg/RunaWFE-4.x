package ru.cg.runaex.esb.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.namespace.QName;

import ru.cg.runaex.esb.bean.StartProcessInstanceRequest;
import ru.cg.runaex.esb.bean.Variable;

/**
 * @author Петров А.
 */
public class StartProcessInstanceServiceClientTest {

  public static void main(String[] args) throws MalformedURLException {
    URL wsdlUrl = new URL("http://localhost:8080/runaex-esb/http/RunaexServices/StartProcessInstanceService?wsdl");
    StartProcessInstanceServiceClient service = new StartProcessInstanceServiceClient(wsdlUrl, new QName("http://runaex", "StartProcessInstanceServiceService"));

    StartProcessInstanceRequest request = new StartProcessInstanceRequest();
    request.setProcessName("walrus");

    ArrayList<Variable> variables = new ArrayList<Variable>(2);
    Variable variable = new Variable();
    variable.setName("param");
    variable.setStringValue("pampam");
    variables.add(variable);
    variable = new Variable();
    variable.setName("longParam");
    variable.setLongValue(1L);

    request.setVariables(variables);

    System.out.println(service.getStartProcessInstancePort().startProcessInstance(request));
  }

}
