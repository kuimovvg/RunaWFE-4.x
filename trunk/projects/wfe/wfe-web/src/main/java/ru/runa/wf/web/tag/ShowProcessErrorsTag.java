package ru.runa.wf.web.tag;

import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;

import com.google.common.collect.Lists;

public class ShowProcessErrorsTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getStartElement() throws JspException {
        return new StringElement();
    }

    @Override
    protected ConcreteElement getEndElement() throws JspException {
        List<TR> rows = Lists.newArrayList();
        for (Map.Entry<Long, Map<String, Throwable>> processEntry : ProcessExecutionErrors.getProcessErrors().entrySet()) {
            for (Map.Entry<String, Throwable> taskEntry : processEntry.getValue().entrySet()) {
                TR tr = new TR();
                tr.addElement(new TD().addElement(processEntry.getKey().toString()).setClass(Resources.CLASS_LIST_TABLE_TD));
                tr.addElement(new TD().addElement(taskEntry.getKey()).setClass(Resources.CLASS_LIST_TABLE_TD));
                tr.addElement(new TD().addElement(taskEntry.getValue().getLocalizedMessage()).setClass(Resources.CLASS_LIST_TABLE_TD));
                rows.add(tr);
            }
        }
        ErrorsHeaderBuilder tasksHistoryHeaderBuilder = new ErrorsHeaderBuilder();
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        return tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder);
    }

    private class ErrorsHeaderBuilder implements HeaderBuilder {

        @Override
        public TR build() {
            TR tr = new TR();
            tr.addElement(new TH(Messages.getMessage("errors.process.id", pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage("errors.task.name", pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage("errors.error", pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

}
