package ru.cg.runaex.esb.bean;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import ru.cg.runaex.esb.client.StartProcessInstanceServiceClient;

/**
 * @author Петров А.
 */
public class StartProcessInstanceEndpoint {

  private StartProcessInstanceServiceClient serviceClient;

  public StartProcessInstanceEndpoint(String endpointName) throws MalformedURLException {
    serviceClient = new StartProcessInstanceServiceClient(new URL("http://localhost:8180/runaex-esb/http/RunaexServices/".concat(endpointName).concat("-StartProcessInstanceService?wsdl")), new QName("http://runaex", "StartProcessInstanceServiceService"));
  }

  public StartProcessInstanceResult startProcessInstance(StartProcessInstanceRequest request) {
    request.setEndpointName(null);
    return serviceClient.getStartProcessInstancePort().startProcessInstance(request);
  }
}
