package ru.runa.wfe.commons.ftl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

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

    public static String process(String ftlTemplate, FormHashModel model) {
        try {
            if (ftlTemplate == null) {
                return null;
            }
            Template template = new Template("", new StringReader(ftlTemplate), cfg, Charsets.UTF_8.name());
            StringWriter out = new StringWriter();
            template.process(model, out);
            out.flush();
            return out.toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static class CustomExceptionHandler implements TemplateExceptionHandler {

        @Override
        public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
            try {
                out.write("<b>" + te.getMessage() + "</b>" + te.getFTLInstructionStack());
            } catch (IOException e) {
                throw new TemplateException("Failed to print error message. Cause: " + e, env);
            }
        }
    }

}
