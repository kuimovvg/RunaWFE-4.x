package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.runa.gpd.Localization;

public class MailNode extends Node {
    private static final String RECIPIENT_PROP = "recipient";

    private static final String SUBJECT_PROP = "subject";

    private static final String MAIL_BODY_PROP = "mailBody";

    private static final List<IPropertyDescriptor> DESCRIPTORS = new ArrayList<IPropertyDescriptor>();
    static {
        DESCRIPTORS.add(new TextPropertyDescriptor(RECIPIENT_PROP, Localization.getString("MailNode.property.recipient")));
        DESCRIPTORS.add(new TextPropertyDescriptor(SUBJECT_PROP, Localization.getString("MailNode.property.subject")));
        DESCRIPTORS.add(new TextPropertyDescriptor(MAIL_BODY_PROP, Localization.getString("MailNode.property.mailBody")));
    }

    private String recipient;

    private String subject;

    private String mailBody;

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    protected List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (RECIPIENT_PROP.equals(id)) {
            return getRecipient();
        } else if (SUBJECT_PROP.equals(id)) {
            return getSubject();
        } else if (MAIL_BODY_PROP.equals(id)) {
            return getMailBody();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (RECIPIENT_PROP.equals(id)) {
            setRecipient((String) value);
        } else if (SUBJECT_PROP.equals(id)) {
            setSubject((String) value);
        } else if (MAIL_BODY_PROP.equals(id)) {
            setMailBody((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return super.allowLeavingTransition(target, transitions) && transitions.size() == 0;
    }

}
