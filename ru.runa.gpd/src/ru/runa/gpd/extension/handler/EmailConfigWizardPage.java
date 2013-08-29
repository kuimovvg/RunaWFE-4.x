package ru.runa.gpd.extension.handler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.handler.ParamDefComposite.MessageDisplay;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.ui.dialog.ChooseVariableDialog;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.var.FileVariable;

public class EmailConfigWizardPage extends WizardPage implements MessageDisplay {
    private TabFolder tabFolder;
    private ParamDefComposite commonComposite;
    private ParamDefDynaComposite connectionComposite;
    private ParamDefDynaComposite messageComposite;
    private ContentComposite contentComposite;
    private StyledText styledText;
    private final boolean bodyInlinedEnabled;
    private final String initValue;
    private final ParamDefConfig summaryConfig = new ParamDefConfig("email-config");
    private final ParamDefConfig commonConfig;
    private final ParamDefConfig connectionConfig;
    private final ParamDefConfig messageConfig;
    private final ParamDefConfig contentConfig;
    private final Delegable delegable;
    private String result;

    private ParamDefConfig getParamConfig(Bundle bundle, String path) {
        try {
            InputStream is = bundle.getEntry(path).openStream();
            Document doc = DocumentHelper.parseText(IOUtils.readStream(is));
            ParamDefConfig config = ParamDefConfig.parse(doc);
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Unable parse config at " + path, e);
        }
    }

    public EmailConfigWizardPage(Bundle bundle, Delegable delegable) {
        super("email", Localization.getString("EmailDialog.title"), SharedImages.getImageDescriptor("/icons/send_email.png"));
        this.initValue = delegable.getDelegationConfiguration();
        this.delegable = delegable;
        GraphElement parent = ((GraphElement) delegable).getParent();
        this.bodyInlinedEnabled = (parent instanceof FormNode) && ((FormNode) parent).hasForm();
        this.commonConfig = getParamConfig(bundle, "/conf/email.common.xml");
        this.connectionConfig = getParamConfig(bundle, "/conf/email.connection.xml");
        this.messageConfig = getParamConfig(bundle, "/conf/email.message.xml");
        this.contentConfig = getParamConfig(bundle, "/conf/email.content.xml");
        summaryConfig.getGroups().addAll(commonConfig.getGroups());
        summaryConfig.getGroups().addAll(connectionConfig.getGroups());
        summaryConfig.getGroups().addAll(messageConfig.getGroups());
        summaryConfig.getGroups().addAll(contentConfig.getGroups());
    }

