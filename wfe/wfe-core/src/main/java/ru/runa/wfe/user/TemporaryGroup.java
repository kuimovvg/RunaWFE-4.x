package ru.runa.wfe.user;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Used for dynamic assignment multiple executors in swimlanes.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "T")
public class TemporaryGroup extends Group {
    private static final long serialVersionUID = 1L;
    /**
     * Prefix for temporary group name.
     */
    public static final String GROUP_PREFIX = "__TmpGroup_";

    public TemporaryGroup() {
    }

    public static TemporaryGroup create(String nameSuffix) {
        TemporaryGroup temporaryGroup = new TemporaryGroup();
        temporaryGroup.setName(GROUP_PREFIX + nameSuffix);
        return temporaryGroup;
    }

}
