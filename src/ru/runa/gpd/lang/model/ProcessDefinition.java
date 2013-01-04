package ru.runa.gpd.lang.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.orgfunction.SwimlaneGUIConfiguration;
import ru.runa.gpd.property.DefaultTaskDueDatePropertyDescriptor;
import ru.runa.gpd.property.StartImagePropertyDescriptor;
import ru.runa.gpd.ui.view.ValidationErrorsView;
import ru.runa.gpd.util.Delay;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.wfe.var.format.ExecutorFormat;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class ProcessDefinition extends NamedGraphElement implements Active, Describable, ITimeOut {
    private Language language;
    private Dimension dimension;
    private SwimlaneGUIConfiguration swimlaneGUIConfiguration;
    private boolean dirty;
    private boolean showActions;
    private boolean showGrid;
    private Delay defaultTaskTimeoutDelay = new Delay();
    private Delay timeOutDelay = new Delay();
    private TimerAction timeOutAction = null;
    private boolean invalid;
    private int nextNodeId;
    private SwimlaneDisplayMode swimlaneDisplayMode;

    public ProcessDefinition() {
    }

    public SwimlaneDisplayMode getSwimlaneDisplayMode() {
        return swimlaneDisplayMode;
    }

    public void setSwimlaneDisplayMode(SwimlaneDisplayMode swimlaneDisplayMode) {
        this.swimlaneDisplayMode = swimlaneDisplayMode;
    }

    public Delay getDefaultTaskTimeoutDelay() {
        return defaultTaskTimeoutDelay;
    }

    public void setDefaultTaskTimeoutDelay(Delay defaultTaskTimeoutDelay) {
        this.defaultTaskTimeoutDelay = defaultTaskTimeoutDelay;
        firePropertyChange(PROPERTY_TIMEOUT_DELAY, null, defaultTaskTimeoutDelay);
    }

    public boolean isShowActions() {
        return showActions;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setShowActions(boolean showActions) {
        boolean stateChanged = this.showActions != showActions;
        this.showActions = showActions;
        if (stateChanged) {
            firePropertyChange(PROPERTY_SHOW_ACTIONS, !this.showActions, this.showActions);
            setDirty();
        }
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        boolean stateChanged = this.showGrid != showGrid;
        if (stateChanged) {
            this.showGrid = showGrid;
            firePropertyChange(PROPERTY_SHOW_GRID, !this.showGrid, this.showGrid);
            setDirty();
        }
    }

    public boolean isBPMNNotation() {
        return Language.BPMN == language;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        boolean stateChanged = this.dirty != dirty;
        if (stateChanged) {
            this.dirty = dirty;
            firePropertyChange(PROPERTY_DIRTY, !this.dirty, this.dirty);
        }
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public Dimension getDimension() {
        if (dimension == null) {
            dimension = new Dimension(0, 0);
        }
        return dimension;
    }

    public void setNextNodeIdIfApplicable(int nextNodeId) {
        if (nextNodeId > this.nextNodeId) {
            this.nextNodeId = nextNodeId;
        }
    }

    public int getNextNodeId() {
        return nextNodeId++;
    }

    public SwimlaneGUIConfiguration getSwimlaneGUIConfiguration() {
        return swimlaneGUIConfiguration;
    }

    public void setSwimlaneGUIConfiguration(SwimlaneGUIConfiguration swimlaneGUIConfiguration) {
        this.swimlaneGUIConfiguration = swimlaneGUIConfiguration;
    }

    @Override
    public void removeAllPropertyChangeListeners() {
        super.removeAllPropertyChangeListeners();
        for (GraphElement element : getChildren(GraphElement.class)) {
            element.removeAllPropertyChangeListeners();
        }
    }

    @Override
    public void setName(String name) {
        if (name.length() == 0) {
            name = "Process";
        }
        super.setName(name);
    }

    @Override
    protected boolean canNameBeSetFromProperties() {
        return false;
    }

    private boolean[] hasValidationErrors;
    private IFile definitionFile;

    /**
     * Used only for validation !!! Either == null
     */
    protected IFile getDefinitionFile() {
        return definitionFile;
    }

    @Override
    protected void validate() {
        List<StartState> startStates = getChildren(StartState.class);
        if (startStates.size() == 0) {
            addError("startState.doesNotExist");
        }
        if (startStates.size() > 1) {
            addError("multipleStartStatesNotAllowed");
        }
    }

    /**
     * 0 = no errors 1 = only warnings 2 = errors
     */
    public int validateDefinition(IFile definitionFile) {
        try {
            this.definitionFile = definitionFile;
            hasValidationErrors = new boolean[] { false, false };
            definitionFile.deleteMarkers(ValidationErrorsView.ID, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
        List<GraphElement> childs = getChildrenRecursive(GraphElement.class);
        validate();
        for (GraphElement element : childs) {
            element.validate();
        }
        this.definitionFile = null;
        if (hasValidationErrors[1]) {
            this.invalid = true;
            return 2;
        }
        this.invalid = false;
        if (hasValidationErrors[0]) {
            return 1;
        }
        return 0;
    }

    protected void addError(GraphElement element, String messageKey, Object... params) {
        hasValidationErrors[1] = true;
        addError(element, messageKey, IMarker.SEVERITY_ERROR, params);
    }

    protected void addWarning(GraphElement element, String messageKey, Object... params) {
        hasValidationErrors[0] = true;
        addError(element, messageKey, IMarker.SEVERITY_WARNING, params);
    }

    private void addError(GraphElement element, String messageKey, int severity, Object... params) {
        try {
            IMarker marker = definitionFile.createMarker(ValidationErrorsView.ID);
            if (marker.exists()) {
                String msg = Localization.getString("model.validation." + messageKey);
                String formatted = MessageFormat.format(msg, params);
                marker.setAttribute(IMarker.MESSAGE, formatted);
                String elementId = element.toString();
                if (element instanceof Node) {
                    elementId = ((Node) element).getId();
                }
                if (element instanceof Swimlane) {
                    marker.setAttribute(PluginConstants.SWIMLANE_LINK_KEY, elementId);
                } else if (element instanceof Action) {
                    NamedGraphElement actionParent = (NamedGraphElement) element.getParent();
                    marker.setAttribute(PluginConstants.ACTION_INDEX_KEY, actionParent.getActions().indexOf(element));
                    String parentNodeTreePath;
                    if (actionParent instanceof Transition) {
                        parentNodeTreePath = ((NamedGraphElement) actionParent.getParent()).getName() + "|" + actionParent.getName();
                    } else {
                        parentNodeTreePath = actionParent.getName();
                    }
                    marker.setAttribute(PluginConstants.PARENT_NODE_KEY, parentNodeTreePath);
                    elementId = element.toString() + " (" + parentNodeTreePath + ")";
                } else {
                    marker.setAttribute(PluginConstants.SELECTION_LINK_KEY, elementId);
                }
                marker.setAttribute(IMarker.LOCATION, element.toString());
                marker.setAttribute(IMarker.SEVERITY, severity);
                marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, getName());
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    public List<String> getVariableNames(boolean includeSwimlanes) {
        List<String> names = new ArrayList<String>();
        for (Variable variable : getVariablesList()) {
            names.add(variable.getName());
        }
        if (includeSwimlanes) {
            names.addAll(getSwimlaneNames());
        }
        return names;
    }

    public Map<String, String> getVariableFormats(boolean includeSwimlanes) {
        Map<String, String> map = new HashMap<String, String>();
        for (Variable variable : getVariablesList()) {
            map.put(variable.getName(), variable.getFormat());
        }
        if (includeSwimlanes) {
            for (String swimlaneName : getSwimlaneNames()) {
                map.put(swimlaneName, ExecutorFormat.class.getName());
            }
        }
        return map;
    }

    public Map<String, Variable> getVariablesMap() {
        Map<String, Variable> map = new HashMap<String, Variable>();
        for (Variable variable : getVariablesList()) {
            map.put(variable.getName(), variable);
        }
        return map;
    }

    public List<Variable> getVariablesList() {
        return getChildren(Variable.class);
    }

    public void addVariable(Variable variable) {
        addChild(variable);
    }

    public void removeVariable(Variable variable) {
        removeChild(variable);
    }

    public String getNextVariableName() {
        List<String> variableNameSet = getVariableNames(true);
        int runner = 1;
        while (true) {
            String candidate = Localization.getString("default.variable.name") + runner;
            if (!variableNameSet.contains(candidate)) {
                return candidate;
            }
            runner++;
        }
    }

    public List<String> getSwimlaneNames() {
        List<String> names = new ArrayList<String>();
        for (Swimlane swimlane : getSwimlanes()) {
            names.add(swimlane.getName());
        }
        return names;
    }

    public List<Swimlane> getSwimlanes() {
        return getChildren(Swimlane.class);
    }

    public Swimlane getSwimlaneByName(String name) {
        if (name == null) {
            return null;
        }
        List<Swimlane> swimlanes = getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            if (name.equals(swimlane.getName())) {
                return swimlane;
            }
        }
        return null;
    }

    public String getNextSwimlaneName() {
        int runner = 1;
        while (true) {
            String candidate = Localization.getString("default.swimlane.name") + runner;
            if (getSwimlaneByName(candidate) == null) {
                return candidate;
            }
            runner++;
        }
    }

    public void addSwimlane(Swimlane swimlane) {
        addChild(swimlane);
        firePropertyChange(ELEMENT_SWIMLANE_ADDED, null, swimlane);
    }

    public void removeSwimlane(Swimlane swimlane) {
        removeChild(swimlane);
        firePropertyChange(ELEMENT_SWIMLANE_REMOVED, null, swimlane);
    }

    public <T extends GraphElement> T getGraphElementById(String nodeId) {
        for (GraphElement graphElement : getElementsRecursive()) {
            if (Objects.equal(nodeId, graphElement.getId())) {
                return (T) graphElement;
            }
        }
        return null;
    }

    public <T extends GraphElement> T getGraphElementByIdNotNull(String nodeId) {
        T node = ((T) getGraphElementById(nodeId));
        if (node == null) {
            // back compatibility: search by name
            //            for (Node testNode : getNodes()) {
            //                if (Objects.equal(nodeId, testNode.getName())) {
            //                    return (T) node;
            //                }
            //            }
            List<String> nodeIds = new ArrayList<String>();
            for (Node childNode : getChildren(Node.class)) {
                nodeIds.add(childNode.getId());
            }
            throw new RuntimeException("Node not found in process definition: " + nodeId + ", all nodes: " + nodeIds);
        }
        return node;
    }

    public List<Node> getNodesRecursive() {
        return getChildrenRecursive(Node.class);
    }

    public List<GraphElement> getElementsRecursive() {
        return getChildrenRecursive(GraphElement.class);
    }

    public List<GraphElement> getContainerElements(GraphElement parentContainer) {
        List<GraphElement> list = Lists.newArrayList();
        for (GraphElement graphElement : getElementsRecursive()) {
            if (Objects.equal(parentContainer, graphElement.getParentContainer())) {
                list.add(graphElement);
            }
            if (parentContainer == this && graphElement.getParentContainer() == null) {
                list.add(graphElement);
            }
        }
        return list;
    }

    @Override
    protected List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
        list.add(new StartImagePropertyDescriptor("startProcessImage", Localization.getString("ProcessDefinition.property.startImage")));
        list.add(new PropertyDescriptor(PROPERTY_LANGUAGE, Localization.getString("ProcessDefinition.property.language")));
        list.add(new DefaultTaskDueDatePropertyDescriptor(PROPERTY_DEFAULT_TASK_DURATION, this));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_LANGUAGE.equals(id)) {
            return language;
        } else if (PROPERTY_DEFAULT_TASK_DURATION.equals(id)) {
            if (defaultTaskTimeoutDelay.hasDuration()) {
                return defaultTaskTimeoutDelay;
            }
            return "";
        }
        if (PROPERTY_TIMEOUT_DELAY.equals(id)) {
            return timeOutDelay;
        }
        if (PROPERTY_TIMEOUT_ACTION.equals(id)) {
            return timeOutAction;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_DEFAULT_TASK_DURATION.equals(id)) {
            setDefaultTaskTimeoutDelay((Delay) value);
            firePropertyChange(PROPERTY_DEFAULT_TASK_DURATION, null, null);
        } else if (PROPERTY_TIMEOUT_DELAY.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            setTimeOutDelay((Delay) value);
        } else if (PROPERTY_TIMEOUT_ACTION.equals(id)) {
            setTimeOutAction((TimerAction) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/process.gif");
    }

    @Override
    public TimerAction getTimeOutAction() {
        return timeOutAction;
    }

    @Override
    public Delay getTimeOutDelay() {
        return timeOutDelay;
    }

    public void setTimeOutAction(TimerAction timeOutAction) {
        if (timeOutAction == TimerAction.NONE) {
            timeOutAction = null;
        }
        this.timeOutAction = timeOutAction;
    }

    @Override
    public void setTimeOutDelay(Delay delay) {
        this.timeOutDelay = delay;
    }
}
