package ru.runa.notifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.runa.notifier.util.ResourcesManager;
import ru.runa.notifier.view.ViewChangeListener;

public class InfoPathFormView extends Composite implements LocationListener {
    private static final Log log = LogFactory.getLog(InfoPathFormView.class);

    private OleFrame oleFrame;

    private OleControlSite controlSite;

    private OleAutomation automation;

    private Button submitFormButton;

    private Button cancelFormButton;

    private ViewChangeListener viewChangeListener;

    private boolean isInfoPathComHasBeenCreated = false;

    private static final UrlHelper urlHelper = new UrlHelper();

    public InfoPathFormView(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));

        try {
            oleFrame = new OleFrame(this, SWT.FILL);
            GridData gridData = new GridData();
            gridData.horizontalAlignment = GridData.FILL;
            gridData.verticalAlignment = GridData.FILL;
            gridData.horizontalSpan = 2;
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
            oleFrame.setLayoutData(gridData);
            
            controlSite = new OleControlSite(oleFrame, SWT.NONE, "ru.runa.ipwctrl.InfoPathWrapperControl");
            controlSite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
            automation = new OleAutomation(controlSite);

            cancelFormButton = new Button(this, SWT.PUSH);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.minimumWidth = 200;
            data.horizontalAlignment = GridData.END;
            cancelFormButton.setLayoutData(data);
            cancelFormButton.setText(ResourcesManager.getProperty("infopath.cancel.button.name"));
            cancelFormButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    closeInfoPathForm();
                    viewChangeListener.showBrowserView();
                }
            });

            submitFormButton = new Button(this, SWT.PUSH);
            data = new GridData();
            data.horizontalAlignment = GridData.END;
            data.minimumWidth = 200;
            submitFormButton.setLayoutData(data);
            submitFormButton.setText(ResourcesManager.getProperty("infopath.submit.button.name"));
            submitFormButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (isFormValidated()) {
                        submitFormData();
                        closeInfoPathForm();
                        viewChangeListener.showBrowserView();
                    }
                }
            });
            isInfoPathComHasBeenCreated = true;
        } catch (Throwable e) {
            log.warn("InfoPath COM control was not created succesfully due to error" + e.getMessage());
        }
    }

    public void setViewChangeListener(ViewChangeListener formClosedListener) {
        this.viewChangeListener = formClosedListener;
    }

    public void changed(LocationEvent locationEvent) {
        // Do nothing
    }

    public void changing(LocationEvent locationEvent) {
        String url = locationEvent.location;
        if (isInfoPathComHasBeenCreated && url.contains("xsn")) {
            locationEvent.doit = false;
            viewChangeListener.showInfoPathFormView();

            log.debug("Parsing URL: " + url);

            String parametersString = url.substring(url.indexOf("?") + 1);
            urlHelper.initFromParameters(parametersString);

            {
                Browser browser = ((Browser) locationEvent.widget);
                CookieFinder cookieFinder = new CookieFinder();
                browser.addStatusTextListener(cookieFinder);
                browser.execute("window.status = document.cookie;");
                String cookie = cookieFinder.getCookie();
                browser.removeStatusTextListener(cookieFinder);
                if (cookie != null) {
                    setPropertyToCOMObject("DocumentCookie", cookie);
                }
            }
            setPropertyToCOMObject("TemplateURL", urlHelper.getTemplateUrl());
            if (urlHelper.shouldLoadVariables()) {
                setPropertyToCOMObject("LoadFromURL", urlHelper.getLoadVariablesUrl());
            }
            setPropertyToCOMObject("DocumentURL", url);
            setPropertyToCOMObject("SubmitToURL", urlHelper.getSubmitUrl());
            submitFormButton.setEnabled(isFormTemplateLoaded());
        }
        if (isInfoPathComHasBeenCreated && url.contains(ResourcesManager.LOGOUT_ACTION)) {
            String dir = System.getProperty("user.dir");
            log.info("User dir: " + dir);
            openCloseEmptyFormTemplate(dir + "\\BlankTemplate.xsn");
        }
    }

    private void setPropertyToCOMObject(String propertyName, String value) {
        int[] rgdispid = automation.getIDsOfNames(new String[] { propertyName });
        int dispIdMember = rgdispid[0];
        Variant templateUrl = new Variant(value);
        automation.setProperty(dispIdMember, templateUrl);
    }

    private int getMethodDispatchId(String methodName) {
        int[] rgdispid = automation.getIDsOfNames(new String[] { methodName });
        if (rgdispid == null) {
            throw new UnsupportedOperationException("Method '" + methodName + "' was not found in COM object");
        }
        if (rgdispid.length != 1) {
            throw new UnsupportedOperationException("Method '" + methodName + "' has more than 1 dispatch id");
        }
        return rgdispid[0];
    }

    private Variant executeMethod(String methodName) {
        return automation.invoke(getMethodDispatchId(methodName));
    }

    private Variant executeMethod(String methodName, String... arguments) {
        Variant[] params = new Variant[arguments.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = new Variant(arguments[i]);
        }
        return automation.invoke(getMethodDispatchId(methodName), params);
    }

    private void closeInfoPathForm() {
        executeMethod("CloseForm");
    }

    private boolean isFormValidated() {
        return executeMethod("ValidateForm").getBoolean();
    }

    private void submitFormData() {
        String result = executeMethod("SubmitForm").getString();
        log.info("Submit: " + result);
    }

    private void openCloseEmptyFormTemplate(String templatePath) {
        String result = executeMethod("OpenDummyForm", templatePath).getString();
        log.info("Open blank form result: " + result);
    }

    private boolean isFormTemplateLoaded() {
        return executeMethod("isFormTemplateLoaded").getBoolean();
    }

    private static class UrlHelper {
        private String xsnFileName;
        private String taskId;
        private String taskName;
        private String definitionId;
        private String actorId;

        public void initFromParameters(String parametersString) {
            xsnFileName = null;
            taskId = null;
            taskName = null;
            definitionId = null;
            actorId = null;
            String[] parametersSplitted = parametersString.split("&");
            for (String paramWithValue : parametersSplitted) {
                if (paramWithValue.startsWith("fileName")) {
                    xsnFileName = paramWithValue.substring(paramWithValue.indexOf("=") + 1);
                }
                if (paramWithValue.startsWith("definitionId")) {
                    definitionId = paramWithValue.substring(paramWithValue.indexOf("=") + 1);
                }
                if (paramWithValue.startsWith("taskId")) {
                    taskId = paramWithValue.substring(paramWithValue.indexOf("=") + 1);
                }
                if (paramWithValue.startsWith("actorId")) {
                    actorId = paramWithValue.substring(paramWithValue.indexOf("=") + 1);
                }
                if (paramWithValue.startsWith("taskName")) {
                    taskName = paramWithValue.substring(paramWithValue.indexOf("=") + 1);
                }
            }
            if (xsnFileName == null || definitionId == null) {
                throw new RuntimeException("Invalid URL on wfe-web!");
            }
        }

        public String getTemplateUrl() {
            return "/wfe/getXsnFormFile.do?def_id=" + definitionId + "&fileName=" + xsnFileName;
        }

        public String getLoadVariablesUrl() {
            return "/wfe/getXsnVariables.do?task_id=" + taskId + "&task_name=" + taskName;
        }

        public boolean shouldLoadVariables() {
            return (taskId != null && taskName != null);
        }

        public String getSubmitUrl() {
            String submitUrl = "/wfe/submitInfoPathForm.do?";
            if (taskId != null) {
                submitUrl += "taskId=" + taskId + "&taskName=" + taskName + "&actorId=" + actorId + "&";
            } else {
                submitUrl += "definitionId=" + definitionId + "&";
            }
            return submitUrl;
        }
    }
    
    public static class CookieFinder implements StatusTextListener {
        private String cookie;

        public void changed(StatusTextEvent statusTextEvent) {
            if (statusTextEvent.text.startsWith("JSESSIONID")) {
                cookie = statusTextEvent.text;
            }
        }

        public String getCookie() {
            return cookie;
        }
    }
}

