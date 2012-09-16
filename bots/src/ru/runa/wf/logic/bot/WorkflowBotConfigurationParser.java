/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.runa.af.logic.bot.BotInvoker;
import ru.runa.commons.xml.PathEntityResolver;
import ru.runa.commons.xml.XMLHelper;

/**
 * Created on 04.03.2005
 */
public class WorkflowBotConfigurationParser {
    private static final String XSD_CONF_PATH = "/bot/botstation.xsd";

    private static final String XML_CONF_PATH = "/bot/botstation.xml";

    private static final PathEntityResolver CONF_PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_CONF_PATH);

    public WorkflowBotConfigurationParser() throws WorkflowBotConfigurationParserException {
        readLoginAndPassword();
    }

    public String login = null;
    public String password = null;
    public int threadPoolSize = 1;

    public void readLoginAndPassword() throws WorkflowBotConfigurationParserException {
        try {
            String confPath = BotInvoker.class.getResource(XML_CONF_PATH).toString();
            Document document = XMLHelper.getDocument(confPath, CONF_PATH_ENTITY_RESOLVER);
            login = document.getDocumentElement().getAttribute("login");
            password = document.getDocumentElement().getAttribute("password");
            NodeList poolSize = document.getDocumentElement().getElementsByTagName("thread-pool-size");
            if (poolSize.getLength() > 0) {
                threadPoolSize = Integer.parseInt(((Element) poolSize.item(0)).getTextContent());
                if (threadPoolSize <= 0) {
                    threadPoolSize = 1;
                }
            }
        } catch (SAXException e) {
            throw new WorkflowBotConfigurationParserException(e);
        } catch (IOException e) {
            throw new WorkflowBotConfigurationParserException(e);
        } catch (Exception e) {
            throw new WorkflowBotConfigurationParserException(e);
        }
    }
}
