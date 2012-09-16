package ru.runa.af.webservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.security.auth.Subject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.ActorPrincipal;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.af.logic.ProfileLogic;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;

import com.google.common.collect.Lists;

@Stateless
@WebService(name = "Profile", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "ProfileWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class ProfileBean {
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ProfileLogic profileLogic;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "BatchPresentationTypeDescr", namespace = "http://runa.ru/workflow/webservices")
    public static class BatchPresentationTypeDescr {

        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String name;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String actorName;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String batchId;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String batchName;

        public BatchPresentationTypeDescr() {
        };

        public String getName() {
            return name;
        }

        public String getActorName() {
            return actorName;
        }

        public String getBatchId() {
            return batchId;
        }

        public String getBatchName() {
            return batchName;
        }
    }

    @WebMethod
    public void replicateBatchPresentation(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actor,
            @WebParam(mode = Mode.IN, name = "batchName", targetNamespace = "http://runa.ru/workflow/webservices") String batchPresentationNewName,
            @WebParam(mode = Mode.IN, name = "useTemplates", targetNamespace = "http://runa.ru/workflow/webservices") String useTemplatesParam,
            @WebParam(mode = Mode.IN, name = "activeMode", targetNamespace = "http://runa.ru/workflow/webservices") String activeModeParam,
            @WebParam(mode = Mode.IN, name = "batchPresentations", targetNamespace = "http://runa.ru/workflow/webservices") List<BatchPresentationTypeDescr> batchPresentations)
            throws ExecutorOutOfDateException, AuthenticationException, AuthorizationException,
            BatchPresentationNotFoundException {
        Subject subject = getSubject(actor);
        boolean useTemplates = isTemplatesActive(useTemplatesParam);
        setActiveMode activeMode = readSetActiveMode(activeModeParam);

        BatchPresentation srcBatch = null;
        Set<BatchPresentation> replaceableBatchPresentations = new HashSet<BatchPresentation>();
        for (BatchPresentationTypeDescr batchPresentation : batchPresentations) {
            String name = batchPresentation.getName();
            if (name.equals("source")) {
                if (srcBatch != null) {
                    throw new InternalApplicationException("Only one source batchPresentation is allowed inside replicateBatchPresentation.");
                }
                srcBatch = readBatchPresentation(subject, batchPresentation);
                continue;
            }
            if (name.equals("template")) {
                replaceableBatchPresentations.add(readBatchPresentation(subject, batchPresentation));
                continue;
            }
            throw new InternalApplicationException("BatchPresentation with name '" + name + "' is not allowed inside replicateBatchPresentation.");
        }

        if (srcBatch == null) {
            throw new InternalApplicationException("No source BatchPresentation in replicateBatchPresentation found.");
        }

        if (batchPresentationNewName == null || batchPresentationNewName.equals("")) {
            batchPresentationNewName = srcBatch.getBatchPresentationName();
        }

        Map<BatchPresentation, ReplicationDescr> replicationDescr = new HashMap<BatchPresentation, ReplicationDescr>();
        srcBatch = srcBatch.clone();
        srcBatch.setBatchPresentationName(batchPresentationNewName);
        replicationDescr.put(srcBatch, new ReplicationDescr(replaceableBatchPresentations, activeMode, useTemplates));
        replicateBatchPresentation(subject, replicationDescr);
    }

    private void replicateBatchPresentation(Subject subject, Map<BatchPresentation, ReplicationDescr> replicationDescr)
            throws AuthenticationException, AuthorizationException, ExecutorOutOfDateException, BatchPresentationNotFoundException {
        if (replicationDescr.isEmpty()) {
            return;
        }
        List<Actor> allActors = executorLogic.getActors(subject, ru.runa.af.presentation.AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation());
        List<Long> actorIds = Lists.newArrayListWithExpectedSize(allActors.size());
        for (Actor actor : allActors) {
            actorIds.add(actor.getId());
        }
        List<Profile> profiles = profileLogic.getProfile(subject, actorIds);
        // For all profiles
        for (Profile profile : profiles) {
            // Replicate all batches
            for (BatchPresentation replicateMe : replicationDescr.keySet()) {
                boolean useTemplates = replicationDescr.get(replicateMe).useTemplates;
                setActiveMode activeMode = replicationDescr.get(replicateMe).setActive;
                Set<BatchPresentation> templates = replicationDescr.get(replicateMe).templates;

                if (useTemplates
                        && !isBatchReplaceNeeded(getBatchFromProfile(profile, replicateMe.getBatchPresentationId(), replicateMe
                                .getBatchPresentationName()), templates)) {
                    if (activeMode.equals(setActiveMode.all)
                            && getBatchFromProfile(profile, replicateMe.getBatchPresentationId(), replicateMe.getBatchPresentationName()) != null) {
                        profile.setActiveBatchPresentation(replicateMe.getBatchPresentationId(), replicateMe.getBatchPresentationName());
                    }
                    continue;
                }
                BatchPresentation clon = replicateMe.clone();
                clon.setBatchPresentationName(replicateMe.getBatchPresentationName());
                profile.addBatchPresentation(clon);
                if (activeMode.equals(setActiveMode.all) || activeMode.equals(setActiveMode.changed)) {
                    profile.setActiveBatchPresentation(replicateMe.getBatchPresentationId(), replicateMe.getBatchPresentationName());
                }
            }
        }
        profileLogic.saveProfile(subject, profiles);
    }

    private enum setActiveMode {
        all, changed, none
    };

    private static class ReplicationDescr {

        private final Set<BatchPresentation> templates;
        private final setActiveMode setActive;
        private final boolean useTemplates;

        public ReplicationDescr(Set<BatchPresentation> templates, setActiveMode setActive, boolean useTemplates) {
            this.templates = templates;
            this.setActive = setActive;
            this.useTemplates = useTemplates;
        }
    }

    private boolean isBatchReplaceNeeded(BatchPresentation batch, Collection<BatchPresentation> templates) {
        if (batch == null) {
            return true;
        }
        for (BatchPresentation template : templates) {
            if (template.fieldEquals(batch)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTemplatesActive(String mode) {
        if (mode != null && mode.equals("no")) {
            return false;
        }
        return true;
    }

    private setActiveMode readSetActiveMode(String mode) {
        if (mode != null && mode.equals("all")) {
            return setActiveMode.all;
        }
        if (mode != null && mode.equals("changed")) {
            return setActiveMode.changed;
        }
        return setActiveMode.none;
    }

    private BatchPresentation readBatchPresentation(Subject subject, BatchPresentationTypeDescr batchPresentationTypeDescr)
            throws ExecutorOutOfDateException, AuthenticationException, AuthorizationException {
        String actorName = batchPresentationTypeDescr.getActorName();
        String batchName = batchPresentationTypeDescr.getBatchName();
        String batchId = batchPresentationTypeDescr.getBatchId();
//        if ((actorName == null || actorName.equals("")) && (batchName == null || batchName.equals(""))) {
//            if (ru.runa.wf.presentation.WFProfileStrategy.PROCESS_TASK_BATCH_PRESENTATION_ID.equals(batchId)) {
//                return ru.runa.wf.presentation.WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
//                        BatchPresentationConsts.DEFAULT_NAME, batchId);
//            } else if (ru.runa.wf.presentation.WFProfileStrategy.PROCESS_INSTANCE_BATCH_PRESENTATION_ID.equals(batchId)) {
//                return ru.runa.wf.presentation.WFProfileStrategy.PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
//                        BatchPresentationConsts.DEFAULT_NAME, batchId);
//            } else if (ru.runa.wf.presentation.WFProfileStrategy.PROCESS_DEFINITION_BATCH_PRESENTATION_ID.equals(batchId)) {
//                return ru.runa.wf.presentation.WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
//                        BatchPresentationConsts.DEFAULT_NAME, batchId);
//            } else {
//                return ru.runa.wf.presentation.WFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
//                        BatchPresentationConsts.DEFAULT_NAME, batchId);
//            }
//        }
        return getBatchFromProfile(profileLogic.getProfile(subject, executorLogic.getExecutor(subject, actorName).getId()), batchId, batchName);
    }

    private BatchPresentation getBatchFromProfile(Profile profile, String batchID, String batchName) {
        try {
            for (BatchPresentation batch : profile.getBatchPresentations(batchID)) {
                if (batch.getBatchPresentationName().equals(batchName)) {
                    return batch;
                }
            }
        } catch (BatchPresentationNotFoundException e) {
            return null;
        }
        return null;
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }
}
