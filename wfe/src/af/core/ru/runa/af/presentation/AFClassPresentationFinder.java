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
package ru.runa.af.presentation;

import java.util.HashMap;
import java.util.Map;

import ru.runa.InternalApplicationException;

/**
 * Created on 22.10.2005
 *
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class AFClassPresentationFinder implements ClassPresentationFinder {
    private static final Map<Integer, ClassPresentation> classPrecentationMap = new HashMap<Integer, ClassPresentation>();
    static {
        classPrecentationMap.put(new Integer(ExecutorClassPresentation.getInstance().getPresentationClass().hashCode()), ExecutorClassPresentation
                .getInstance());
        classPrecentationMap.put(new Integer(ExecutorClassPresentation.class.hashCode()), ExecutorClassPresentation.getInstance());
        classPrecentationMap.put(new Integer(RelationClassPresentation.getInstance().getPresentationClass().hashCode()), RelationClassPresentation
                .getInstance());
        classPrecentationMap.put(new Integer(RelationClassPresentation.class.hashCode()), RelationClassPresentation.getInstance());
        classPrecentationMap.put(new Integer(RelationGroupClassPresentation.getInstance().getPresentationClass().hashCode()),
                RelationGroupClassPresentation.getInstance());
        classPrecentationMap.put(new Integer(RelationGroupClassPresentation.class.hashCode()), RelationGroupClassPresentation.getInstance());
    }

    public ClassPresentation getClassPresentationById(int id) {
        ClassPresentation result = classPrecentationMap.get(new Integer(id));
        if (result == null) {
            throw new InternalApplicationException("Failed to found ClassPresentation with id " + id + " not found");
        }
        return result;
    }

    public int getClassPresentationId(ClassPresentation classPresentation) {
        return classPresentation.getPresentationClass().hashCode();
    }
}
