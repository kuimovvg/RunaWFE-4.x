package ru.runa.gpd.ui.dialog.projectstructure;

import java.text.MessageFormat;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.data.util.ProjectStructureUtils;

public class ProjectStructureDirectory extends Dialog {

	private Tree categoryTree;
	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;

	private String projectName;
	private String[] selectedItemFullPath;

	public ProjectStructureDirectory(String projectName) {
		super(Display.getCurrent().getActiveShell());

		this.projectName = projectName;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(300, 400);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(1, true);
		area.setLayout(layout);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite directoryButtonsPanel = new Composite(area, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 3;
		directoryButtonsPanel.setLayout(layout);
		directoryButtonsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false));

		addBtn = new Button(directoryButtonsPanel, SWT.NONE);
		addBtn.setToolTipText(Localization.getString("dialog.add"));
		Image addBtnImage = SharedImages.getImageDescriptor(
				"icons/add_project.gif").createImage();
		addBtn.setImage(addBtnImage);
		addBtn.setEnabled(false);
		addBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				onAddButtonClick();
			}
		});

		editBtn = new Button(directoryButtonsPanel, SWT.NONE);
		editBtn.setToolTipText(Localization.getString("dialog.edit"));
		Image editBtnImage = SharedImages
				.getImageDescriptor("icons/rename.gif").createImage();
		editBtn.setImage(editBtnImage);
		editBtn.setEnabled(false);
		editBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				onEditBtnClick();
			}
		});

		deleteBtn = new Button(directoryButtonsPanel, SWT.NONE);
		deleteBtn.setToolTipText(Localization.getString("dialog.delete"));
		Image deleteBtnImage = SharedImages.getImageDescriptor(
				"icons/delete.gif").createImage();
		deleteBtn.setImage(deleteBtnImage);
		deleteBtn.setEnabled(false);
		deleteBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				onDeleteBtnClick();
			}
		});

		categoryTree = new Tree(area, SWT.NONE);
		categoryTree
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		categoryTree.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				onItemSelected();
			}

		});
		reloadTree();

		return area;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				Localization.getString("dialog.select"), true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				Localization.getString("dialog.cancel"), false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Localization.getString("dialog.project_structure"));
	}

	private void onAddButtonClick() {
		EditCategoryDialog dialog = new EditCategoryDialog(
				getSelectedCategoryFullPath(), true);
		dialog.open();

		String[] parentCategoryFullPath = dialog.getSelectedItem();

		switch (dialog.getReturnCode()) {
		case Window.OK:
			ProjectStructureUtils.addCategory(parentCategoryFullPath, dialog.getCategoryName());
			
			reloadTree();

			String[] newCategoryFullPath = new String[parentCategoryFullPath.length + 1];
			System.arraycopy(parentCategoryFullPath, 0, newCategoryFullPath, 0,
					parentCategoryFullPath.length);
			newCategoryFullPath[newCategoryFullPath.length - 1] = dialog
					.getCategoryName();

			selectCategoryByFullPath(newCategoryFullPath);
			break;
		case Window.CANCEL:
			// Do nothing
			break;
		}		
	}

	private void onEditBtnClick() {
		EditCategoryDialog dialog = new EditCategoryDialog(
				getSelectedCategoryFullPath(), false);
		dialog.open();

		String[] selectedCategoryFullPath = dialog.getSelectedItem();

		switch (dialog.getReturnCode()) {
		case Window.OK:
			ProjectStructureUtils.renameCategory(selectedCategoryFullPath, dialog.getCategoryName());
			
			reloadTree();

			selectedCategoryFullPath[selectedCategoryFullPath.length - 1] = dialog.getCategoryName();

			selectCategoryByFullPath(selectedCategoryFullPath);
			break;
		case Window.CANCEL:
			// Do nothing
			break;
		}		
	}

	private void onDeleteBtnClick() {
		String[] selectedCategoryFullPath = getSelectedCategoryFullPath();
		String selectedCategoryName = selectedCategoryFullPath[selectedCategoryFullPath.length - 1];

		String message = Localization.getString("dialog.delete_category");
		if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
				Localization.getString("message.confirm.operation"),
				MessageFormat.format(message, selectedCategoryName))) {
			ProjectStructureUtils.deleteCategory(selectedCategoryFullPath);
		}

		reloadTree();

		String[] parentElemetFullPath = new String[selectedCategoryFullPath.length - 1];
		System.arraycopy(selectedCategoryFullPath, 0, parentElemetFullPath, 0,
				parentElemetFullPath.length);
		selectCategoryByFullPath(parentElemetFullPath);
		categoryTree.getSelection()[0].setExpanded(true);
	}

	private void reloadTree() {
		categoryTree.removeAll();

		ProjectStructureUtils.fillTree(categoryTree, projectName);
	}

	private void onItemSelected() {
		if (categoryTree.getSelectionCount() == 0) {
			selectedItemFullPath = null;
		}

		LinkedList<String> selectedCategoryFullPath = new LinkedList<String>();

		TreeItem treeItem = categoryTree.getSelection()[0];

		while (treeItem != null) {
			selectedCategoryFullPath.addFirst(treeItem.getText());

			treeItem = treeItem.getParentItem();
		}

		this.selectedItemFullPath = selectedCategoryFullPath
				.toArray(new String[0]);

		boolean notRootElementSelected = selectedItemFullPath.length > 1;
		addBtn.setEnabled(true);
		editBtn.setEnabled(notRootElementSelected);
		deleteBtn.setEnabled(notRootElementSelected);
	}

	public String[] getSelectedCategoryFullPath() {
		return selectedItemFullPath;
	}

	public void selectCategoryByFullPath(String[] itemFullPath) {
		if (itemFullPath == null || itemFullPath.length == 0) {
			return;
		}

		TreeItem rootItem = findRoot(itemFullPath[0]);

		if (itemFullPath.length == 1) {
			categoryTree.select(rootItem);
		} else {
			categoryTree.select(findChildItem(rootItem, itemFullPath, 1));
		}

		onItemSelected();
	}

	private TreeItem findRoot(String rootName) {
		if (rootName != null) {
			for (TreeItem treeItem : categoryTree.getItems()) {
				if (rootName.equals(treeItem.getText())
						&& treeItem.getParentItem() == null) {
					return treeItem;
				}
			}
		}

		return null;
	}

	private TreeItem findChildItem(TreeItem root, String[] fullPath,
			int startFrom) {
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
