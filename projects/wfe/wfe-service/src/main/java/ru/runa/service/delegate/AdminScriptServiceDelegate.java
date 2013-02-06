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
package ru.runa.service.delegate;

import ru.runa.service.wf.AdminScriptService;
import ru.runa.wfe.user.User;

/**
 * Created on 23.09.2005
 */
public class AdminScriptServiceDelegate extends EJB3Delegate implements AdminScriptService {

    public AdminScriptServiceDelegate() {
        super("AdminScriptServiceBean", AdminScriptService.class);
    }

    private AdminScriptService getWfeScriptService() {
        return getService();
    }

    @Override
    public void run(User user, byte[] configData, byte[][] processFiles) {
        getWfeScriptService().run(user, configData, processFiles);
    }

}
