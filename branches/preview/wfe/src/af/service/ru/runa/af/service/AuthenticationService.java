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
package ru.runa.af.service;

import javax.security.auth.Subject;

import jcifs.smb.NtlmPasswordAuthentication;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;

/**
 * Created on 20.07.2004
 */
public interface AuthenticationService {

    public Subject authenticate() throws AuthenticationException;

    public Subject authenticate(NtlmPasswordAuthentication authentication) throws AuthenticationException;

    public Subject authenticate(byte[] token) throws AuthenticationException;

    public Subject authenticate(String name, String password) throws AuthenticationException;

    public Actor getActor(Subject subject) throws AuthenticationException;
}
