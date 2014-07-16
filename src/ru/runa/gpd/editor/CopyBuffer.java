package ru.runa.gpd.editor;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.ui.actions.Clipboard;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.SelectionItem;

import com.google.common.base.Objects;

public class CopyBuffer {
	public static final String GROUP_ACTION_HANDLERS = Localization.getString("CopyBuffer.ActionHandler");
	public static final String GROUP_SWIMLANES = Localization.getString("CopyBuffer.Swimlane");
	public static final String GROUP_FORM_FILES = Localization.getString("CopyBuffer.FormFiles");
    public static final String GROUP_VARIABLES = Localization.getString("CopyBuffer.Variable");
	
    private IFolder sourceFolder;
    private Language sourceLanguage;
    private List<NamedGraphElement> sourceNodes;
    
	public CopyBuffer() {
        Object contents = Clipboard.getDefault().getContents();
        if (contents != null && contents.getClass().isArray()) {
            Object[] array = (Object[]) contents;
            if (array.length == 3) {
    	        sourceFolder = (IFolder) array[0];
    	        sourceLanguage = (Language) array[1];
    	        sourceNodes = (List<NamedGraphElement>) array[2];
            }
        }
	}

    public CopyBuffer(IFolder sourceFolder, Language sourceLanguage, List<NamedGraphElement> sourceNodes) {
        this.sourceFolder = sourceFolder;
        this.sourceLanguage = sourceLanguage;
        this.sourceNodes = sourceNodes;
    }

    public void setToClipboard() {
        Clipboard.getDefault().setContents(new Object[] { sourceFolder, sourceLanguage, sourceNodes });
    }

    public List<NamedGraphElement> getSourceNodes() {
        return sourceNodes;
    }
    
	public boolean isValid() {
		return sourceFolder != null;
	}
	
	public IFolder getSourceFolder() {
		return sourceFolder;
	}

	public Language getLanguage() {
		return sourceLanguage;
	}

	public static abstract class ExtraCopyAction extends SelectionItem implements Comparable<ExtraCopyAction> {
        private final String groupName;
    	private final String name;
        private String changes;
        
		public ExtraCopyAction(String groupName, String name) {
		    super(true, null);
		    this.groupName = groupName;
			this.name = name;
		}

		public String getName() {
            return name;
        }

		@Override
        public String getLabel() {
            String label = groupName + ": " + name;
            if (changes != null) {
                label += " (" + changes + ")";
            }
            return label;
        }
        
        public final boolean isUserConfirmationRequired() {
            changes = getChanges();
            return changes != null;
        }
        
        protected String getChanges() {
            return null;
        }

		public abstract void execute() throws Exception;

		public abstract void undo() throws Exception;

//        @Override
//        public String toString() {
//            return getClass().getSimpleName() + " on " + sourceFormNode;
//        }

		@Override
		public String toString() {
		    return getLabel();
		}
		
		@Override
		public boolean equals(Object obj) {
		    ExtraCopyAction o = (ExtraCopyAction) obj;
		    return groupName.equals(o.groupName) && name.equals(o.name);
		}
		
		@Override
		public int hashCode() {
		    return Objects.hashCode(groupName, name);
		}
		
		@Override
		public int compareTo(ExtraCopyAction o) {
		    if (groupName.equals(o.groupName)) {
	            return name.compareTo(o.name);
		    }
		    return groupName.compareTo(o.groupName);
		}
		
	}
}
