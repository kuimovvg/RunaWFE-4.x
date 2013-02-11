package ru.runa.gpd.extension.orgfunction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class SwimlaneElement {
    private String name;
    private String description = "";
    private SwimlaneElement parent;
    private final List<SwimlaneElement> children = new ArrayList<SwimlaneElement>();
    protected List<ISwimlaneElementListener> listeners = new ArrayList<ISwimlaneElementListener>();
    protected Section section;
    protected Composite clientArea;
    protected String swimlaneName;
    protected OrgFunctionDefinition currentDefinition;
    protected ProcessDefinition processDefinition;
    private String orgFunctionDefinitionName;
    private final HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());
    private String treePath;

    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public void setOrgFunctionDefinitionName(String orgFunctionDefinitionName) {
        this.orgFunctionDefinitionName = orgFunctionDefinitionName;
    }

    public String getOrgFunctionDefinitionName() {
        return orgFunctionDefinitionName;
    }

    protected OrgFunctionDefinition createNew() {
        return new OrgFunctionDefinition(OrgFunctionsRegistry.getInstance().getArtifactNotNull(orgFunctionDefinitionName));
    }

    public void setDescription(String description) {
        this.description = Localization.getString(description);
    }

    public Composite getClientArea() {
        if (section != null) {
            return (Composite) section.getClient();
        }
        return clientArea;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        for (SwimlaneElement swimlaneElement : children) {
            swimlaneElement.setProcessDefinition(processDefinition);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return Localization.getString(name);
    }

    public final String getName() {
        return name;
    }

    public void open(String path, String swimlaneName, OrgFunctionDefinition currentDefinition) {
        if (!path.startsWith(getName())) {
            return;
        }
        this.swimlaneName = swimlaneName;
        if (path.length() > getName().length()) {
            String childPath = path.substring(path.indexOf("/") + 1);
            for (SwimlaneElement element : children) {
                if (childPath.startsWith(element.getName())) {
                    element.open(childPath, swimlaneName, currentDefinition);
                } else {
                    element.close();
                }
            }
        }
        if (section != null && !section.isExpanded()) {
            section.setExpanded(true);
        }
        if (currentDefinition != null && currentDefinition.getName().equals(orgFunctionDefinitionName)) {
            this.currentDefinition = currentDefinition;
        } else {
            this.currentDefinition = null;
        }
    }

    public void close() {
        for (SwimlaneElement element : children) {
            element.close();
        }
        if (section != null && section.isExpanded()) {
            section.setExpanded(false);
        }
    }

    public void setParent(SwimlaneElement parent) {
        this.parent = parent;
    }

    public void addChild(SwimlaneElement swimlaneElement) {
        children.add(swimlaneElement);
        swimlaneElement.setParent(this);
    }

    public void addElementListener(ISwimlaneElementListener listener) {
        listeners.add(listener);
        for (SwimlaneElement element : children) {
            element.addElementListener(listener);
        }
    }

    public void removeElementListener(ISwimlaneElementListener listener) {
        listeners.remove(listener);
        for (SwimlaneElement element : children) {
            element.removeElementListener(listener);
        }
    }

    public List<SwimlaneElement> getChildren() {
        return children;
    }

    public abstract void createGUI(Composite clientArea);

    protected Hyperlink createLink(Composite parent, String msg) {
        Hyperlink link = new Hyperlink(parent, SWT.NONE);
        link.setText(msg);
        hyperlinkGroup.add(link);
        return link;
    }

    protected Composite createSection(Composite parentComposite, int numColumns) {
        section = new Section(parentComposite, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE);
        section.marginHeight = 5;
        section.marginWidth = 5;
        section.setText(getLabel());
        section.setDescription(description);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.minimumHeight = 100;
        section.setLayoutData(gridData);
        Composite clientArea = new Composite(section, SWT.NONE);
        section.setClient(clientArea);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        clientArea.setLayoutData(gridData);
        GridLayout layout = new GridLayout(numColumns, false);
        layout.marginBottom = 2;
        clientArea.setLayout(layout);
        section.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                if (e.getState()) {
                    for (ISwimlaneElementListener listener : listeners) {
                        listener.opened(calculatePath());
                    }
                }
            }
        });
        return clientArea;
    }

    protected Composite createComposite(Composite parentComposite, int numColumns) {
        clientArea = new Composite(parentComposite, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.minimumHeight = 100;
        clientArea.setLayoutData(gridData);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        clientArea.setLayoutData(gridData);
        GridLayout layout = new GridLayout(numColumns, false);
        layout.marginBottom = 2;
        clientArea.setLayout(layout);
        return clientArea;
    }

    private String calculatePath() {
        String path = parent != null ? parent.getName() + "/" : "";
        path += getName();
        return path;
    }

    protected void fireCompletedEvent(OrgFunctionDefinition definition) {
        for (ISwimlaneElementListener listener : listeners) {
            listener.completed(calculatePath(), definition);
        }
    }
}
