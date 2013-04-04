package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.GroupFormat;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;

public class ViewUtil {

    public static String createExecutorSelect(User user, WfVariable variable) {
        ExecutorService executorService = Delegates.getExecutorService();
        BatchPresentation batchPresentation;
        if (ActorFormat.class.getName().equals(variable.getFormatClassNameNotNull())) {
            batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
        } else if (ExecutorFormat.class.getName().equals(variable.getFormatClassNameNotNull())) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        } else if (GroupFormat.class.getName().equals(variable.getFormatClassNameNotNull())) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        } else {
            throw new InternalApplicationException("Unexpected format " + variable.getFormatClassNameNotNull());
        }
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        List<Executor> executors = (List<Executor>) executorService.getExecutors(user, batchPresentation);

        String html = "<select name=\"" + variable.getDefinition().getName() + "\">";
        for (Executor executor : executors) {
            html += "<option value=\"ID" + executor.getId() + "\"";
            if (Objects.equal(executor, variable.getValue())) {
                html += " selected";
            }
            String value = executor instanceof Actor ? executor.getFullName() : executor.getName();
            html += ">" + value + "</option>";
        }
        html += "</select>";
        return html;
    }

    public static String getVariableValueHtml(User user, WebHelper webHelper, Long processId, WfVariable variable) {
        VariableFormat<Object> format = variable.getFormatNotNull();
        if (format instanceof VariableDisplaySupport) {
            if (webHelper == null || processId == null) {
                return "";
            }
            VariableDisplaySupport<Object> displaySupport = (VariableDisplaySupport<Object>) format;
            return displaySupport.getHtml(user, webHelper, processId, variable.getDefinition().getName(), variable.getValue());
        } else {
            return format.format(variable.getValue());
        }
    }
}
