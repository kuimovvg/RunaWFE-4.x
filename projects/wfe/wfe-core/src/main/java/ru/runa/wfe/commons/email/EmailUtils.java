package ru.runa.wfe.commons.email;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.email.EmailConfig.Attachment;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class EmailUtils {

    private static final Log log = LogFactory.getLog(EmailConfig.class);

    private static final String MAIL_TRANSPORT = "mail.transport.protocol";
    private static final String MAIL_HOST = "mail.host";
    private static final String MAIL_USER = "mail.user";
    private static final String MAIL_PASSWORD = "mail.password";

    private static MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    static {
        System.setProperty("mail.mime.encodefilename", "true");
    }

    public static void sendMessage(EmailConfig config, List<Attachment> attachments) throws Exception {
        config.checkValid();
        Properties props = new Properties();
        props.putAll(config.getConnectionProperties());

        String protocol = props.getProperty(MAIL_TRANSPORT);

        String connectionTimepoutPropName = "mail." + protocol + ".connectiontimeout";
        if (!props.contains(connectionTimepoutPropName)) {
            props.put(connectionTimepoutPropName, "5000");
        }
        String timepoutPropName = "mail." + protocol + ".timeout";
        if (!props.contains(timepoutPropName)) {
            props.put(timepoutPropName, "5000");
        }

        if (config.getHeaderProperties().containsKey("Subject")) {
            String subject = config.getHeaderProperties().get("Subject");
            try {
                subject = MimeUtility.encodeText(subject, Charsets.UTF_8.name(), null);
                config.getHeaderProperties().put("Subject", subject);
            } catch (UnsupportedEncodingException e) {
            }
        }

        PasswordAuthenticator authenticator = null;
        boolean auth = "true".equals(props.getProperty("mail." + protocol + ".auth"));
        if (auth) {
            String username = props.getProperty(MAIL_USER);
            String password = props.getProperty(MAIL_PASSWORD);
            Preconditions.checkNotNull(username, "Authenticaton enabled but property " + MAIL_USER + " is not set");
            Preconditions.checkNotNull(password, "Authenticaton enabled but property " + MAIL_PASSWORD + " is not set");
            authenticator = new PasswordAuthenticator(username, password);
            if (!config.getHeaderProperties().containsKey("From")) {
                config.getHeaderProperties().put("From", username);
            }
        }

        Session session = Session.getInstance(props, authenticator);
        MimeMessage msg = new MimeMessage(session);
        for (String headerName : config.getHeaderProperties().keySet()) {
            String headerValue = config.getHeaderProperties().get(headerName);
            msg.setHeader(headerName, headerValue);
        }
        Multipart multipart = new MimeMultipart("related");
        MimeBodyPart part = new MimeBodyPart();
        part.setText(config.getMessage(), Charsets.UTF_8.name(), config.getMessageType());
        multipart.addBodyPart(part);
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                MimeBodyPart attach = new MimeBodyPart();
                attach.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment.content, fileTypeMap.getContentType(attachment.fileName))));
                if (attachment.inlined) {
                    attach.setHeader("Content-ID", attachment.fileName);
                    attach.setDisposition(Part.INLINE);
                } else {
                    attach.setFileName(attachment.fileName);
                }
                multipart.addBodyPart(attach);
            }
        }
        msg.setContent(multipart);
        log.info("Connecting to [" + protocol + "]: " + props.getProperty(MAIL_HOST) + ":" + props.getProperty("mail." + protocol + ".port"));
        Transport transport = session.getTransport();
        try {
            transport.connect();
            msg.saveChanges();
            transport.sendMessage(msg, msg.getAllRecipients());
            log.info("Message sent to " + Arrays.asList(msg.getRecipients(RecipientType.TO)));
        } finally {
            transport.close();
        }
    }

    public static void sendTaskMessage(Subject subject, EmailConfig config, Interaction interaction, IVariableProvider variableProvider,
            IFileDataProvider fileDataProvider) throws Exception {
        config.applySubstitutions(variableProvider);
        byte[] formBytes;
        if (config.isUseMessageFromTaskForm()) {
            if (!interaction.hasForm()) {
                throw new Exception("Set property 'UseMessageFromTaskForm' but form does not exist");
            }
            formBytes = interaction.getFormData();
        } else {
            formBytes = config.getMessage().getBytes(Charsets.UTF_8);
        }
        FormHashModel model = new FormHashModel(subject, null, variableProvider, null);
        String formMessage = FreemarkerProcessor.process(formBytes, model);

        Map<String, String> replacements = new HashMap<String, String>();

        List<Attachment> attachments = new ArrayList<Attachment>();
        // List<String> images = HTMLUtils.findImages(formBytes);
        // for (String image : images) {
        // Attachment attachment = new Attachment();
        // attachment.fileName = image;
        // attachment.content =
        // fileDataProvider.getFileDataNotNull(attachment.fileName);
        // attachment.inlined = true;
        // attachments.add(attachment);
        // replacements.put(attachment.fileName, "cid:" + attachment.fileName);
        // } TODO images not supported
        for (String variableName : config.getAttachments()) {
            FileVariable fileVariable = variableProvider.get(FileVariable.class, variableName);
            if (fileVariable != null) {
                Attachment attachment = new Attachment();
                attachment.fileName = fileVariable.getName();
                attachment.content = fileVariable.getData();
                attachments.add(attachment);
            }
        }
        for (String repl : replacements.keySet()) {
            formMessage = formMessage.replaceAll(repl, replacements.get(repl));
        }
        config.setMessage(formMessage);
        sendMessage(config, attachments);
    }

    private static class PasswordAuthenticator extends Authenticator {

        private final String username;
        private final String password;

        public PasswordAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}
