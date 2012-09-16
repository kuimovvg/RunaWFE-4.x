package tk.eclipse.plugin.htmleditor.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import tk.eclipse.plugin.htmleditor.HTMLPlugin;

/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension OR
 * with the extension that matches the expected one (html).
 * 
 * @author Naoki Takezoe
 * @since 1.4.1
 */
public class URLNewWizardPage extends WizardNewFileCreationPage {
	
	private Text textURL;
//	private ISelection selection;
	
	public URLNewWizardPage(ISelection selection) {
		super("wizardPage",(IStructuredSelection)selection);
		setTitle(HTMLPlugin.getResourceString("URLNewWizardPage.Title"));
		setDescription(HTMLPlugin.getResourceString("URLNewWizardPage.Description"));
//		this.selection = selection;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		this.setFileName("newfile.url");
		Composite container = new Composite((Composite)getControl(),SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		container.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL));
		
		Label label = new Label(container, SWT.NULL);
		label.setText(HTMLPlugin.getResourceString("URLNewWizardPage.InputURL"));
		textURL = new Text(container, SWT.BORDER | SWT.SINGLE);
		textURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	protected InputStream getInitialContents() {
		StringBuffer sb = new StringBuffer();
		sb.append("URL=" + textURL.getText());
		return new ByteArrayInputStream(sb.toString().getBytes());
	}
}