package ru.runa.gpd.formeditor.ftl.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ru.runa.gpd.help.IHelpToticProvider;

public class ToolPaletteView extends ViewPart implements IPartListener2 {
    private TableViewer viewer;
    private HelpToticProvider helpTopicProvider;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

        helpTopicProvider = new HelpToticProvider();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IHelpToticProvider.class) {
            return helpTopicProvider;
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getPartService().removePartListener(this);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        int operations = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
        viewer.addDragSupport(operations, transferTypes, new ToolPaletteDragListener(viewer));
        viewer.setContentProvider(new TableContentProvider());
        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(ContentProvider.INSTANCE.getModel());

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                helpTopicProvider.setSelection(event.getSelection());
            }
        });
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

    @Override
    public void partActivated(IWorkbenchPartReference arg0) {
        if (viewer != null) {
            viewer.setInput(ContentProvider.INSTANCE.getModel());
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void partClosed(IWorkbenchPartReference arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void partHidden(IWorkbenchPartReference arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void partOpened(IWorkbenchPartReference arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partVisible(IWorkbenchPartReference arg0) {
        // TODO Auto-generated method stub
    }

    private static class HelpToticProvider implements IHelpToticProvider {
        private ISelection selection;

        public void setSelection(ISelection selection) {
            this.selection = selection;
        }

        @Override
        public String getHelpTopicUrl(ISelection selection) {
            return null;
        }

        @Override
        public String getHelpTopicUrl() {
            if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
                return null;

            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            ToolPalleteMethodTag selectedComponent = (ToolPalleteMethodTag) structuredSelection.getFirstElement();

            return selectedComponent.getHelpUrl();
        }
    }

}
