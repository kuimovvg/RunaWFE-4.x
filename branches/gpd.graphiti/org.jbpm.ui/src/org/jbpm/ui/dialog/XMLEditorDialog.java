package org.jbpm.ui.dialog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.util.XmlUtil;
import org.xml.sax.SAXParseException;

public class XMLEditorDialog extends DelegableConfigurationDialog {

    private Label errorLabel;
    private boolean validateXSD;

    public XMLEditorDialog(String initialValue) {
        super(initialValue);
    }

    public void setValidateXSD(boolean validateXSD) {
        this.validateXSD = validateXSD;
    }

    @Override
    protected void createDialogHeader(Composite composite) {
        errorLabel = new Label(composite, SWT.NONE);
        errorLabel.setForeground(ColorConstants.red);
        errorLabel.setText("");
        errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    @Override
    protected void createDialogFooter(Composite composite) {
        styledText.addLineStyleListener(new XmlHighlightTextStyling());
    }

    private void setErrorLabelText(String text) {
        errorLabel.setText(text);
        errorLabel.pack(true);
    }

    @Override
    protected void okPressed() {
        try {
            String xml = styledText.getText();
            InputStream xmlStream = new ByteArrayInputStream(xml.getBytes(PluginConstants.UTF_ENCODING));
            if (validateXSD) {
                XmlUtil.parseDocumentValidateXSD(xmlStream);
            } else {
                XmlUtil.parseDocument(xmlStream);
            }
            super.okPressed();
        } catch (SAXParseException e) {
            int lineNumber = e.getLineNumber();
            int offset = e.getColumnNumber();
            setErrorLabelText(e.getMessage() + " [" + lineNumber + ", " + offset + "]");
        } catch (Exception e) {
            setErrorLabelText(e.getMessage());
        }
    }

}
