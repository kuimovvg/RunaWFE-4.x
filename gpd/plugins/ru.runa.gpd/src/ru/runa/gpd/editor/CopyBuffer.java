package ru.runa.gpd.editor;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.ui.actions.Clipboard;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class CopyBuffer {
	public static final String GROUP_ACTION_HANDLERS = Localization.getString("CopyBuffer.ActionHandler");
	public static final String GROUP_SWIMLANES = Localization.getString("CopyBuffer.Swimlane");
	public static final String GROUP_FORM_FILES = Localization.getString("CopyBuffer.FormFiles");
    public static final String GROUP_VARIABLES = Localization.getString("CopyBuffer.Variable");
	
    private IFolder sourceFolder;
    private ProcessDefinition sourceDefinition;
    private List<Node> sourceNodes;
    
	public CopyBuffer() {
        Object contents = Clipboard.getDefault().getContents();
        if (contents != null && contents.getClass().isArray()) {
            Object[] array = (Object[]) contents;
            if (array.length == 3) {
    	        sourceFolder = (IFolder) array[0];
    	        sourceDefinition = (ProcessDefinition) array[1];
    	        sourceNodes = (List<Node>) array[2];
            }
        }
	}

    public CopyBuffer(IFolder sourceFolder, ProcessDefinition sourceDefinition, List<Node> sourceNodes) {
        this.sourceFolder = sourceFolder;
        this.sourceDefinition = sourceDefinition;
        this.sourceNodes = sourceNodes;
    }

    public void setToClipboard() {
        Clipboard.getDefault().setContents(new Object[] { sourceFolder, sourceDefinition, sourceNodes });
    }

    public List<Node> getSourceNodes() {
        return sourceNodes;
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

        public String getLabel() {
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

		@Override
		public String toString() {
		    return getLabel();
		}
	}
}
