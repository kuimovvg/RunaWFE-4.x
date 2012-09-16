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
package ru.runa.af;

import java.util.Collection;

import javax.security.auth.Subject;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.Profile;

import com.google.common.base.Strings;

/**
 * Created on 08.12.2005
 * 
 */
public final class ArgumentsCommons {

    private ArgumentsCommons() {
    }

    /**
     * Check if {@linkplain Object} is not null; throws exception otherwise.
     * @param object Object to check.
     * @throws IllegalArgumentException if object==null.
     */
    public static void checkNotNull(Object object) throws IllegalArgumentException {
        internalNullCheck(object, "Parameter");
    }

    /**
     * Check if {@linkplain Object} is not null; throws exception otherwise.
     * @param object Object to check.
     * @param objectName Checking object name, which used in exception message.
     * @throws IllegalArgumentException if object==null.
     */
    public static void checkNotNull(Object object, String objectName) throws IllegalArgumentException {
        internalNullCheck(object, objectName);
    }

    /**
     * Check if object's array is not null and all objects in array is also not null; throws exception otherwise. 
     * @param object Object's array to check.
     * @param objectName Checking object name, which used in exception message.
     * @throws IllegalArgumentException if objects == null.
     */
    public static void checkNotNull(Object[] objects) throws IllegalArgumentException {
        internalNullCheck(objects, "Parameter");
    }

    /**
     * Check if {@linkplain Subject} is not null; throws exception otherwise.
     * @param subject Subject to check.
     * @throws IllegalArgumentException if subject==null.
     */
    public static void checkNotNull(Subject subject) throws IllegalArgumentException {
        internalNullCheck(subject, "Subject");
    }

    /**
     * Check if {@linkplain Permission} is not null; throws exception otherwise.
     * @param permission Permission to check.
     * @throws IllegalArgumentException if permission==null.
     */
    public static void checkNotNull(Permission permission) throws IllegalArgumentException {
        internalNullCheck(permission, "Permission");
    }

    /**
     * Check if {@linkplain Permission[]} is not null and all permissions is also not null; throws exception otherwise.
     * @param permission Permission to check.
     * @throws IllegalArgumentException if permission==null.
     */
    public static void checkNotNull(Permission[] permission) throws IllegalArgumentException {
        internalNullCheck(permission, "Permission");
    }

    /**
     * Check if {@linkplain Permission[]} is not null and all permissions is also not null; throws exception otherwise.
     * @param permission Permission to check.
     * @throws IllegalArgumentException if permission==null.
     */
    public static void checkNotNull(Permission[][] permission) throws IllegalArgumentException {
        internalNullCheck(permission, "Permission");
    }

    /**
     * Check if {@linkplain SecuredObject} is not null; throws exception otherwise.
     * @param securedObject SecuredObject to check.
     * @throws IllegalArgumentException if securedObject==null.
     */
    public static void checkNotNull(SecuredObject securedObject) throws IllegalArgumentException {
        internalNullCheck(securedObject, "Secured object");
    }

    /**
     * Check if {@linkplain SecuredObject[]} is not null and all SecuredObject's is also not null; throws exception otherwise.
     * @param securedObjects SecuredObject to check.
     * @throws IllegalArgumentException if securedObjects==null.
     */
    public static void checkNotNull(SecuredObject[] securedObjects) throws IllegalArgumentException {
        internalNullCheck(securedObjects, "Secured object");
    }

    /**
     * Check if {@linkplain Executor} is not null; throws exception otherwise.
     * @param executor Executor to check.
     * @throws IllegalArgumentException if executor==null.
     */
    public static <T extends Executor> void checkNotNull(T executor) throws IllegalArgumentException {
        internalNullCheck(executor, "Executor");
    }

    /**
     * Check if {@linkplain Executor[]} is not null and all Executor is also not null; throws exception otherwise.
     * @param executors Executors to check.
     * @throws IllegalArgumentException if executors==null.
     */
    public static <T extends Executor> void checkNotNull(T[] executors) {
        internalNullCheck(executors, "Executor");
    }

    /**
     * Check if {@linkplain Executor[]} is not null and all Executor is also not null; throws exception otherwise.
     * @param executors Executors to check.
     * @throws IllegalArgumentException if executors==null.
     */
    public static <T extends Executor> void checkNotNull(Collection<T> executors) {
        internalNullCheck(executors, "Executor");
    }

