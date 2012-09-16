package ru.runa.bpm.ui.jpdl3;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.JpdlModelConverter;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Variable;

public class From2JpdlModelConverter implements JpdlModelConverter {

    private String newJpdlVersion;
    private StringBuffer log;

    public synchronized ProcessDefinition convert(ProcessDefinition oldDef, String newJpdlVersion) throws Exception {
        this.log = new StringBuffer("--- JPDL version update to ").append(newJpdlVersion);
        this.newJpdlVersion = newJpdlVersion;
        try {
            ProcessDefinition newDef = JpdlVersionRegistry.getElementTypeDefinition(newJpdlVersion, oldDef.getTypeName()).createElement();
            setProperties(oldDef, newDef, false);

            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.StartState.class, "start-state");
            addElements(oldDef, newDef, ru.runa.bpm.ui.jpdl2.model.TimerState.class, "task-node");
            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.WaitState.class, "waitState");
            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.Decision.class, "decision");
            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.Fork.class, "fork");
            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.Join.class, "join");
            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.Subprocess.class, "process-state");
            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.EndState.class, "end-state");
            addElements(oldDef, newDef, ru.runa.bpm.ui.common.model.Swimlane.class, "swimlane");

            List<Variable> oldVariables = oldDef.getChildren(Variable.class);
            for (Variable oldVariable : oldVariables) {
                Variable newVariable = new Variable(oldVariable);
                newDef.addChild(newVariable);
            }

            log.append(" ---");
            return newDef;
        } finally {
            DesignerLogger.logInfo(log.toString());
        }
    }

    private void addElements(ProcessDefinition oldDef, ProcessDefinition newDef, Class<? extends GraphElement> sourceClass, String targetTypeName)
            throws Exception {
        List<? extends GraphElement> sourceElements = oldDef.getChildren(sourceClass);
        for (GraphElement oldElement : sourceElements) {
            GraphElement newElement = JpdlVersionRegistry.getElementTypeDefinition(newJpdlVersion, targetTypeName).createElement();
            setProperties(oldElement, newElement, true);
            newDef.addChild(newElement);
        }
    }

    private static final String childrenFieldName = "childs";
    private static final List<String> fieldsToIgnore = new ArrayList<String>();
    static {
        fieldsToIgnore.add("parent");
        fieldsToIgnore.add("initialSize");
        fieldsToIgnore.add("listeners");
        fieldsToIgnore.add("typeName");

        fieldsToIgnore.add("dirtyListener");
        fieldsToIgnore.add("dirty");
        fieldsToIgnore.add("hasValidationErrors");
    }

    @SuppressWarnings("unchecked")
    private void setProperties(GraphElement source, GraphElement target, boolean copyChildren) throws Exception {
        Map<String, Object> sourceFieldValues = new HashMap<String, Object>();

        Class<? extends Object> sourceParent = source.getClass();
        while (sourceParent != null) {
            Field[] fields = sourceParent.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers()) && !fieldsToIgnore.contains(field.getName())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(source);
                        if (value != null) {
                            if (childrenFieldName.equals(field.getName())) {
                                if (copyChildren) {
                                    copyChildren(target, (List<GraphElement>) value);
                                }
                            } else {
                                sourceFieldValues.put(field.getName(), value);
                            }
                        }
                    } catch (NoSuchFieldException e) {
                        log.append("Ignoring field: ").append(field.getName()).append("; ");
                    }
                }
            }
            sourceParent = sourceParent.getSuperclass();
        }

        Class<? extends Object> targetParent = target.getClass();
        while (targetParent != null) {
            Field[] fields = targetParent.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers()) && !fieldsToIgnore.contains(field.getName())) {
                    try {
                        Field targetField = targetParent.getDeclaredField(field.getName());
                        targetField.setAccessible(true);
                        Object value = sourceFieldValues.get(field.getName());
                        if (value != null) {
                            targetField.set(target, value);
                            log.append("Setting field: '").append(field.getName()).append("' to value = '").append(value).append("'; ");
                        }
                    } catch (NoSuchFieldException e) {
                        log.append("Ignoring field: ").append(field.getName()).append("; ");
                    }
                }
            }
            targetParent = targetParent.getSuperclass();
        }
    }

    private void copyChildren(GraphElement target, List<GraphElement> childs) throws Exception {
        log.append("Copy children for element '").append(target.getTypeName()).append("'; ");
        for (GraphElement oldChild : childs) {
            GraphElement newChild = JpdlVersionRegistry.getElementTypeDefinition(newJpdlVersion, oldChild.getTypeName()).createElement();
            setProperties(oldChild, newChild, true);
            target.addChild(newChild);
        }
    }
}
