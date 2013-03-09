package ru.runa.wf.web.ftl.method;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.ftl.AjaxJsonFreemarkerTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

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
        html.append(exportScript("scripts/ActorsMultiSelectTag.js", substitutions, false));

        html.append("<div id=\"actorsMultiSelect_").append(variableName).append("\"><div id=\"actorsMultiSelectCnt_").append(variableName)
                .append("\"></div><div id=\"actorsMultiSelectAddButton_").append(variableName).append("\"><a href=\"javascript:{}\" id=\"btnAdd_")
                .append(variableName).append("\">[ + ]</a></div></div>");
        return html.toString();
    }

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        String displayFormat = getParameterAs(String.class, 1);
        Group group = getParameterAs(Group.class, 2);
        boolean byLogin = "login".equals(displayFormat);
        String hint = request.getParameter("hint");
        List<Actor> actors = getActors(user, group, byLogin, hint);
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

    private List<Actor> getActors(User user, Group group, boolean byLogin, String hint) {
        int rangeSize = 50;
        List<Actor> actors = Lists.newArrayListWithExpectedSize(rangeSize);
        ExecutorService executorService = Delegates.getExecutorService();
        if (group != null) {
            List<Actor> groupActors = executorService.getGroupActors(user, group);
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
        } else {
            BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createDefault();
            batchPresentation.setRangeSize(rangeSize);
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
            if (hint.length() > 0) {
                int filterIndex = byLogin ? 0 : 1;
                batchPresentation.getFilteredFields().put(filterIndex, new StringFilterCriteria(hint + "%"));
            }
            // thid method used instead of getActors due to lack paging in
            // that
            // method
            actors.addAll((Collection<? extends Actor>) executorService.getExecutors(user, batchPresentation));
        }
        return actors;
    }

}
