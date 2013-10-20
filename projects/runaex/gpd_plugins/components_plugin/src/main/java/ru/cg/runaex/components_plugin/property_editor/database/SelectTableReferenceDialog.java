package ru.cg.runaex.components_plugin.property_editor.database;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components_plugin.component_parameter.descriptor.DescriptorLocalizationFactory;
import ru.runa.gpd.ui.view.DBResourcesLabelProvider;

public class SelectTableReferenceDialog extends Dialog {
    private Tree tree;
    private TableReference selectedTableReference;
    private Button selectBtn;

    private TreeViewer viewer;
    private IProject currentProject;
    private ru.cg.runaex.components_plugin.Localization localization;

    public SelectTableReferenceDialog(IProject currentProject) {
        super(Display.getCurrent().getActiveShell());
        localization = DescriptorLocalizationFactory.getSelectTableReferenceDialogLocalization();
        this.currentProject = currentProject;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(300, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout areaLayout = new GridLayout(1, false);
        area.setLayout(areaLayout);
        area.setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer = new TreeViewer(parent, SWT.SINGLE);
        GridData treeLayout = new GridData();
        treeLayout.verticalAlignment = SWT.FILL;
        treeLayout.grabExcessVerticalSpace = true;
        treeLayout.horizontalAlignment = SWT.FILL;
        treeLayout.grabExcessHorizontalSpace = true;

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                if (!event.getSelection().isEmpty() && ((TreeSelection) event.getSelection()).size() == 1
                        && !(((TreeSelection) event.getSelection()).getFirstElement().getClass().getName().equals("org.apache.ddlutils.model.Table"))) {
                    viewer.setSelection(StructuredSelection.EMPTY);
                    selectBtn.setEnabled(false);
                } else {
                    if (!event.getSelection().isEmpty() && ((TreeSelection) event.getSelection()).size() == 1) {
                        selectBtn.setEnabled(true);
                    }
                }
            }
        });

        // Set the layout
        viewer.getControl().setLayoutData(treeLayout);
        viewer.setContentProvider(new TableContentProvider(area.getShell(), currentProject));
        viewer.setLabelProvider(new DBResourcesLabelProvider());
        viewer.setInput(new Object());
        tree = viewer.getTree();
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        tree.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                onItemSelected();
            }

        });

        return area;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        selectBtn = createButton(parent, IDialogConstants.OK_ID, localization.get("dialog.select"), true);
        createButton(parent, IDialogConstants.CANCEL_ID, localization.get("dialog.cancel"), false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(localization.get("dialog.title"));
    }

    private void onItemSelected() {
        TreeItem[] selection = tree.getSelection();
        if (selection.length > 0) {
            TreeItem treeItem = selection[0];
            String table = treeItem.getText();
            treeItem = treeItem.getParentItem();
            String schema = treeItem.getText();
            selectedTableReference = new TableReference(schema, table, 2);
        } else {
            selectedTableReference = null;
        }
    }

    public TableReference getSelectedColumnReference() {
        return selectedTableReference;
    }

    public void selectByFullPath(String[] itemFullPath) {
        if (itemFullPath == null || itemFullPath.length == 0) {
            return;
        }

        TreeItem rootItem = findRoot(itemFullPath[0]);

        if (itemFullPath.length == 1) {
            tree.select(rootItem);
        } else {
            tree.select(findChildItem(rootItem, itemFullPath, 1));
        }
    }

    private TreeItem findRoot(String rootName) {
        if (rootName != null) {
            for (TreeItem treeItem : tree.getItems()) {
                if (rootName.equals(treeItem.getText()) && treeItem.getParentItem() == null) {
                    return treeItem;
                }
            }
        }

        return null;
    }

    private TreeItem findChildItem(TreeItem root, String[] fullPath, int startFrom) {
        for (TreeItem treeItem : root.getItems()) {
            if (fullPath[startFrom].equals(treeItem.getText())) {
                if (startFrom == fullPath.length - 1) {
                    return treeItem;
                } else {
                    return findChildItem(treeItem, fullPath, startFrom + 1);
                }
            }
        }
        return null;
    }
}
