package ru.runa.gpd.ui.view;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Schema;
import org.apache.ddlutils.model.Table;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ru.runa.gpd.GPDProject;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.util.WorkspaceOperations;

public class DBExplorerTreeView extends ViewPart implements ISelectionListener,
		IPartListener2 {
	private TreeViewer viewer;
	private FormToolkit toolkit;
	private Form form;
	private IProject currentProject;
	private boolean visible;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
	}

	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getPartService()
				.removePartListener(this);
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		super.dispose();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);
		toolkit.decorateFormHeading(form);
		Action syncAction = new Action("",
				SharedImages.getImageDescriptor("icons/db_synchronize.png")) {
			@Override
			public void run() {
				WorkspaceOperations.deleteDBResources(currentProject);
				refreshView();
			}
		};
		syncAction.setToolTipText(Localization.getString("db_synchronize"));

		form.getToolBarManager().add(syncAction);
		form.getToolBarManager().update(true);
		form.setToolBarVerticalAlignment(SWT.BOTTOM);
		viewer = new TreeViewer(form.getBody(), SWT.FULL_SELECTION);
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
		viewer.addDragSupport(operations, transferTypes,
				new DBExplorerDragListener(viewer));
		GridData treeLayout = new GridData();
		treeLayout.verticalAlignment = GridData.FILL;
		treeLayout.grabExcessVerticalSpace = true;
		treeLayout.horizontalAlignment = GridData.FILL;
		treeLayout.grabExcessHorizontalSpace = true;

		// Set the layout
		viewer.getControl().setLayoutData(treeLayout);
		viewer.setContentProvider(new DBResourcesContentProvider(getSite()
				.getShell(), currentProject));
		viewer.setLabelProvider(new DBResourcesLabelProvider());
		viewer.setInput(new Object());

		getSite().setSelectionProvider(viewer);
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				DBExplorerTreeView.this.fillContextMenu(manager);
			}
		});
		viewer.getControl().setMenu(menu);
	}

	private void refreshView() {
		try {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!viewer.getControl().isDisposed()) {
						viewer.refresh();
					}
				}
			});
		} catch (Exception e) {
			// disposed
		}
	}

	protected void fillContextMenu(IMenuManager manager) {
	}

	@Override
	public void setFocus() {
	}

	private boolean isThisPart(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		return part != null && part.equals(this);
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		IEditorPart actEditor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (actEditor == null) {
			currentProject = null;
		} else {
			IFile formFile = (IFile) (((IFileEditorInput) (actEditor
					.getEditorInput())).getFile());
			currentProject = formFile.getProject();
		}
		if (visible) {
			refreshProviderIfNeeded();
		}
	}

	private void refreshProviderIfNeeded() {
		if (currentProject != null) {
			DBResourcesContentProvider provider = ((DBResourcesContentProvider) viewer
					.getContentProvider());
			IFolder dbResourcesFolder = currentProject
					.getFolder(GPDProject.DB_RESOURCES_FOLDER);
			boolean cacheNotFound = false;
			try {
				dbResourcesFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
				cacheNotFound = !dbResourcesFolder.exists()
						|| dbResourcesFolder.members().length == 0;
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			IProject project = provider.getProject();
			if (currentProject == null || !currentProject.equals(project)
					|| cacheNotFound) {
				provider.setProject(currentProject);
				refreshView();
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible = false;
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible = false;
		}
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible = true;
		}
	}

	private class DBExplorerDragListener implements DragSourceListener {

		private TreeViewer viewer;

		public DBExplorerDragListener(TreeViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void dragFinished(DragSourceEvent e) {
			IEditorPart actEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if(actEditor instanceof IDBExplorerDropEditor){
				IDBExplorerDropEditor dbDropEditor=((IDBExplorerDropEditor)actEditor);
				dbDropEditor.doDrop();
			}
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			// Here you do the convertion to the type which is expected.
			ITreeSelection selection = (ITreeSelection) viewer.getSelection();
			event.data = " ";
			Object tableObject = selection.getPaths()[0].getParentPath()
					.getLastSegment();
			Object schemaObject=selection.getPaths()[0].getParentPath().getParentPath()!=null?selection.getPaths()[0].getParentPath().getParentPath().getLastSegment():null;
			Object selectObject = selection.getFirstElement();
			if (selectObject instanceof Column && tableObject instanceof Table && schemaObject instanceof Schema) {
				String tag = TagUtils.createByColumn((Column) selectObject,
						(Table) tableObject,(Schema)schemaObject);
				if (tag != null) {
					event.data = tag;
				}
			}
		}

		@Override
		public void dragStart(DragSourceEvent e) {
			System.out.println("dragStart");

		}

	}

	private static class TagUtils {
		public static String createByColumn(Column column, Table table, Schema schema) {
			if ("BIGINT".equals(column.getType())||"NUMERIC".equals(column.getType())) {
				return "${NumberField(\"" + getColumnReference(schema, table, column) + "\")}";
			} else if ("VARCHAR".equals(column.getType())) {
				return "${TextField(\"" + getColumnReference(schema, table, column) + "\")}";
			}else if("TIMESTAMP".equals(column.getType())){
				return "${DateTimePicker(\"" + getColumnReference(schema, table, column) + "\",\"\",\"\",\"date_time\")}";
			}else if("DATE".equals(column.getType())){
				return "${DateTimePicker(\"" + getColumnReference(schema, table, column) + "\",\"\",\"\",\"date\")}";
			}else if("BIT".equals(column.getType())){
				return "${CheckBox(\""+ getColumnReference(schema, table, column) +"\")}";
			}
			System.out.println(column.getType());
			return null;
		}
		
		private static String getColumnReference(Schema schema ,Table table,Column column){
			return schema.getName() + "."
					+ table.getName() + "." + column.getName();
		}
	}
}
