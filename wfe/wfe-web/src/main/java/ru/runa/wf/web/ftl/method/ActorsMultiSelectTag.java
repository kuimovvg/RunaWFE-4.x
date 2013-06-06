package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.AjaxJsonFreemarkerTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

import freemarker.template.TemplateModelException;

@SuppressWarnings("unchecked")
public class ActorsMultiSelectTag extends AjaxJsonFreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("VARIABLE", variableName);
        StringBuffer html = new StringBuffer();

        List<String> previouslySubmittedValues = variableProvider.getValue(List.class, variableName);
        if (previouslySubmittedValues == null) {
            previouslySubmittedValues = new ArrayList<String>();
        }
        substitutions.put("START_COUNTER", String.valueOf(previouslySubmittedValues.size()));

        html.append(exportScript("scripts/ActorsMultiSelectTag.js", substitutions, false));

        html.append("<div id=\"actorsMultiSelect_").append(variableName).append("\">");
        html.append("<div id=\"actorsMultiSelectCnt_").append(variableName).append("\">");
        for (int i = 0; i < previouslySubmittedValues.size(); i++) {
            String divId = "div_" + variableName + i;
            String e = "<div id='" + divId + "'>";
            String code = previouslySubmittedValues.get(i);
            e += "<input type='hidden' name='" + variableName + "' value='" + code + "' /> " + getDisplayName(code);
            e += " <a href='javascript:{}' onclick='$(\"#" + divId + "\").remove();'>[ X ]</a>";
            e += "</div>";
            html.append(e);
        }
        html.append("</div>");
        html.append("<div id=\"actorsMultiSelectAddButton_").append(variableName).append("\">");
        html.append("<a href=\"javascript:{}\" id=\"btnAdd_").append(variableName).append("\">[ + ]</a>");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    private String getDisplayName(String code) throws TemplateModelException {
        Executor executor = TypeConversionUtil.convertToExecutor(code, new DelegateExecutorLoader(user));
        String displayFormat = getParameterAs(String.class, 1);
        return "login".equals(displayFormat) ? executor.getName() : executor.getFullName();
    }

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        String displayFormat = getParameterAs(String.class, 1);
        Group group = getParameterAs(Group.class, 2);
        boolean byLogin = "login".equals(displayFormat);
        String hint = request.getParameter("hint");
        List<Actor> actors = getActors(group, byLogin, hint);
        if (actors.size() == 0) {
            jsonArray.add(createJsonObject(null, ""));
        }
        for (Actor actor : actors) {
            jsonArray.add(createJsonObject(actor.getId(), byLogin ? actor.getName() : actor.getFullName()));
        }
        return jsonArray;
    }

    private JSONObject createJsonObject(Long id, String name) {
        JSONObject object = new JSONObject();
        object.put("id", id != null ? "ID" + id : "");
        object.put("name", name);
        return object;
    }

    private List<Actor> getActors(Group group, boolean byLogin, String hint) {
        int rangeSize = 50;
        List<Actor> actors = Lists.newArrayListWithExpectedSize(rangeSize);
        if (group != null) {
            List<Actor> groupActors = Delegates.getExecutorService().getGroupActors(user, group);
            for (Actor actor : groupActors) {
                if (byLogin) {
                    if (actor.getName().startsWith(hint)) {
                        actors.add(actor);
                    }
                } else {
                    if (actor.getFullName().startsWith(hint)) {
                        actors.add(actor);
                    }
                }
            }
            Collections.sort(actors);
            if (actors.size() > rangeSize) {
                return actors.subList(0, rangeSize);
            }
            return actors;
        } else {
            BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createDefault();
            batchPresentation.setRangeSize(rangeSize);
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
            if (hint.length() > 0) {
                int filterIndex = byLogin ? 0 : 1;
                batchPresentation.getFilteredFields().put(filterIndex, new StringFilterCriteria(hint + StringFilterCriteria.ANY_SYMBOLS));
            }
            actors.addAll((Collection<? extends Actor>) Delegates.getExecutorService().getExecutors(user, batchPresentation));
        }
        return actors;
    }

}
