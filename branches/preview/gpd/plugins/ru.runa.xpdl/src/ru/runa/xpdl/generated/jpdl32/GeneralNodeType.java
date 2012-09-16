package ru.runa.xpdl.generated.jpdl32;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mika951
 * Date: 12.05.12
 * Time: 11:58
 * To change this template use File | Settings | File Templates.
 */
public interface GeneralNodeType
{
    /**
         * Gets the value of the name property.
         *
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getName();

        /**
         * Sets the value of the name property.
         *
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setName(java.lang.String value);

        void addTransition ( Transition transition);

    void setDescription(String s);

    List getSubObjectList();
}
