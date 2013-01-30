package ru.runa.common.web.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.Action;

import ru.runa.common.web.Commons;
import ru.runa.wfe.user.User;

public abstract class ActionBase extends Action {

    protected User getLoggedUser(HttpServletRequest request) {
        return Commons.getUser(request.getSession());
    }
}
