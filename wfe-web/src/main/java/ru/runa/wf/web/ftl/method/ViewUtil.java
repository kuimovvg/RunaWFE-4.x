package ru.runa.wf.web.ftl.method;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.GroupFormat;

import com.google.common.base.Objects;

public class ViewUtil {

    public static String createExecutorSelect(Subject subject, WfVariable variable) {
        ExecutorService executorService = Delegates.getExecutorService();
        BatchPresentation batchPresentation;
        if (ActorFormat.class.getName().equals(variable.getDefinition().getFormatClassName())) {
            batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
        } else if (ExecutorFormat.class.getName().equals(variable.getDefinition().getFormatClassName())) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        } else if (GroupFormat.class.getName().equals(variable.getDefinition().getFormatClassName())) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        } else {
            throw new InternalApplicationException("Unexpected format " + variable.getDefinition().getFormatClassName());
        }
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        List<Executor> executors = executorService.getAll(subject, batchPresentation);

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

}
