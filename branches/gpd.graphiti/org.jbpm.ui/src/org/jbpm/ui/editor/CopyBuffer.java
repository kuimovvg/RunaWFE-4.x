package org.jbpm.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.part.graph.NodeGraphicalEditPart;
import org.jbpm.ui.resource.Messages;

public class CopyBuffer {
	public static final String GROUP_ACTION_HANDLERS = Messages.getString("CopyBuffer.ActionHandler");
	public static final String GROUP_SWIMLANES = Messages.getString("CopyBuffer.Swimlane");
	public static final String GROUP_FORM_FILES = Messages.getString("CopyBuffer.FormFiles");
    public static final String GROUP_VARIABLES = Messages.getString("CopyBuffer.Variable");
	
    private IFolder sourceFolder;
    private ProcessDefinition sourceDefinition;
    private IStructuredSelection sourceSelection;
    
	public CopyBuffer() {
        Object contents = Clipboard.getDefault().getContents();
        if (contents != null && contents.getClass().isArray()) {
            Object[] array = (Object[]) contents;
            if (array.length == 3) {
    	        sourceFolder = (IFolder) array[0];
    	        sourceDefinition = (ProcessDefinition) array[1];
    	        sourceSelection = (IStructuredSelection) array[2];
            }
        }
	}

    public List<Node> extractSourceNodes() {
    	List<Node> result = new ArrayList<Node>();
        for (Object object : sourceSelection.toList()) {
            if (!(object instanceof NodeGraphicalEditPart)) {
                continue;
            }
            Node node = ((NodeGraphicalEditPart) object).getModel();
            result.add(node);
        }
    	return result;
    }
	
	public boolean isValid() {
		return sourceFolder != null;
	}
	
	public IFolder getSourceFolder() {
		return sourceFolder;
	}

	public ProcessDefinition getSourceDefinition() {
		return sourceDefinition;
	}

	public IStructuredSelection getSourceSelection() {
		return sourceSelection;
	}
	
	public static abstract class ExtraCopyAction {
        private final String groupName;
    	private final String name;
        private boolean enabled = true;
        
		public ExtraCopyAction(String groupName, String name) {
		    this.groupName = groupName;
			this.name = name;
		}

		public String getName() {
            return name;
        }

        public String getDisplayName() {
            return groupName + ": " + name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isUserConfirmationRequired() {
            return false;
        }

		public abstract void execute() throws Exception;

		public abstract void undo() throws Exception;

		/*
		@Override
		public int hashCode() {
		    int hash = groupName.hashCode();
		    hash += 37*name.hashCode();
		    return hash;
		}
		
		@Override
		public boolean equals(Object obj) {
		    return hashCode() == obj.hashCode();
		}
		*/
		
		@Override
		public String toString() {
		    return getDisplayName();
		}
	}
}
