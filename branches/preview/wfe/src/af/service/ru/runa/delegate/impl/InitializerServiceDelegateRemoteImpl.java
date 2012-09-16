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

import ru.runa.af.service.InitializerService;


/**
 * Created on 24.06.2005
 */
public class InitializerServiceDelegateRemoteImpl extends EJB3Delegate implements InitializerService {

    @Override
    protected String getBeanName() {
        return "InitializerServiceBean";
    }

    private InitializerService getInitializerService() {
        setRemote(true);
        return (InitializerService) getService();
    }

    public static void main(String[] args) throws Exception {
        InitializerServiceDelegateRemoteImpl delegate = new InitializerServiceDelegateRemoteImpl();
        delegate.init(true, false);
    }

    @Override
    public void init(boolean force, boolean isArchiveDBinit) {
        getInitializerService().init(force, isArchiveDBinit);
    }

}
