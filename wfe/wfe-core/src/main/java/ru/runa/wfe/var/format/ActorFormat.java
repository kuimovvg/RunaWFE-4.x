package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Actor;

public class ActorFormat implements VariableFormat<Actor> {

    @Override
    public Class<Actor> getJavaClass() {
        return Actor.class;
    }

    @Override
    public Actor parse(String source) throws Exception {
        return TypeConversionUtil.convertTo(Actor.class, source);
    }

    @Override
    public String format(Actor object) {
        if (object == null) {
            return null;
        }
        return object.getName();
    }

}
