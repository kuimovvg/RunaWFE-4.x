package ru.runa.gpd.formeditor.ftl.view;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class ToolPaletteDragListener implements DragSourceListener {

	private final TableViewer viewer;
	
	private ToolPalleteMethodTag element;

	public ToolPaletteDragListener(TableViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		IEditorPart actEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(actEditor instanceof IToolPalleteDropEditor){
			IToolPalleteDropEditor toolPalleteDropEditor=((IToolPalleteDropEditor)actEditor);
			if(element!=null){
				toolPalleteDropEditor.doDrop(element.getTagName());
				element=null;
			}
		}
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		// Here you do the convertion to the type which is expected.
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		element = (ToolPalleteMethodTag) selection.getFirstElement();

		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = element.getTagName();
		}

	}

	@Override
	public void dragStart(DragSourceEvent event) {
		System.out.println("Start Drag");
	}

}