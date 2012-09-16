/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.webservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.InternalApplicationException;
import ru.runa.af.ActorPrincipal;
import ru.runa.af.AttributeRequiredException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.logic.ArchivingLogic;

@Stateless
@WebService(name = "Archiving", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "ArchivingWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class ArchivingBean {
    @Autowired
    private ArchivingLogic archLogic;
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @WebMethod
    public void removeOldProcessInstances(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String prInstName,
            @WebParam(mode = Mode.IN, name = "version", targetNamespace = "http://runa.ru/workflow/webservices") String prInstVersion,
            @WebParam(mode = Mode.IN, name = "id", targetNamespace = "http://runa.ru/workflow/webservices") String prInstId,
            @WebParam(mode = Mode.IN, name = "idTill", targetNamespace = "http://runa.ru/workflow/webservices") String prInstIdTill,
            @WebParam(mode = Mode.IN, name = "startDate", targetNamespace = "http://runa.ru/workflow/webservices") String prInstStartDate,
            @WebParam(mode = Mode.IN, name = "endDate", targetNamespace = "http://runa.ru/workflow/webservices") String prInstEndDate,
            @WebParam(mode = Mode.IN, name = "onlyFinished", targetNamespace = "http://runa.ru/workflow/webservices") String prInstOnlyFinished,
            @WebParam(mode = Mode.IN, name = "dateInterval", targetNamespace = "http://runa.ru/workflow/webservices") String prInstDateInterval)
            throws AuthenticationException, ProcessInstanceDoesNotExistException, SuperProcessInstanceExistsException,
            SecuredObjectOutOfDateException, AttributeRequiredException {
        operationWithOldProcessInstances(getSubject(actor), prInstName, prInstVersion, prInstId, prInstIdTill, prInstStartDate, prInstEndDate,
                prInstOnlyFinished, prInstDateInterval, 1);
    }

    @WebMethod
    public void removeOldProcessDefinitionVersion(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String defName,
            @WebParam(mode = Mode.IN, name = "version", targetNamespace = "http://runa.ru/workflow/webservices") String defVersion)
            throws NumberFormatException, AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException,
            SecuredObjectOutOfDateException {
        operationWithOldProcessDefinitionVersion(getSubject(actor), defName, defVersion, 1);
    }

    @WebMethod
    public void archiveOldProcessInstances(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String prInstName,
            @WebParam(mode = Mode.IN, name = "version", targetNamespace = "http://runa.ru/workflow/webservices") String prInstVersion,
            @WebParam(mode = Mode.IN, name = "id", targetNamespace = "http://runa.ru/workflow/webservices") String prInstId,
            @WebParam(mode = Mode.IN, name = "idTill", targetNamespace = "http://runa.ru/workflow/webservices") String prInstIdTill,
            @WebParam(mode = Mode.IN, name = "startDate", targetNamespace = "http://runa.ru/workflow/webservices") String prInstStartDate,
            @WebParam(mode = Mode.IN, name = "endDate", targetNamespace = "http://runa.ru/workflow/webservices") String prInstEndDate,
            @WebParam(mode = Mode.IN, name = "onlyFinished", targetNamespace = "http://runa.ru/workflow/webservices") String prInstOnlyFinished,
            @WebParam(mode = Mode.IN, name = "dateInterval", targetNamespace = "http://runa.ru/workflow/webservices") String prInstDateInterval)
            throws AuthenticationException, ProcessInstanceDoesNotExistException, SuperProcessInstanceExistsException,
            SecuredObjectOutOfDateException, AttributeRequiredException {
        operationWithOldProcessInstances(getSubject(actor), prInstName, prInstVersion, prInstId, prInstIdTill, prInstStartDate, prInstEndDate,
                prInstOnlyFinished, prInstDateInterval, 2);
    }

    @WebMethod
    public void archiveOldProcessDefinitionVersion(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String defName,
            @WebParam(mode = Mode.IN, name = "version", targetNamespace = "http://runa.ru/workflow/webservices") String defVersion)
            throws NumberFormatException, AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException,
            SecuredObjectOutOfDateException {
        operationWithOldProcessDefinitionVersion(getSubject(actor), defName, defVersion, 2);
    }

    @WebMethod
    public void retrieveOldProcessInstances(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String prInstName,
            @WebParam(mode = Mode.IN, name = "version", targetNamespace = "http://runa.ru/workflow/webservices") String prInstVersion,
            @WebParam(mode = Mode.IN, name = "id", targetNamespace = "http://runa.ru/workflow/webservices") String prInstId,
            @WebParam(mode = Mode.IN, name = "idTill", targetNamespace = "http://runa.ru/workflow/webservices") String prInstIdTill,
            @WebParam(mode = Mode.IN, name = "startDate", targetNamespace = "http://runa.ru/workflow/webservices") String prInstStartDate,
            @WebParam(mode = Mode.IN, name = "endDate", targetNamespace = "http://runa.ru/workflow/webservices") String prInstEndDate,
            @WebParam(mode = Mode.IN, name = "onlyFinished", targetNamespace = "http://runa.ru/workflow/webservices") String prInstOnlyFinished,
            @WebParam(mode = Mode.IN, name = "dateInterval", targetNamespace = "http://runa.ru/workflow/webservices") String prInstDateInterval)
            throws AuthenticationException, ProcessInstanceDoesNotExistException, SuperProcessInstanceExistsException,
            SecuredObjectOutOfDateException, AttributeRequiredException {
        operationWithOldProcessInstances(getSubject(actor), prInstName, prInstVersion, prInstId, prInstIdTill, prInstStartDate, prInstEndDate,
                prInstOnlyFinished, prInstDateInterval, 3);
    }

    @WebMethod
    public void retrieveOldProcessDefinitionVersion(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String defName,
            @WebParam(mode = Mode.IN, name = "version", targetNamespace = "http://runa.ru/workflow/webservices") String defVersion)
            throws NumberFormatException, AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException,
            SecuredObjectOutOfDateException {
        operationWithOldProcessDefinitionVersion(getSubject(actor), defName, defVersion, 3);
    }

    /* @param operation: remove - 1; archiving - 2; retrieve from archive - 3 */
    private void operationWithOldProcessInstances(Subject subject, String prInstName, String prInstVersion, String prInstId, String prInstIdTill,
            String prInstStartDate, String prInstEndDate, String prInstOnlyFinished, String prInstDateInterval, int operation)
            throws AuthenticationException, ProcessInstanceDoesNotExistException, SuperProcessInstanceExistsException,
            SecuredObjectOutOfDateException, AttributeRequiredException {
        boolean onlyFinished = prInstOnlyFinished == null || prInstOnlyFinished.trim().length() == 0 ? true : Boolean
                .parseBoolean(prInstOnlyFinished);
        boolean dateInterval = prInstDateInterval == null || prInstDateInterval.trim().length() == 0 ? false : Boolean
                .parseBoolean(prInstDateInterval);
        int version = prInstVersion == null || prInstVersion.trim().length() == 0 ? 0 : Integer.parseInt(prInstVersion);
        Long id = prInstId == null || prInstId.trim().length() == 0 ? null : Long.parseLong(prInstId);
        Long idTill = prInstIdTill == null || prInstIdTill.trim().length() == 0 ? null : Long.parseLong(prInstIdTill);

        Date startDate = null;
        Date finishDate = null;
        if (prInstId == null || prInstId.trim().length() == 0) {
            if ((prInstStartDate == null || prInstStartDate.trim().length() == 0) && (prInstEndDate == null || prInstEndDate.trim().length() == 0)) {
                throw new AttributeRequiredException("operationWithOldProcessInstances", "StartDate and EndDate");
            }
        }

        try {
            if (prInstStartDate != null && prInstStartDate.trim().length() > 0) {
                startDate = FORMAT.parse(prInstStartDate);
            }
            if (prInstEndDate != null && prInstEndDate.trim().length() > 0) {
                finishDate = FORMAT.parse(prInstEndDate);
            }
        } catch (ParseException e) {
            throw new InternalApplicationException(e);
        }

        switch (operation) {
        case 1:
            archLogic.removeProcessInstances(subject, startDate, finishDate, prInstName, version, id, idTill, onlyFinished, dateInterval);
            break;
        case 2:
            archLogic.archiveProcessInstances(subject, startDate, finishDate, prInstName, version, id, idTill, onlyFinished, dateInterval);
            break;
        case 3:
            archLogic.restoreProcessInstances(subject, startDate, finishDate, prInstName, version, id, idTill, onlyFinished, dateInterval);
            break;
        }
    }

    /* @param number: remove - 1; archiving - 2; retrieve from archive - 3 */
    private void operationWithOldProcessDefinitionVersion(Subject subject, String defName, String defVersion, int operation)
            throws NumberFormatException, AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException,
            SecuredObjectOutOfDateException {
        switch (operation) {
        case 1:
            archLogic.removeProcessDefinition(subject, defName, defVersion == null || defVersion.trim().length() == 0 ? 0 : Integer
                    .parseInt(defVersion), 0);
            break;
        case 2:
            archLogic.archiveProcessDefinition(subject, defName, defVersion == null || defVersion.trim().length() == 0 ? 0 : Integer
                    .parseInt(defVersion));
            break;
        case 3:
            archLogic.restoreProcessDefinitionFromArchive(subject, defName, defVersion == null || defVersion.trim().length() == 0 ? 0 : Integer
                    .parseInt(defVersion));
            break;
        }
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }
}
