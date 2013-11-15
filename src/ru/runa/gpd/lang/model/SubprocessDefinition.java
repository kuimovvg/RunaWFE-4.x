package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.swimlane.SwimlaneGUIConfiguration;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

public class SubprocessDefinition extends ProcessDefinition {

    public SubprocessDefinition() {
        setAccessType(ProcessDefinitionAccessType.EmbeddedSubprocess);
    }
    
    @Override
    public SwimlaneDisplayMode getSwimlaneDisplayMode() {
        return SwimlaneDisplayMode.none;
    }

    @Override
    public void setSwimlaneDisplayMode(SwimlaneDisplayMode swimlaneDisplayMode) {
        throw new UnsupportedOperationException("This property is always SwimlaneDisplayMode.none");
    }

    @Override
    public Duration getDefaultTaskTimeoutDelay() {
        return getParent().getDefaultTaskTimeoutDelay();
    }

    @Override
    public void setDefaultTaskTimeoutDelay(Duration defaultTaskTimeoutDelay) {
        throw new UnsupportedOperationException("This property is inherited from main process definition");
    }

    @Override
    protected List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
        list.add(new PropertyDescriptor(PROPERTY_LANGUAGE, Localization.getString("ProcessDefinition.property.language")));
        list.add(new PropertyDescriptor(PROPERTY_DEFAULT_TASK_DURATION, Localization.getString("default.task.duedate")));
        list.add(new PropertyDescriptor(PROPERTY_ACCESS_TYPE, Localization.getString("ProcessDefinition.property.accessType")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_ACCESS_TYPE.equals(id)) {
            return Localization.getString("ProcessDefinition.property.accessType.EmbeddedSubprocess");
        }
        return super.getPropertyValue(id);
    }
    @Override
    public boolean isShowActions() {
        return getParent().isShowActions();
    }

    @Override
    public void setShowActions(boolean showActions) {
        throw new UnsupportedOperationException("This property is inherited from main process definition");
    }

    @Override
    public boolean isShowGrid() {
        return getParent().isShowGrid();
    }

    @Override
    public void setShowGrid(boolean showGrid) {
        throw new UnsupportedOperationException("This property is inherited from main process definition");
    }

    @Override
    public Language getLanguage() {
        return getParent().getLanguage();
    }

    @Override
    public void setLanguage(Language language) {
        throw new UnsupportedOperationException("This property is inherited from main process definition");
    }

    @Override
    public SwimlaneGUIConfiguration getSwimlaneGUIConfiguration() {
        return getParent().getSwimlaneGUIConfiguration();
    }

    @Override
    public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters) {
        return getParent().getVariableNames(includeSwimlanes, typeClassNameFilters);
    }

    @Override
    public List<Variable> getVariables(boolean includeSwimlanes, String... typeClassNameFilters) {
        return getParent().getVariables(includeSwimlanes, typeClassNameFilters);
    }

    @Override
    public Variable getVariable(String name, boolean searchInSwimlanes) {
        return getParent().getVariable(name, searchInSwimlanes);
    }

    @Override
    public void addVariable(Variable variable) {
        getParent().addVariable(variable);
    }

    @Override
    public void removeVariable(Variable variable) {
        getParent().removeVariable(variable);
    }

    @Override
    public String getNextVariableName() {
        return getParent().getNextVariableName();
    }

    @Override
    public List<String> getSwimlaneNames() {
        return getParent().getSwimlaneNames();
    }

    @Override
    public List<Swimlane> getSwimlanes() {
        return getParent().getSwimlanes();
    }

    @Override
    public Swimlane getSwimlaneByName(String name) {
        return getParent().getSwimlaneByName(name);
    }

    @Override
    public String getNextSwimlaneName() {
        return getParent().getNextSwimlaneName();
    }

    @Override
    public void addSwimlane(Swimlane swimlane) {
        getParent().addSwimlane(swimlane);
    }

    @Override
    public void removeSwimlane(Swimlane swimlane) {
        getParent().removeSwimlane(swimlane);
    }

    @Override
    public TimerAction getTimeOutAction() {
        return getParent().getTimeOutAction();
    }

    @Override
    public Duration getTimeOutDelay() {
        return getParent().getTimeOutDelay();
    }

    @Override
    public void setTimeOutAction(TimerAction timeOutAction) {
    }

    @Override
    public void setTimeOutDelay(Duration duration) {
    }

    @Override
    public NodeTypeDefinition getTypeDefinition() {
        return NodeRegistry.getNodeTypeDefinition(ProcessDefinition.class);
    }

    @Override
    public ProcessDefinition getParent() {
        return (ProcessDefinition) super.getParent();
    }

}
