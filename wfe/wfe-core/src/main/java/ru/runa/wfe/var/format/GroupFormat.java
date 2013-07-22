package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Group;

public class GroupFormat implements VariableFormat<Group> {

    @Override
    public Class<? extends Group> getJavaClass() {
        return Group.class;
    }

    @Override
    public Group parse(String[] source) throws Exception {
        return TypeConversionUtil.convertTo(Group.class, source[0]);
    }

    @Override
    public String format(Group object) {
        return object.getName();
    }

}
