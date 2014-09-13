package ru.runa.wfe.user;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.google.common.base.Objects;

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
    public static final String GROUP_PREFIX = "TmpGroup_";

    public static TemporaryGroup create(String nameSuffix, String description) {
        TemporaryGroup temporaryGroup = new TemporaryGroup();
        temporaryGroup.setCreateDate(new Date());
        temporaryGroup.setName(GROUP_PREFIX + nameSuffix);
        temporaryGroup.setDescription(description);
        return temporaryGroup;
    }

    public static TemporaryGroup create(Long processId, String swimlaneName) {
        String nameSuffix = processId + "_" + swimlaneName;
        String description = processId.toString();
        return create(nameSuffix, description);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", getId()).add("name", getName()).add("description", getDescription()).toString();
    }

}
