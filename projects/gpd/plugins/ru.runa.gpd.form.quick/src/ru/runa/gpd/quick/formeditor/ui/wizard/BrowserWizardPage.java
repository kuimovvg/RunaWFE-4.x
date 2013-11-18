package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.util.FormPresentationUtils;

public class BrowserWizardPage extends WizardPage {
	private String htmlContent;
	
	protected BrowserWizardPage(String pageName, String htmlContent) {
		super(pageName);
		setTitle(Messages.getString("BrowserWizardPage.page.title"));
        setDescription(Messages.getString("BrowserWizardPage.page.description"));
		this.htmlContent = htmlContent;
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Browser browser = new Browser(composite, SWT.NULL);
		browser.setBounds(5, 5, 800, 600);
		String html = FormPresentationUtils.adjustForm(htmlContent, null);
		//apply img
		URL fileUrl = null;
		Enumeration<URL> enumUrls = Platform.getBundle("ru.runa.gpd.form.quick").findEntries("/img/", "background.jpg", true);
        while (enumUrls.hasMoreElements()) {
            URL bundleUrl = enumUrls.nextElement();
            try {
                fileUrl = FileLocator.toFileURL(bundleUrl);
            } catch (IOException e) {
                throw new RuntimeException("Error loading background img from: " + bundleUrl);
            }
        }
        
		if(fileUrl != null) {
			html = html.replaceFirst("<BODY>", "<BODY><img src=\""+ fileUrl.toString() +"\"><DIV style=\"position:absolute;left:275px;top:130px;\">");
			html = html.replaceFirst("</BODY>", "</DIV></BODY>");
		}        
		
		browser.setText(html);
		setControl(composite);
	}

}