    /**
     * Check if {@linkplain Identifiable} is not null; throws exception otherwise.
     * @param identifiable Identifiable to check.
     * @throws IllegalArgumentException if identifiable==null.
     */
    public static void checkNotNull(Identifiable identifiable) throws IllegalArgumentException {
        internalNullCheck(identifiable, "Identifiable");
    }

    /**
     * Check if {@linkplain Identifiable[]} is not null and all Identifiable is also not null; throws exception otherwise.
     * @param identifiables Identifiable to check.
     * @throws IllegalArgumentException if identifiables==null.
     */
    public static void checkNotNull(Identifiable[] identifiables) throws IllegalArgumentException {
        internalNullCheck(identifiables, "Identifiable");
    }

    /**
     * Check if {@linkplain Substitution} is not null; throws exception otherwise.
     * @param substitution Substitution to check.
     * @throws IllegalArgumentException if substitution==null.
     */
    public static void checkNotNull(Substitution substitution) throws IllegalArgumentException {
        internalNullCheck(substitution, "Substitution");
    }

    /**
     * Check if {@linkplain Substitution[]} is not null and all Substitution is also not null; throws exception otherwise.
     * @param substitutions Substitution to check.
     * @throws IllegalArgumentException if substitutions==null.
     */
    public static void checkNotNull(Substitution[] substitutions) throws IllegalArgumentException {
        internalNullCheck(substitutions, "Substitution");
    }

    /**
     * Check if {@linkplain SubstitutionCriteria} is not null; throws exception otherwise.
     * @param criteria SubstitutionCriteria to check.
     * @throws IllegalArgumentException if criteria==null.
     */
    public static <T extends SubstitutionCriteria> void checkNotNull(T criteria) throws IllegalArgumentException {
        internalNullCheck(criteria, "Substitution criteria");
    }

    /**
     * Check if {@linkplain SubstitutionCriteria[]} is not null and all SubstitutionCriteria is also not null; throws exception otherwise.
     * @param criterias SubstitutionCriteria to check.
     * @throws IllegalArgumentException if criterias==null.
     */
    public static <T extends SubstitutionCriteria> void checkNotNull(T[] criterias) throws IllegalArgumentException {
        internalNullCheck(criterias, "Substitution criteria");
    }

    /**
     * Check if {@linkplain BatchPresentation} is not null; throws exception otherwise.
     * @param batchPresentation BatchPresentation to check.
     * @throws IllegalArgumentException if batchPresentation==null.
     */
    public static void checkNotNull(BatchPresentation batchPresentation) throws IllegalArgumentException {
        internalNullCheck(batchPresentation, "Batch presentation");
    }

    /**
     * Check if {@linkplain Bot} is not null; throws exception otherwise.
     * @param bot Bot to check.
     * @throws IllegalArgumentException if bot==null.
     */
    public static void checkNotNull(Bot bot) throws IllegalArgumentException {
        internalNullCheck(bot, "Bot");
    }

    /**
     * Check if {@linkplain BotTask} is not null; throws exception otherwise.
     * @param botTask BotTask to check.
     * @throws IllegalArgumentException if botTask==null.
     */
    public static void checkNotNull(BotTask botTask) throws IllegalArgumentException {
        internalNullCheck(botTask, "Bot task");
    }

    /**
     * Check if {@linkplain BotStation} is not null; throws exception otherwise.
     * @param botStation BotStation to check.
     * @throws IllegalArgumentException if botStation==null.
     */
    public static void checkNotNull(BotStation botStation) throws IllegalArgumentException {
        internalNullCheck(botStation, "Bot station");
    }

    /**
     * Check if {@linkplain Profile} is not null; throws exception otherwise.
     * @param profile Profile to check.
     * @throws IllegalArgumentException if profile==null.
     */
    public static void checkNotNull(Profile profile) throws IllegalArgumentException {
        internalNullCheck(profile, "Profile");
    }

    /**
     * Check if identities array is not null; throws exception otherwise.
     * @param identifiers Identities array to check.
     * @throws IllegalArgumentException if identifiers==null.
     */
    public static void checkNotNull(long[] identifiers) {
        internalNullCheck((Object) identifiers, "Identifiers array");
    }

