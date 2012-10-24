package org.jbpm.ui.orgfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.dialog.ChooseItemDialog;
import org.jbpm.ui.resource.Messages;

public class SwimlaneWFElement extends SwimlaneElement {
	private int mask;
	private Text selectionText;
	
	public SwimlaneWFElement() {
		setOrgFunctionDefinitionName("ExecutorByNameFunction");
	}

	public void setMask(String maskString) {
		this.mask = Integer.parseInt(maskString);
	}

    @Override
    public void createGUI(Composite parent) {
        Composite clientArea = createSection(parent, 2);

        selectionText = new Text(clientArea, SWT.READ_ONLY | SWT.BORDER);
        selectionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Hyperlink h1 = createLink(clientArea, Messages.getString("button.choose"));
        h1.addHyperlinkListener(new HyperlinkAdapter() {

            @SuppressWarnings("unchecked")
			@Override
            public void linkActivated(HyperlinkEvent e) {
                try {
                    List<String> items = new ArrayList<String>();
                    Map<String, Boolean> executors = WFEServerExecutorsImporter.getInstance().loadCachedData();
                    for (String name : executors.keySet()) {
                        boolean isGroup = executors.get(name);
                        if (isGroup && (mask & 2) != 0) {
                            items.add(name);
                        }
                        if (!isGroup && (mask & 1) != 0) {
                            items.add(name);
                        }
                    }
                    ChooseItemDialog dialog = new ChooseItemDialog(Messages.getString("WFDialog.Text"), null, true);
                    dialog.setItems(items);
                    dialog.setLabelProvider(new LabelProvider());
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        selectionText.setText((String) dialog.getSelectedItem());
                        OrgFunctionDefinition definition = createNew();
                        definition.getParameters().get(0).setValue(selectionText.getText());
                        fireCompletedEvent(definition);
                    }
                } catch (Exception ex) {
                    DesignerLogger.logError(ex);
                }
            }

        });
    }

    @Override
	public void open(String path, String swimlaneName, OrgFunctionDefinition definition) {
		super.open(path, swimlaneName, definition);
		String value = "";
		if (currentDefinition != null && currentDefinition.getParameters().size() > 0) {
			value = currentDefinition.getParameters().get(0).getValue();
		}
		selectionText.setText(value);
	}

}
