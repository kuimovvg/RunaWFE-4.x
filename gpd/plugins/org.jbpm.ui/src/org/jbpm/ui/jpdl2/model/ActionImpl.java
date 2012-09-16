package ru.runa.bpm.ui.jpdl2.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.State;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.resource.Messages;

public class ActionImpl extends Action {
    private static final String EVENTTYPE_PROCESS_START = "process-start";
    private static final String EVENTTYPE_PROCESS_END = "process-end";
    private static final String EVENTTYPE_STATE_ENTER = "state-enter";
    private static final String EVENTTYPE_STATE_LEAVE = "state-leave";
    private static final String EVENTTYPE_STATE_AFTER_ASSIGNMENT = "state-after-assignment";
    private static final String EVENTTYPE_TRANSITION = "transition";

    private String eventType;

    @Override
    public void setParent(GraphElement parent) {
        super.setParent(parent);
        if (parent instanceof Transition) {
            eventType = EVENTTYPE_TRANSITION;
        } else if (parent instanceof ProcessDefinition) {
            eventType = EVENTTYPE_PROCESS_START;
        } else if (parent instanceof State) {
            eventType = EVENTTYPE_STATE_AFTER_ASSIGNMENT;
        }
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        if (eventType != null && canSetEventType(eventType)) {
            String old = this.getEventType();
            this.eventType = eventType;
            firePropertyChange(PROPERTY_EVENT_TYPE, old, this.getEventType());
        }
    }

    private boolean canSetEventType(String eventType) {
        return getAllowedEventTypes().contains(eventType);
    }

    private static final String[] TRANSITION_ACTION_EVENT_TYPES = { EVENTTYPE_TRANSITION };

    private static final String[] STATE_ACTION_EVENT_TYPES = { EVENTTYPE_STATE_ENTER, EVENTTYPE_STATE_LEAVE, EVENTTYPE_STATE_AFTER_ASSIGNMENT };

    private static final String[] DEFINITION_ACTION_EVENT_TYPES = { EVENTTYPE_PROCESS_START, EVENTTYPE_PROCESS_END };

    private List<String> getAllowedEventTypes() {
        GraphElement parent = getParent();
        if (parent instanceof Transition) {
            return Arrays.asList(TRANSITION_ACTION_EVENT_TYPES);
        } else if (parent instanceof State) {
            return Arrays.asList(STATE_ACTION_EVENT_TYPES);
        } else if (parent instanceof ProcessDefinition) {
            return Arrays.asList(DEFINITION_ACTION_EVENT_TYPES);
        } else {
            throw new IllegalArgumentException("Unknown action element " + parent);
        }
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<String> allowedEventTypes = getAllowedEventTypes();
        String[] eventTypes = new String[allowedEventTypes.size()];
        int i = 0;
        for (String string : allowedEventTypes) {
            eventTypes[i++] = string;
        }
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new ComboBoxPropertyDescriptor(PROPERTY_EVENT_TYPE, Messages.getString("Action.property.eventType"), eventTypes));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_EVENT_TYPE.equals(id)) {
            String eventType = getEventType();
            if (eventType == null) {
                return new Integer(-1);
            }
            List<String> eventTypes = getAllowedEventTypes();
            return new Integer(eventTypes.indexOf(eventType));
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_EVENT_TYPE.equals(id)) {
            int index = ((Integer) value).intValue();
            setEventType(getAllowedEventTypes().get(index));
        } else {
            super.setPropertyValue(id, value);
        }
    }

}
