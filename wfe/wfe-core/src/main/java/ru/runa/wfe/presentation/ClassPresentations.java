package ru.runa.wfe.presentation;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.WfException;
import ru.runa.wfe.audit.SystemLogClassPresentation;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.relation.RelationClassPresentation;
import ru.runa.wfe.relation.RelationGroupClassPresentation;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.user.ActorClassPresentation;
import ru.runa.wfe.user.ExecutorClassPresentation;
import ru.runa.wfe.user.GroupClassPresentation;

public class ClassPresentations {
    private static final Map<Integer, ClassPresentation> map = new HashMap<Integer, ClassPresentation>();
    static {
        registerClassPresentation(ExecutorClassPresentation.getInstance());
        registerClassPresentation(ActorClassPresentation.getInstance());
        registerClassPresentation(GroupClassPresentation.getInstance());
        registerClassPresentation(DefinitionClassPresentation.getInstance());
        registerClassPresentation(ProcessClassPresentation.getInstance());
        registerClassPresentation(TaskClassPresentation.getInstance());
        registerClassPresentation(RelationClassPresentation.getInstance());
        registerClassPresentation(RelationGroupClassPresentation.getInstance());
        registerClassPresentation(SystemLogClassPresentation.getInstance());
    }

    private static void registerClassPresentation(ClassPresentation classPresentation) {
        map.put(getClassPresentationId(classPresentation.getPresentationClass()), classPresentation);
        map.put(getClassPresentationId(classPresentation.getClass()), classPresentation);
    }

    public static ClassPresentation getClassPresentation(Integer id) {
        ClassPresentation result = map.get(id);
        if (result == null) {
            throw new WfException("Failed to found ClassPresentation with id " + id);
        }
        return result;
    }

    public static ClassPresentation getClassPresentation(Class<?> classPresentationClass) {
        return getClassPresentation(getClassPresentationId(classPresentationClass));
    }

    public static int getClassPresentationId(ClassPresentation classPresentation) {
        return getClassPresentationId(classPresentation.getPresentationClass());
    }

    public static int getClassPresentationId(Class<?> classPresentationClass) {
        return classPresentationClass.getName().hashCode();
    }

}
