package ru.runa.wf.web.html;

import java.util.Date;

import ru.runa.common.web.html.BaseDateTDBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;

public class DefinitionDeployedDateTDBuilder extends BaseDateTDBuilder<WfDefinition> {

    @Override
    protected Date getDate(WfDefinition object) {
        return object.getDeployedDate();
    }

}
