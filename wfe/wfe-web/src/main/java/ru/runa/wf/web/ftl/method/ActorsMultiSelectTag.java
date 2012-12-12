package ru.runa.wf.web.ftl.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Charsets;

import freemarker.template.TemplateModelException;

public class ActorsMultiSelectTag extends AjaxFreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() throws TemplateModelException {
        try {
            String variableName = getParameterAs(String.class, 0);
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("VARIABLE", variableName);
            StringBuffer html = new StringBuffer();
            html.append(exportScript("scripts/ActorsMultiSelectTag.js", substitutions));

            html.append("<div id=\"actorsMultiSelect").append(variableName).append("\"><div id=\"actorsMultiSelectCnt").append(variableName)
                    .append("\"></div><div id=\"actorsMultiSelectAddButton\"><a href=\"javascript:{}\" id=\"btnAdd").append(variableName)
                    .append("\">[ + ]</a></div></div>");
            return html.toString();
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String displayFormat = getParameterAs(String.class, 1);
        String resultFormat = getParameterAs(String.class, 2);
        boolean byLogin = "login".equals(displayFormat);
        StringBuffer json = new StringBuffer("[");
        String hint = request.getParameter("hint");
        List<Executor> executors = getActors(subject, byLogin, hint);
        if (executors.size() == 0) {
            json.append("{\"code\": \"\", \"name\": \"\"}");
        }
        for (Executor executor : executors) {
            if (executor instanceof Actor) {
                Actor actor = (Actor) executor;
                if (json.length() > 10) {
                    json.append(", ");
                }
                Object data;
                if ("login".equals(resultFormat)) {
                    data = actor.getName();
                } else if ("email".equals(resultFormat)) {
                    data = actor.getEmail();
                } else if ("fio".equals(resultFormat)) {
                    data = actor.getFullName();
                } else {
                    data = actor.getCode();
                }
                json.append("{\"code\": \"").append(data).append("\", \"name\": \"");
                if (byLogin) {
                    json.append(actor.getName());
                } else {
                    json.append(actor.getFullName());
                }
                json.append("\"}");
            }
        }
        json.append("]");
        response.getOutputStream().write(json.toString().getBytes(Charsets.UTF_8));
    }

    private List<Executor> getActors(Subject subject, boolean byLogin, String hint) throws TemplateModelException {
        try {
            ExecutorService executorService = DelegateFactory.getExecutorService();
            BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
            if (hint.length() > 0) {
                int filterIndex = byLogin ? 0 : 1;
                Map<Integer, FilterCriteria> filterFieldsMap = batchPresentation.getFilteredFields();
                StringFilterCriteria filterCriteriaEnd = (StringFilterCriteria) FilterCriteriaFactory.getFilterCriteria(batchPresentation,
                        filterIndex);
                filterCriteriaEnd.applyFilterTemplates(new String[] { hint + "%" });
                filterFieldsMap.put(filterIndex, filterCriteriaEnd);
            }
            // this method used instead of getActors due to lack paging in that
            // method
            return executorService.getAll(subject, batchPresentation);
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

}