    /**
     * Check if string is not null and not empty; throws exception otherwise.
     * @param string String to check.
     * @param objectName Checked parameter name, used in exception message. 
     * @throws IllegalArgumentException if string is null or empty.
     */
    public static void checkNotEmpty(String string, String objectName) throws IllegalArgumentException {
        if (Strings.isNullOrEmpty(string)) {
            throw new IllegalArgumentException(objectName + " must be specified");
        }
    }

    /**
     * throws {@link java.lang.IllegalArgumentException} if object1.length!=object2.length
     * @param objects1
     * @param objects2
     * @param message
     */
    public static void checkArraysLengthEQ(Object[] objects1, Object[] objects2, String message) {
        if (objects1.length != objects2.length) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @param objects
     * @param length
     * @param errorMessage
     * @throws IllegalArgumentException if !(objects.length<length) 
     */
    public static void checkArrayLengthGT(Object[] objects, int length, String errorMessage) throws IllegalArgumentException {
        if (!(objects.length > length)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * @param objects
     * @param length
     * @param errorMessage
     * @throws IllegalArgumentException if !(objects.length<length) 
     */
    public static void checkArrayLengthGT(Object[] objects, int length) throws IllegalArgumentException {
        checkArrayLengthGT(objects, length, "must be greater than" + length);
    }

    /**
     * @param objects
     * @param length
     * @param errorMessage
     * @throws IllegalArgumentException if (objects.length!=length) 
     */
    public static void checkArrayLengthEQ(Object[] objects, int length) throws IllegalArgumentException {
        checkArrayLengthEQ(objects, length, "must be equal to" + length);
    }

    /**
     * @param objects
     * @param length
     * @param errorMessage
     * @throws IllegalArgumentException if (objects.length!=length) 
     */
    public static void checkArrayLengthEQ(Object[] objects, int length, String errorMessage) throws IllegalArgumentException {
        if (!(objects.length == length)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void checkType(Object object, Class<?> expectedClass, String errorMessages) throws IllegalArgumentException {
        if (!(object.getClass().equals(expectedClass))) {
            throw new IllegalArgumentException(errorMessages);
        }
    }

    public static void checkType(Object object, Class<?> expectedClass) throws IllegalArgumentException {
        checkType(object, expectedClass, "given object is not instance of " + expectedClass);
    }

    /**
     * Check if object is not null; throws exception otherwise. 
     * @param object Object to check.
     * @param objectName Checking object name, which used in exception message.
     * @throws IllegalArgumentException if object == null.
     */
    private static <T> void internalNullCheck(T object, String objectName) throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException(objectName + " must be specified, but got null");
        }
    }

    /**
     * Check if object is not null; throws exception otherwise. 
     * @param object Object to check.
     * @param objectName Checking object name, which used in exception message.
     * @throws IllegalArgumentException if object == null.
     */
    private static <T> void internalArrayNullCheck(T object, String objectName) throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException(objectName + " must be specified inside collection, but got null");
        }
    }

    /**
     * Check if object's array is not null and all objects in array is also not null; throws exception otherwise. 
     * @param object Object's array to check.
     * @param objectName Checking object name, which used in exception message.
     * @throws IllegalArgumentException if objects == null.
     */
    private static <T> void internalNullCheck(T[] objects, String objectName) throws IllegalArgumentException {
        if (objects == null) {
            throw new IllegalArgumentException(objectName + " array must be specified, but got null");
        }
        for (T obj : objects) {
            internalArrayNullCheck(obj, objectName);
        }
    }

    /**
     * Check if object's array is not null and all objects in array is also not null; throws exception otherwise. 
     * @param object Object's array to check.
     * @param objectName Checking object name, which used in exception message.
     * @throws IllegalArgumentException if objects == null.
     */
    private static <T> void internalNullCheck(Collection<T> objects, String objectName) throws IllegalArgumentException {
        if (objects == null) {
            throw new IllegalArgumentException(objectName + " collection must be specified, but got null");
        }
        for (T obj : objects) {
            internalArrayNullCheck(obj, objectName);
        }
    }
}
