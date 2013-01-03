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
package ru.runa.wfe.security.auth;

import java.security.Key;
import java.util.Arrays;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPrincipal;

import com.google.common.base.Preconditions;

/**
 * Helps to extract {@link Actor} from Subject principals at logic layer.
 */
public class SubjectPrincipalsHelper {
    private static final Log log = LogFactory.getLog(SubjectPrincipalsHelper.class);

    private static Key securedKey = null;
    private static String encryptionType = "DES";

    static {
        try {
            securedKey = KeyGenerator.getInstance(encryptionType).generateKey();
        } catch (Exception e) {
            log.error("Unable to get instance of KeyGenerator", e);
        }
    }

    private SubjectPrincipalsHelper() {
    }

    private static byte[] getActorKey(Actor actor) {
        return (actor.getCode() + actor.getName()).getBytes();
    }

    public static byte[] encodeActor(Actor actor) {
        try {
            Cipher cipher = Cipher.getInstance(encryptionType);
            cipher.init(Cipher.ENCRYPT_MODE, securedKey);
            return cipher.doFinal(getActorKey(actor));
        } catch (Exception e) {
            log.warn("Can't create subject cipher");
            return null;
        }
    }

    private static void validateActorPrincipal(ActorPrincipal actorPrincipal) throws AuthenticationException {
        try {
            Cipher cipher = Cipher.getInstance(encryptionType);
            cipher.init(Cipher.DECRYPT_MODE, securedKey);
            if (!Arrays.equals(getActorKey(actorPrincipal.getActor()), cipher.doFinal(actorPrincipal.getKey()))) {
                throw new AuthenticationException("Incorrect actor principal at subject received");
            }
        } catch (Exception e) {
            log.error("Error in subject decryption", e);
            throw new AuthenticationException("Error in subject decryption");
        }
    }

    public static Actor getActor(Subject subject) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        Set<ActorPrincipal> principals = subject.getPrincipals(ActorPrincipal.class);
        for (ActorPrincipal principal : principals) {
            if (principal != null) {
                validateActorPrincipal(principal);
                return (principal).getActor();
            }
        }
        throw new AuthenticationException("Subject does not contain actor principal");
    }

    // private static Actor getActorById(long id) throws AuthenticationException
    // {
    // AFDaoHolder daoHolder = new AFDaoHolder();
    // try {
    // return daoHolder.getExecutorDAO().getActor(id);
    // } catch (ExecutorOutOfDateException e) {
    // throw new AuthenticationException(e);
    // } finally {
    // daoHolder.close();
    // }
    // }

}
