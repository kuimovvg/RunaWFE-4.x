package ru.runa.wfe.commons.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONAware;

import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;

public abstract class JsonAjaxCommand implements AjaxCommand {

    @Override
    public void execute(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONAware json = execute(user, request);
        response.getOutputStream().write(json.toString().getBytes(Charsets.UTF_8));
    }

    protected abstract JSONAware execute(User user, HttpServletRequest request) throws Exception;

}
