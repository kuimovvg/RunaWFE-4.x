package ru.runa.gpd.ui.dialog.projectstructure;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;

public class EditCategoryDialog extends Dialog {
	
	private Text categoryNameField;	
	private String[] selectedItem;	
	private boolean add;
	private String categoryName;

	public EditCategoryDialog(String[] selectedItem, boolean add) {
		super(Display.getCurrent().getActiveShell());
				
		this.selectedItem = selectedItem;
		this.add = add;
	}
	
	@Override
    protected Point getInitialSize() {
        return new Point(350, 110);
    }
	
	@Override
    protected Control createDialogArea(Composite parent) {		
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        area.setLayout(layout);        
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                
        Label label = new Label(area, SWT.NONE);
        label.setText(Localization.getString("label.category_name"));
        
        categoryNameField = new Text(area, SWT.BORDER);
        categoryNameField.setLayoutData(new GridData(GridData.FILL_BOTH));
        if (!add) {
        	this.categoryName = selectedItem[selectedItem.length - 1];
        	categoryNameField.setText(categoryName);        	
        }
        categoryNameField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                onCategoryNameModified();
            }
        });               
        
        return area;
    }
	
	protected void createButtonsForButtonBar(Composite parent) { 
		createButton(parent, IDialogConstants.OK_ID, Localization.getString("dialog.ok"),	true);
		createButton(parent, IDialogConstants.CANCEL_ID, Localization.getString("dialog.cancel"), false);
		
		getButton(IDialogConstants.OK_ID).setEnabled(!add);
	}
	
	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString(add ? "dialog.add_category" : "dialog.edit_category"));
    }
	
	private void onCategoryNameModified() {        
		getButton(IDialogConstants.OK_ID).setEnabled(!categoryNameIsEmpty());
		
		this.categoryName = categoryNameField.getText();
    }
	
	private boolean categoryNameIsEmpty() {
		return categoryNameField.getText().length() == 0;
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public String[] getSelectedItem() {
		return selectedItem;
	}
}
