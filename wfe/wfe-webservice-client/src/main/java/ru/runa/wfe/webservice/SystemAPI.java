
package ru.runa.wfe.webservice;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2-12/14/2009 02:16 PM(ramkris)-
 * Generated source version: 2.2
 * 
 */
@WebService(name = "SystemAPI", targetNamespace = "http://impl.service.wfe.runa.ru/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface SystemAPI {


    /**
     * 
     * @param user
     * @param localizations
     */
    @WebMethod
    @RequestWrapper(localName = "saveLocalizations", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.SaveLocalizations")
    @ResponseWrapper(localName = "saveLocalizationsResponse", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.SaveLocalizationsResponse")
    public void saveLocalizations(
        @WebParam(name = "user", targetNamespace = "")
        User user,
        @WebParam(name = "localizations", targetNamespace = "")
        List<Localization> localizations);

    /**
     * 
     * @param user
     * @return
     *     returns java.util.List<ru.runa.wfe.webservice.Localization>
     */
    @WebMethod
    @WebResult(name = "result", targetNamespace = "")
    @RequestWrapper(localName = "getLocalizations", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.GetLocalizations")
    @ResponseWrapper(localName = "getLocalizationsResponse", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.GetLocalizationsResponse")
    public List<Localization> getLocalizations(
        @WebParam(name = "user", targetNamespace = "")
        User user);

    /**
     * 
     * @param name
     * @param user
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(name = "result", targetNamespace = "")
    @RequestWrapper(localName = "getLocalized", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.GetLocalized")
    @ResponseWrapper(localName = "getLocalizedResponse", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.GetLocalizedResponse")
    public String getLocalized(
        @WebParam(name = "user", targetNamespace = "")
        User user,
        @WebParam(name = "name", targetNamespace = "")
        String name);

    /**
     * 
     * @param user
     */
    @WebMethod
    @RequestWrapper(localName = "login", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.Login")
    @ResponseWrapper(localName = "loginResponse", targetNamespace = "http://impl.service.wfe.runa.ru/", className = "ru.runa.wfe.webservice.LoginResponse")
    public void login(
        @WebParam(name = "user", targetNamespace = "")
        User user);

}
