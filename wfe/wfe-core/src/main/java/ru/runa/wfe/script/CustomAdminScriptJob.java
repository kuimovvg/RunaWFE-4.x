package ru.runa.wfe.script;

import javax.security.auth.Subject;

import org.w3c.dom.Element;

public interface CustomAdminScriptJob {

	public void execute(Subject subject, Element element) throws Exception;
	
}
