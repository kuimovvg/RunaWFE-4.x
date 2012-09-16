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
package ru.runa.af.authenticaion;

import java.net.UnknownHostException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;
import ru.runa.af.Actor;

/**
 * Provides NTLM authentication support
 * 
 * Created on 07.11.2005
 */
public class NTLMLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new NTLMCallback();
        callbackHandler.handle(callbacks);
        NtlmPasswordAuthentication ntlmPasswordAuthentication = ((NTLMCallback) callbacks[0]).getNtlmPasswordAuthentication();
        if (ntlmPasswordAuthentication == null) {
            throw new LoginException("No NtlmPasswordAuthentication information provided.");
        }
        String actorName = getActorName(ntlmPasswordAuthentication);
        return executorDAO.getActorCaseInsensitive(actorName);
    }

    private String getActorName(NtlmPasswordAuthentication ntlmPasswordAuthentication) throws UnknownHostException, SmbException {
        String domainController = ADResources.getDomainName();
        UniAddress dc = UniAddress.getByName(domainController, true);
        SmbSession.logon(dc, ntlmPasswordAuthentication);
        return ntlmPasswordAuthentication.getUsername();
    }

}
