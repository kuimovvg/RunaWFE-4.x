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
package ru.runa.wfe.user;

import java.io.Serializable;
import java.security.Principal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * This class implements the <code>Principal</code> interface and represents a
 * logged actor.
 * 
 * <p>
 * Principals such as this may be associated with a particular
 * <code>Subject</code> to augment that <code>Subject</code> with an additional
 * identity. Refer to the <code>Subject</code> class for more information on how
 * to achieve this. Authorization decisions can then be based upon the
 * Principals associated with a <code>Subject</code>. Created on 16.07.2004
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActorPrincipal", namespace = "http://runa.ru/workflow/webservices")
public class User implements Principal, Serializable {
    private static final long serialVersionUID = 43549879345L;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private byte[] securedKey;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private Actor actor;

    // This is need for web services
    protected User() {
    }

    /**
     * @param actor
     *            logged actor
     * @param actor
     *            secured key
     */
    public User(Actor actor, byte[] securedKey) {
        this.actor = actor;
        this.securedKey = securedKey;
    }

    @Override
    public String getName() {
        return actor.getName();
    }

    public Actor getActor() {
        return actor;
    }

    public byte[] getSecuredKey() {
        return securedKey;
    }

}
