package ru.runa.wfe.commons.ftl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Charsets;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreemarkerProcessor {

    private final static Configuration cfg = new Configuration();
    static {
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setLocalizedLookup(false);
        cfg.setTemplateExceptionHandler(new CustomExceptionHandler());
    }

    public static String process(byte[] ftlBytes, FormHashModel model) {
        try {
            Template template = new Template("", new InputStreamReader(new ByteArrayInputStream(ftlBytes), Charsets.UTF_8), cfg,
                    Charsets.UTF_8.name());
            StringWriter out = new StringWriter();
            template.process(model, out);
            out.flush();
            return out.toString();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    private static class CustomExceptionHandler implements TemplateExceptionHandler {

        public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
            try {
                out.write("<b>" + te.getMessage() + "</b>" + te.getFTLInstructionStack());
            } catch (IOException e) {
                throw new TemplateException("Failed to print error message. Cause: " + e, env);
            }
        }
    }

}
