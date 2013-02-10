package ru.runa.service.wf.impl;

import ru.runa.wfe.WfException;

/**
 * Means that there is no 'ReceiveMessage' node handler yet, send JMS message
 * back to the queue.
 * 
 * @author dofs
 * @since 4.0
 */
public class MessagePostponedException extends WfException {
	private static final long serialVersionUID = 1L;

	public MessagePostponedException(String message) {
		super(message);
	}

}