    @Override
    public void createControl(Composite parent) {
        tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        ScrolledComposite scrolledComposite;
        scrolledComposite = createScrolledTab(Localization.getString("EmailDialog.title.common"));
        commonComposite = new ParamDefComposite(scrolledComposite, delegable, commonConfig, commonConfig.parseConfiguration(initValue));
        commonComposite.setHelpInlined(true);
        commonComposite.createUI();
        scrolledComposite.setContent(commonComposite);
        scrolledComposite = createScrolledTab(Localization.getString("EmailDialog.title.connection"));
        connectionComposite = new ParamDefDynaComposite(scrolledComposite, delegable, connectionConfig, connectionConfig.parseConfiguration(initValue), connectionConfig
                .getGroups().get(0), Localization.getString("EmailDialog.connection.descDynaParams"));
        connectionComposite.setMenuForSettingVariable(true);
        connectionComposite.createUI();
        connectionComposite.setMessageDisplay(this);
        scrolledComposite.setContent(connectionComposite);
        scrolledComposite = createScrolledTab(Localization.getString("EmailDialog.title.message"));
        messageComposite = new ParamDefDynaComposite(scrolledComposite, delegable, messageConfig, messageConfig.parseConfiguration(initValue), messageConfig.getGroups().get(0),
                Localization.getString("EmailDialog.message.descDynaParams"));
        messageComposite.setMenuForSettingVariable(true);
        messageComposite.createUI();
        messageComposite.setMessageDisplay(this);
        scrolledComposite.setContent(messageComposite);
        scrolledComposite = createScrolledTab(Localization.getString("EmailDialog.title.content"));
        contentComposite = new ContentComposite(scrolledComposite, contentConfig.parseConfiguration(initValue), EmailAttachmentsConfig.parseAttachments(initValue));
        scrolledComposite.setContent(contentComposite);
        styledText = new StyledText(tabFolder, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.addLineStyleListener(new XmlHighlightTextStyling());
        styledText.setText(this.initValue);
        styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
        styledText.setEditable(false);
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText(Localization.getString("EmailDialog.title.xml"));
        tabItem.setControl(styledText);
        tabFolder.addSelectionListener(new TabSelectionHandler());
        setControl(tabFolder);
    }

    private ScrolledComposite createScrolledTab(String header) {
        Composite composite = new Composite(tabFolder, SWT.NONE);
        composite.setLayout(new GridLayout());
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText(header);
        tabItem.setControl(composite);
        ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinHeight(200);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return scrolledComposite;
    }

    @Override
    public void setMessages(String message, String errorMessage) {
        setDescription(message);
        setErrorMessage(errorMessage);
    }

    private class TabSelectionHandler extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            generateCode();
        }
    }

    private class ContentComposite extends Composite {
        private final HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());
        private final Button bodyInlinedButton;
        private final StyledText styledText;
        private final TableViewer tableViewer;
        private final List<String> attachments;

        public ContentComposite(Composite parent, Map<String, String> properties, List<String> attachments) {
            super(parent, SWT.NONE);
            this.attachments = attachments;
            setLayout(new GridLayout(3, false));
            setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            GridData gridData;
            Label label = new Label(this, SWT.NONE);
            gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            label.setLayoutData(gridData);
            label.setText(Localization.getString("mail.bodyInlined"));
            bodyInlinedButton = new Button(this, SWT.CHECK);
            gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 150;
            bodyInlinedButton.setLayoutData(gridData);
            bodyInlinedButton.setEnabled(bodyInlinedEnabled);
            String selectedValue = properties.get("bodyInlined");
            if (selectedValue == null) {
                selectedValue = "true";
            }
            bodyInlinedButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    styledText.setEditable(!bodyInlinedButton.getSelection());
                }
            });
            Hyperlink hl3 = new Hyperlink(this, SWT.NONE);
            gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
            hl3.setLayoutData(gridData);
            hl3.setText(Localization.getString("button.insert_variable"));
            hl3.addHyperlinkListener(new LoggingHyperlinkAdapter() {
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    List<String> ftlVariableNames = VariableUtils.getValidVariableNames(delegable.getVariableNames(true));
                    ChooseVariableDialog dialog = new ChooseVariableDialog(ftlVariableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        String r = VariableUtils.wrapVariableName(variableName);
                        styledText.insert(r);
                        styledText.setFocus();
                        styledText.setCaretOffset(styledText.getCaretOffset() + r.length());
                    }
                }
            });
            hyperlinkGroup.add(hl3);
            styledText = new StyledText(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
            styledText.setLineSpacing(2);
            gridData = new GridData(GridData.FILL_BOTH);
            gridData.horizontalSpan = 3;
            gridData.heightHint = 150;
            styledText.setLayoutData(gridData);
            String message = properties.get("body");
            if (message == null) {
                message = "";
            }
            styledText.setText(message);
            styledText.setFocus();
            Composite dynaComposite = new Composite(this, SWT.NONE);
            gridData = new GridData(GridData.FILL_BOTH);
            gridData.horizontalSpan = 3;
            gridData.heightHint = 150;
            dynaComposite.setLayoutData(gridData);
            dynaComposite.setLayout(new GridLayout(2, false));
            tableViewer = new TableViewer(dynaComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            gridData = new GridData(GridData.FILL_BOTH);
            tableViewer.getControl().setLayoutData(gridData);
            Table table = tableViewer.getTable();
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
            String[] columnNames = new String[] { Localization.getString("EmailDialog.title.attachments") };
            int[] columnWidths = new int[] { 200 };
            int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
            for (int i = 0; i < columnNames.length; i++) {
                TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
                tableColumn.setText(columnNames[i]);
                tableColumn.setWidth(columnWidths[i]);
            }
            tableViewer.setContentProvider(new ArrayContentProvider());
            setTableInput();
            Composite buttonsBar = new Composite(dynaComposite, SWT.NONE);
            gridData = new GridData(GridData.FILL_VERTICAL);
            buttonsBar.setLayoutData(gridData);
            buttonsBar.setLayout(new GridLayout(1, false));
            createButton(buttonsBar, Localization.getString("button.add"), new AddSelectionAdapter());
            final Button deleteButton = createButton(buttonsBar, Localization.getString("button.delete"), new DeleteSelectionAdapter());
            deleteButton.setEnabled(false);
            tableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
                @Override
                protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                    deleteButton.setEnabled(!tableViewer.getSelection().isEmpty());
                }
            });
            bodyInlinedButton.setSelection(bodyInlinedEnabled && "true".equals(selectedValue));
        }

        private Button createButton(Composite parent, String label, SelectionAdapter selectionAdapter) {
            Button button = new Button(parent, SWT.PUSH);
            button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
            button.setText(label);
            button.setFont(JFaceResources.getDialogFont());
            button.addSelectionListener(selectionAdapter);
            return button;
        }

        public List<String> getAttachments() {
            return attachments;
        }

        private void setTableInput() {
            tableViewer.setInput(attachments);
        }

        private class AddSelectionAdapter extends LoggingSelectionAdapter {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                List<String> fileVariableNames = VariableUtils.getValidVariableNames(delegable.getVariableNames(true, FileVariable.class.getName()));
                ChooseVariableDialog dialog = new ChooseVariableDialog(fileVariableNames);
                String variableName = dialog.openDialog();
                if (variableName != null) {
                    attachments.add(variableName);
                    setTableInput();
                }
            }
        }

        private class DeleteSelectionAdapter extends LoggingSelectionAdapter {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String data = (String) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                attachments.remove(data);
                setTableInput();
            }
        }

        private boolean isUseFormFromTaskForm() {
            return bodyInlinedButton.getSelection();
        }

        public String getMessage() {
            return styledText.getText();
        }
    }

    public String generateCode() {
        Map<String, String> summaryProperties = new HashMap<String, String>();
        summaryProperties.putAll(commonComposite.readUserInput());
        summaryProperties.putAll(connectionComposite.readUserInput());
        summaryProperties.putAll(messageComposite.readUserInput());
        summaryProperties.put("bodyInlined", String.valueOf(contentComposite.isUseFormFromTaskForm()));
        summaryProperties.put("body", contentComposite.getMessage());
        Document document = summaryConfig.toConfigurationXml(delegable.getVariableNames(true), summaryProperties);
        EmailAttachmentsConfig.addAttachments(document, contentComposite.getAttachments());
        String c = XmlUtil.toString(document);
        styledText.setText(c);
        this.result = styledText.getText();
        return result;
    }

    public String getResult() {
        return result;
    }
}
