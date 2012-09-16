package ru.runa.wf.logic;

import javax.security.auth.Subject;

import org.w3c.dom.Element;

public interface CustomWfeScriptJob {

	public void execute(Subject subject, Element element) throws Exception;
	
}
