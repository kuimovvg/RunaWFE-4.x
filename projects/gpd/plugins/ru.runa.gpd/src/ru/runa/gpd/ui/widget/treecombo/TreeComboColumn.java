package ru.runa.gpd.ui.widget.treecombo;

import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeColumn;

public class TreeComboColumn extends Item {

	private TreeColumn realTreeColumn;
	private TreeCombo parent;
	private int width;
	
	public TreeComboColumn(TreeCombo parent, int style) {
		super(parent, style);
		this.parent = parent;
		this.parent.columns.add(this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeColumn(new TreeColumn(parent.tree,style));	
		}		
	}
	
	public TreeComboColumn(TreeCombo parent, int style, int index) {
		super(parent, style, index);
		this.parent = parent;
		this.parent.columns.add(this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeColumn(new TreeColumn(parent.tree,style,index));	
		}
	}

	void setRealTreeColumn(TreeColumn realTreeColumn) {
		this.realTreeColumn = realTreeColumn;
		this.realTreeColumn.setText(getText());
		this.realTreeColumn.setWidth(width);
	}
	
	TreeColumn getRealTreeColumn() {
		return realTreeColumn;
	}

	private boolean checkRealColumn() {
		return realTreeColumn != null && ! realTreeColumn.isDisposed();
	}
	
	public int getWidth() {
		if( checkRealColumn() ) {
			return realTreeColumn.getWidth();
		}
		return 0;
	}
	
	public void setWidth(int width) {
		this.width = width;
		realTreeColumn.setWidth(width);
	}

	@Override
	public void setText(String string) {
		super.setText(string);
		realTreeColumn.setText(string);
	}
}
