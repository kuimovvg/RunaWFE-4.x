<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:web="http://java.sun.com/xml/ns/j2ee">
    <xsl:output method="text" indent="yes"/>

    <xsl:template match="web:web-app">
package ru.runa.common.web.portlet.impl;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.portlet.PortletExceptionHandler;

public class DefaultExceptionHandler implements PortletExceptionHandler
{
	public boolean processError(Exception exception, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
        <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
		return false;
	}

	private static ActionErrors getActionErrors(HttpServletRequest request) {
		ActionErrors messages = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
		if (messages == null) {
			messages = new ActionErrors();
			request.setAttribute(Globals.ERROR_KEY, messages);
		}
		return messages;
	}

}
    </xsl:template>

    <xsl:template match="web:web-app/web:error-page[web:exception-type]">
		if(exception instanceof <xsl:value-of select="web:exception-type"/>){
			ActionExceptionHelper.addException(getActionErrors(request), exception);
			servletContext.getRequestDispatcher(&quot;<xsl:value-of select="web:location"/>&quot;).forward(request, response);
			return true;
		}
    </xsl:template>

    <xsl:template match="text()|@*|*|processing-instruction()|comment()"> 
        <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
    </xsl:template> 

</xsl:stylesheet>
