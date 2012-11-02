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
package ru.runa.wfe.commons.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Created on 25.07.2005
 * 
 */
public class LoggingErrorHandler implements ErrorHandler {
    private final Log log;

    public LoggingErrorHandler(Class<?> loggingClass) {
        log = LogFactory.getLog(loggingClass);
    }

    public void error(SAXParseException exception) {
        log.debug(exception);
    }

    public void fatalError(SAXParseException exception) {
        log.debug(exception);
    }

    public void warning(SAXParseException exception) {
        log.debug(exception);
    }
}
