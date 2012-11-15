package ru.runa.gpd.editor;

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.GEFProcessEditor;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class EditorPartBase extends EditorPart implements PropertyChangeListener {

    protected final GEFProcessEditor editor;

    protected FormToolkit toolkit;

    public EditorPartBase(final GEFProcessEditor editor) {
        this.editor = editor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        editor.getDefinition().addPropertyChangeListener(this);
    }

    @Override
    public void dispose() {
        editor.getDefinition().removePropertyChangeListener(this);
        super.dispose();
    }

    Menu menu;

    protected void createContextMenu(Control control) {
        menu = new Menu(control.getShell(), SWT.POP_UP);
        control.setMenu(menu);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    protected ProcessDefinition getDefinition() {
        return editor.getDefinition();
    }

    protected Button addButton(Composite parent, String buttonKey, SelectionAdapter selectionListener, boolean addToMenu) {
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        Button button = toolkit.createButton(parent, Localization.getString(buttonKey), SWT.PUSH);
        button.setLayoutData(gridData);
        button.addSelectionListener(selectionListener);

        if (addToMenu) {
            MenuItem item = new MenuItem(menu, SWT.NONE);
            item.setText(Localization.getString(buttonKey));
            item.addSelectionListener(selectionListener);
            button.setData("menuItem", item);
        }
        return button;
    }

    protected void enableAction(Button button, boolean enabled) {
        button.setEnabled(enabled);
        ((MenuItem) button.getData("menuItem")).setEnabled(enabled);
    }

    protected SashForm createToolkit(Composite parent, String titleKey) {
        toolkit = new FormToolkit(parent.getDisplay());
        Form form = toolkit.createForm(parent);
        form.setText(Localization.getString(titleKey));

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        form.getBody().setLayout(layout);

        SashForm sashForm = new SashForm(form.getBody(), SWT.NULL);
        toolkit.adapt(sashForm, false, false);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        return sashForm;
    }

    protected Composite createSection(SashForm sashForm, String sectionTitleKey) {
        Section section = toolkit.createSection(sashForm, ExpandableComposite.TITLE_BAR);
        section.marginHeight = 5;
        section.marginWidth = 5;
        section.setText(Localization.getString(sectionTitleKey));

        Composite clientArea = toolkit.createComposite(section);
        section.setClient(clientArea);
        toolkit.paintBordersFor(clientArea);

        GridLayout layoutRight = new GridLayout();
        layoutRight.marginWidth = 2;
        layoutRight.marginHeight = 2;
        layoutRight.numColumns = 2;
        clientArea.setLayout(layoutRight);

        return clientArea;
    }

}
