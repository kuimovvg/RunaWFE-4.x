package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

public class ActorFormat implements VariableFormat<Actor>, VariableDisplaySupport<Actor> {

    @Override
    public Class<Actor> getJavaClass() {
        return Actor.class;
    }

    @Override
    public Actor parse(String[] source) throws Exception {
        return TypeConversionUtil.convertTo(Actor.class, source[0]);
    }

    @Override
    public String format(Actor object) {
        return object.getFullName();
    }

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, Actor value) {
        return FormatCommons.getVarOut(user, value, webHelper, processId, name, 0, null);
    }

}
