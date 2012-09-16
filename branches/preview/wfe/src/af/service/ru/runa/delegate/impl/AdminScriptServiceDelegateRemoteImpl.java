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
package ru.runa.delegate.impl;

import ru.runa.af.AuthenticationException;
import ru.runa.wf.WfeScriptException;
import ru.runa.wf.service.AdminScriptService;

/**
 * Created on 23.09.2005
 */
public class AdminScriptServiceDelegateRemoteImpl extends EJB3Delegate implements AdminScriptService {
    
    @Override
    protected String getBeanName() {
        return "AdminScriptServiceBean";
    }

    private AdminScriptService getWfeScriptService() {
        setRemote(true);
        return getService();
    }

    @Override
    public void run(String login, String password, byte[] configData, byte[][] processFiles) throws WfeScriptException, AuthenticationException {
        getWfeScriptService().run(login, password, configData, processFiles);
    }

    @Override
    protected String getInitialContextFactory() {
        return WfeScriptDelegateResources.getDelegateRemoteInitialContextFactory();
    }

    @Override
    protected String getUrlPkgPrefixes() {
        return WfeScriptDelegateResources.getDelegateRemoteUrlPkgPrefixes();
    }

    @Override
    protected String getProviderUrl() {
        return WfeScriptDelegateResources.getDelegateRemoteProviderUrl();
    }
}
