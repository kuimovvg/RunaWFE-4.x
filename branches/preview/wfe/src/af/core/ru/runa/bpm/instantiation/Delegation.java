/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.instantiation;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

import org.dom4j.CDATA;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.commons.ClassLoaderUtil;
import ru.runa.commons.EqualsUtil;
import ru.runa.wf.ProcessDefinitionXMLFormatException;

public class Delegation implements Parsable, Serializable {
    private static final long serialVersionUID = 1L;

    private String className;
    private String configuration;
    private transient Object instance;

    public Delegation() {
    }

    public Delegation(String className) {
        this.className = className;
    }

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element delegateElement, JpdlXmlReader jpdlReader) {
        className = delegateElement.attributeValue("class");
        if (className == null) {
            throw new ProcessDefinitionXMLFormatException("no class specified in " + delegateElement.asXML());
        }
        if (delegateElement.hasContent()) {
            try {
                List<Node> nodes = delegateElement.content();
                if (nodes.size() == 1 && nodes.get(0) instanceof CDATA) {
                    CDATA cdata = (CDATA) nodes.get(0);
                    configuration = cdata.getText();
                    configuration = configuration.trim();
                } else {
                    StringWriter stringWriter = new StringWriter();
                    // when parsing, it could be to store the config in the database, so we want to make the configuration compact
                    XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createCompactFormat());
                    for (Node node : nodes) {
                        xmlWriter.write(node);
                    }
                    xmlWriter.flush();
                    configuration = stringWriter.toString();
                }
            } catch (IOException e) {
                throw new ProcessDefinitionXMLFormatException("io problem while parsing the configuration of " + delegateElement.asXML());
            }
        } else {
            configuration = "";
        }
    }

    public Object getInstance() {
        if (instance == null) {
            instance = instantiate();
        }
        return instance;
    }

    private Object instantiate() {
        Class<?> clazz = ClassLoaderUtil.loadClass(className);
        Instantiator instantiator = new ConfigurationPropertyInstantiator();
        Object object = instantiator.instantiate(clazz, configuration);
        ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(object);
        return object;
    }

    // equals ///////////////////////////////////////////////////////////////////
    // hack to support comparing hibernate proxies against the real objects
    // since this always falls back to ==, we don't need to overwrite the hashcode
    public boolean equals(Object o) {
        return EqualsUtil.equals(this, o);
    }

    // getters and setters //////////////////////////////////////////////////////

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

}
