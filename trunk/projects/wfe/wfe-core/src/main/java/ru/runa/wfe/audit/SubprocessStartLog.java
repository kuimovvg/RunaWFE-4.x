package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.Node;

/**
 * Logging sub-process creation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "B")
public class SubprocessStartLog extends NodeEnterLog {
    private static final long serialVersionUID = 1L;

    public SubprocessStartLog() {
    }

    public SubprocessStartLog(Node node, Process subProcess) {
        super(node);
        addAttribute(ATTR_PROCESS_ID, subProcess.getId().toString());
    }

    @Transient
    public Long getSubprocessId() {
        return TypeConversionUtil.convertTo(getAttributeNotNull(ATTR_PROCESS_ID), Long.class);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ProcessIdValue(getSubprocessId()) };
    }

}
