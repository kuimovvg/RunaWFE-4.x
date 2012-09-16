package ru.runa.wf.email;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.base.Charsets;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.commons.IOCommons;
import ru.runa.commons.email.EmailResources;
import ru.runa.commons.xml.PathEntityResolver;
import ru.runa.commons.xml.XMLHelper;
import ru.runa.wf.web.html.ReflectionVarTagFactory;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

public class EmailSenderImpl implements ru.runa.commons.email.EmailSender {
    private static final Log log = LogFactory.getLog(EmailSenderImpl.class);

    @Override
    public void sendMessage(EmailResources resources, Subject subject, Map<String, Object> variables, Long tokenId, String tokenName)
            throws Exception {
        JbpmHtmlTaskFormParser parser = new JbpmHtmlTaskFormParser(subject, null);
        parser.setTask(tokenId, tokenName);

        sendMessage(resources, subject, variables, parser.getParsedForm());
    }

    private String message_from = null;
    private String message_to = null;
    private String message_cc = null;
    private String message_bcc = null;
    private String message_subject = null;
    private String message_body = null;

    private void readProperties(EmailResources resources, Subject subject, Map<String, Object> variables) throws AuthenticationException,
            WorkflowFormProcessingException {
        String emailTemplate = parseProperty(subject, resources.getEmailTemplate(), variables);
        if (emailTemplate != null) {
            readXML(emailTemplate, subject, variables);
        }
        if (message_from == null) {
            message_from = parseProperty(subject, resources.getFROM(), variables);
        }
        if (message_to == null) {
            message_to = parseProperty(subject, resources.getTO(), variables);
        }
        if (message_cc == null) {
            message_cc = parseProperty(subject, resources.getCC(), variables);
        }
        if (message_bcc == null) {
            message_bcc = parseProperty(subject, resources.getBCC(), variables);
        }
        if (message_subject == null) {
            message_subject = parseProperty(subject, resources.getSUBJECT(), variables);
        }
    }

    private static final String XSD_PATH = "/email-template.xsd";
    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_PATH);

    private String readElement(Document document, String name) {
        NodeList elements = document.getElementsByTagName(name);
        if (elements.getLength() == 1) {
            return elements.item(0).getTextContent();
        }
        return null;
    }

    private void readXML(String configuration, Subject subject, Map<String, Object> variables) {
        try {
            InputStream inputStream = EmailSenderImpl.class.getResourceAsStream(configuration);
            Document document = XMLHelper.getDocument(inputStream, PATH_ENTITY_RESOLVER);
            message_from = parseProperty(subject, readElement(document, "from"), variables);
            message_to = parseProperty(subject, readElement(document, "to"), variables);
            message_cc = parseProperty(subject, readElement(document, "cc"), variables);
            message_bcc = parseProperty(subject, readElement(document, "bcc"), variables);
            message_subject = parseProperty(subject, readElement(document, "subject"), variables);
            message_body = parseProperty(subject, readElement(document, "body"), variables);
        } catch (Exception e) {
            log.warn("Error reading email configuration from xml file " + configuration + ":" + e.getMessage());
        }

    }

    public void sendMessage(EmailResources resources, Subject subject, Map<String, Object> variables, String message) throws Exception {
        readProperties(resources, subject, variables);
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", parseProperty(subject, resources.getSMTPServerAddress(), variables));
        props.setProperty("mail.smtp.port", resources.getSMTPServerPort());

        if (resources.isAuthRequired()) {
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.user", resources.getSmtpUsername());
            props.setProperty("mail.smtp.password", resources.getSmtpPassword());
        }

        if (resources.isUsingSSL()) {
            props.setProperty("mail.smtp.socketFactory.port", resources.getSMTPServerPort());
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.pop3.socketFactory.fallback", "false");
        }

        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.debug", "true");

        Session session = Session.getInstance(props, new PasswordAuthenticator(subject, variables, resources));
        Message msg = new MimeMessage(session);
        if (message_from != null) {
            msg.setFrom(new InternetAddress(message_from));
        }
        if (message_to != null) {
            InternetAddress[] address = InternetAddress.parse(message_to);
            if (address == null || address.length == 0) {
                return;
            }
            msg.setRecipients(Message.RecipientType.TO, address);
        }
        if (message_cc != null) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(message_cc));
        }
        if (message_bcc != null) {
            msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(message_bcc));
        }
        if (message_subject != null) {
            msg.setSubject(MimeUtility.encodeText(message_subject, Charsets.UTF_8.name(), null));
        }
        BodyPart part = new MimeBodyPart();
        part.setContent(message_body == null ? message : message_body, resources.getContentType() + "; charset=\"UTF-8\"");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(part);
        msg.setContent(multipart);
        log.info("Connecting to " + props.getProperty("mail.smtp.host") + ":" + props.getProperty("mail.smtp.port"));
        Transport transport = session.getTransport();
        transport.connect();
        Transport.send(msg);
        transport.close();
        log.info("Email is send to " + message_to);
    }

    // TODO extract this pattern to a helper class
    private static final Pattern CUSTOM_TAG_PATTERN = Pattern.compile(
            "<customtag\\s+var\\s*=\\s*\"([^\"]+)\"\\s+delegation\\s*=\\s*\"([^\"]+)\"\\s*/>", Pattern.MULTILINE);

    private static String parseProperty(Subject subject, String property, Map<String, Object> variables) throws WorkflowFormProcessingException,
            AuthenticationException {
        if (property == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(property);
        Matcher matcher = CUSTOM_TAG_PATTERN.matcher(sb);
        for (int position = 0; matcher.find(position);) {
            int start = matcher.start();
            int end = matcher.end();
            String varName = matcher.group(1);
            String className = matcher.group(2);
            VarTag customTag = ReflectionVarTagFactory.create(className);
            // even if the variableMap.get(varName)==null we call varTag because
            // it's up to tag implementor
            // to decide whether it should throw an exception or not
            String replacement = customTag.getHtml(subject, varName, variables.get(varName), null);
            if (replacement == null) {
                replacement = "";
            }
            sb.replace(start, end, replacement);
            position = start + replacement.length();

        }
        return sb.toString();
    }

    private static class PasswordAuthenticator extends Authenticator {

        private final Map<String, Object> variables;

        private final Subject subject;

        EmailResources resources;

        public PasswordAuthenticator(Subject subject, Map<String, Object> variables, EmailResources resources) {
            this.subject = subject;
            this.variables = variables;
            this.resources = resources;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            String username = resources.getSmtpUsername();
            String password = resources.getSmtpPassword();
            if (username != null && password != null) {
                try {
                    return new PasswordAuthentication(parseProperty(subject, username, variables), parseProperty(subject, password, variables));
                } catch (Exception e) {
                    throw new InternalApplicationException(e);
                }
            }
            return null;
        }
    }

}
