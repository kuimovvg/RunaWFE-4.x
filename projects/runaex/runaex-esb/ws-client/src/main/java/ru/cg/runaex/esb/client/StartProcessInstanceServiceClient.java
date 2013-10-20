package ru.cg.runaex.esb.client;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * @author Петров А.
 */
@WebServiceClient(name = "StartProcessInstanceServiceClient", targetNamespace = "http://runaex")
public class StartProcessInstanceServiceClient extends Service {

  public StartProcessInstanceServiceClient(URL wsdlLocation, QName serviceName) {
    super(wsdlLocation, serviceName);
  }

  /**
   * @return returns StartProcessInstance
   */
  @WebEndpoint(name = "StartProcessInstanceServicePort")
  public StartProcessInstance getStartProcessInstancePort() {
    return super.getPort(new QName("http://runaex", "StartProcessInstanceServicePort"), StartProcessInstance.class);
  }

  /**
   * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
   * @return returns StartProcessInstance
   */
  @WebEndpoint(name = "StartProcessInstanceServicePort")
  public StartProcessInstance getStartProcessInstancePort(WebServiceFeature... features) {
    return super.getPort(new QName("http://runaex", "StartProcessInstanceServicePort"), StartProcessInstance.class, features);
  }
}
