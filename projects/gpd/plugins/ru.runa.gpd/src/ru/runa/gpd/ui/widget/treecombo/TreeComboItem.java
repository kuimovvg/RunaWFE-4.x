package ru.runa.gpd.ui.widget.treecombo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;

public class TreeComboItem extends Item {

	static final String DATA_ID = "treecombo.treeitem";
	private TreeCombo parent;
	private TreeComboItem parentItem;
	private List<TreeComboItem> childItems = new ArrayList<TreeComboItem>();
	private TreeItem realTreeItem;
	
	private String[] fullPath;
	
	public TreeComboItem(TreeComboItem parentItem, int style, int index) {
		super(parentItem.parent, style);
		this.parent = parentItem.parent;
		this.parentItem = parentItem;
		this.parentItem.childItems.add(index, this);
		
		if( parentItem.realTreeItem != null && ! parentItem.realTreeItem.isDisposed() ) {
			setRealTreeItem(new TreeItem(parentItem.realTreeItem,style,index));
		}
		
		initFullPath(parentItem);
	}

	public TreeComboItem(TreeComboItem parentItem, int style) {
		super(parentItem.parent,style);
		this.parent = parentItem.parent;
		this.parentItem = parentItem;
		this.parentItem.childItems.add(this);
		
		if( parentItem.realTreeItem != null && ! parentItem.realTreeItem.isDisposed() ) {
			setRealTreeItem(new TreeItem(parentItem.realTreeItem,style));
		}
		
		initFullPath(parentItem);
	}
	
	public TreeComboItem(TreeCombo parent, int style, int index) {
		super(parent, style);
		this.parent = parent;
		this.parent.items.add(index, this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeItem(new TreeItem(this.parent.tree,style,index));
		}
		
		initEmptyFullPath();
	}

	public TreeComboItem(TreeCombo parent, int style) {
		super(parent, style);
		this.parent = parent;
		this.parent.items.add(this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeItem(new TreeItem(this.parent.tree,style));
		}
		
		initEmptyFullPath();
	}
	
	protected void initEmptyFullPath() {
		this.fullPath = new String[1];
	}
	
	protected void initFullPath(TreeComboItem parentItem) {
		String[] parentFullPath = parentItem.getFullPath();
		this.fullPath = new String[parentFullPath.length + 1];
		System.arraycopy(parentFullPath, 0, this.fullPath, 0, parentFullPath.length);
	}
	
	protected void updateParentFullPath(String[] parentFullPath) {
		System.arraycopy(parentFullPath, 0, this.fullPath, 0, this.fullPath.length - 1);
	}
	
	protected void updateChildItemsFullPath() {
		for (TreeComboItem childItem : childItems) {
			childItem.updateParentFullPath(this.fullPath);
		}
	}
	
	public void dispose() {
		super.dispose();
		if( realTreeItem != null && !realTreeItem.isDisposed() ) {
			realTreeItem.dispose();
		}
		
		if( this.parentItem != null && ! this.parentItem.isDisposed() ) {
			this.parentItem.childItems.remove(this);
		}
		
		for( TreeComboItem i: childItems ) {
			i.dispose();
		}
	}
	
	void setRealTreeItem(TreeItem realTreeItem) {
		this.realTreeItem = realTreeItem;
		this.realTreeItem.setData(DATA_ID, this);
	}
	
	TreeItem getRealTreeItem() {
		return this.realTreeItem;
	}
	
	public TreeComboItem[] getItems() {
		return childItems.toArray(new TreeComboItem[0]);
	}

	private boolean checkRealItem() {
		return realTreeItem != null && ! realTreeItem.isDisposed();
	}
	
	@Override
	public void setImage(Image image) {
		super.setImage(image);
		if( checkRealItem() ) {
			realTreeItem.setImage(image);
		}
	}

	@Override
	public void setText(String string) {
		super.setText(string);
		
		if( checkRealItem() ) {
			realTreeItem.setText(string);
		}
		
		this.fullPath[this.fullPath.length - 1] = string;
		
		updateChildItemsFullPath();
	}

	public Rectangle getBounds(int i) { 
		if( checkRealItem() ) {
			return realTreeItem.getBounds(i);
		}
		return null;
	}

	public Rectangle getBounds() {
		if( checkRealItem() ) {
			return realTreeItem.getBounds();
		}
		return null;
	}

	public TreeCombo getParent() {
		return parent;
	}

	public Color getBackground(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getBackground(columnIndex);
		}
		return null;
	}

	public Font getFont(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getFont(columnIndex);
		}
		return null;
	}

	public Color getForeground(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getForeground(columnIndex);
		}
		return null;
	}

	public Image getImage(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getImage(columnIndex);
		}
		return null;
	}

	public String getText(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getText(columnIndex); 
		}
		return null;
	}

	public void setBackground(int columnIndex, Color color) {
		if( checkRealItem() ) {
			realTreeItem.setBackground(columnIndex, color);
		}
	}

	public void setFont(int columnIndex, Font font) {
		if( checkRealItem() ) {
			realTreeItem.setFont(columnIndex, font);
		}
	}

	public void setForeground(int columnIndex, Color color) {
		if( checkRealItem() ) {
			realTreeItem.setForeground(columnIndex, color);
		}
	}

	public void setImage(int columnIndex, Image image) {
		if( checkRealItem() ) {
			realTreeItem.setImage(columnIndex, image);
		}
	}

	public void setText(int columnIndex, String string) {
		if( checkRealItem() ) {
			realTreeItem.setText(columnIndex, string);
		}
	}

	public TreeComboItem getParentItem() {
		return parentItem;
	}

	public boolean getExpanded() {
		if( checkRealItem() ) {
			return realTreeItem.getExpanded();
		}
		return false;
	}

	public int getItemCount() {
		return childItems.size();
	}

	public TreeComboItem getItem(int i) {
		return childItems.get(i);
	}

	public int indexOf(TreeComboItem item) {
		return childItems.indexOf(item); 
	}

	public Rectangle getTextBounds(int index) {
		if( checkRealItem() ) {
			return realTreeItem.getBounds(index);
		}
		
		return null;
	}

	public Rectangle getImageBounds(int index) {
		if( checkRealItem() ) {
			return realTreeItem.getImageBounds(index);
		}
		return null;
	}

	public void setExpanded(boolean expand) {
		if( checkRealItem() ) {
			realTreeItem.setExpanded(expand);
		}
	}

	public void setItemCount(int count) {
		if( checkRealItem() ) {
			realTreeItem.setItemCount(count);
		}
	}

	public void clear(int indexToDisaccociate, boolean b) {
		realTreeItem.clear(indexToDisaccociate, b);
	}

	public void clearAll(boolean b) {
		realTreeItem.clearAll(b);
	}
	
	public String[] getFullPath() {
		return fullPath;
	}
}
