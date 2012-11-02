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

import java.security.Principal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p> This class implements the <code>Principal</code> interface
 * and represents a Actor.
 *
 * <p> Principals such as this <code>ActorPrincipal</code>
 * may be associated with a particular <code>Subject</code>
 * to augment that <code>Subject</code> with an additional
 * identity.  Refer to the <code>Subject</code> class for more information
 * on how to achieve this.  Authorization decisions can then be based upon 
 * the Principals associated with a <code>Subject</code>.
 * Created on 16.07.2004
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActorPrincipal", namespace = "http://runa.ru/workflow/webservices")
public class ActorPrincipal implements Principal, java.io.Serializable {

    private static final long serialVersionUID = 4574419555576578526L;
    /**
     * Actor name
     */
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private Actor actor;
    @XmlElement(namespace = "http://runa.ru/workflow/webservices")
    private byte[] key;

    // This is need for web services
    @SuppressWarnings("unused")
    private ActorPrincipal() {
    }

    /**
     * Create a ActorPrincipal with a given name.
     * @param actor an Actor.
     * @exception NullPointerException if the <code>name</code> *is <code>null</code>.
     */
    public ActorPrincipal(Actor actor, byte[] key) {
        if (actor == null) {
            throw new NullPointerException("Name must be specified.");
        }
        this.actor = actor;
        this.key = key;
    }

    /**
     * Return the Actor name for this <code>ActorPrincipal</code>.
     * @return the Actor name for this <code>ActorPrincipal</code>
     */
    public Actor getActor() {
        return actor;
    }

    public String getName() {
        return actor.getName();
    }

    public byte[] getKey() {
        return key;
    }

    /**
     * Return a string representation of this <code>ActorPrincipal</code>.
     * @return a string representation of this <code>ActorPrincipal</code>.
     */
    public String toString() {
        return ("ActorPrincipal:  " + actor);
    }

    /**
     * Compares the specified Object with this <code>ActorPrincipal</code>
     * for equality.  Returns true if the given object is also a
     * <code>ActorPrincipal</code> and the two ActorPrincipals
     * have the same name.
     * @param o Object to be compared for equality with this
     *<code>ActorPrincipal</code>.
     * @return true if the specified Object is equal equal to this
     *<code>ActorPrincipal</code>.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof ActorPrincipal)) {
            return false;
        }
        ActorPrincipal that = (ActorPrincipal) o;

        if (getActor().equals(that.getActor())) {
            return true;
        }
        return false;
    }

    /**
     * Return a hash code for this <code>ActorPrincipal</code>.
     * @return a hash code for this <code>ActorPrincipal</code>.
     */
    public int hashCode() {
        return actor.hashCode();
    }
}
