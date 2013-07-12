package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class GroupFormat implements VariableFormat<Group>, VariableDisplaySupport<Group> {

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

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, Group value) {
        return FormatCommons.getVarOut(user, value, webHelper, processId, name, 0, null);
    }
}
