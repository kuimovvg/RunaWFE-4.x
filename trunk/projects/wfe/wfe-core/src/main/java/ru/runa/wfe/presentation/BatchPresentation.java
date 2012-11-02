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
package ru.runa.wfe.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import ru.runa.wfe.commons.ArraysCommons;
import ru.runa.wfe.commons.OracleCommons;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Presentation of objects collection, contains sorting rules, filter rules and so on.
 */
@Entity
@Table(name = "BATCH_PRESENTATION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class BatchPresentation implements Cloneable, Serializable {
    private static final long serialVersionUID = 6631653373163613071L;
    private static final Log log = LogFactory.getLog(BatchPresentation.class);

    /**
     * Identity of {@link BatchPresentation}.
     */
    private Long id;

    /**
     * Object version (need by hibernate for correct updating).
     */
    private Long version;

    /**
     * {@link ClassPresentation}, refers by this {@link BatchPresentation}.
     */
    private Integer classPresentationId;

    /**
     * Presentation group identity. Such as tasksList, processLists and so on. Each group refers to some page in web interface.
     */
    private String category;

    /**
     * Presentation name. Displays in web interface.
     */
    private String name;

    /**
     * Is this batchPresentation active inside category.
     */
    private boolean active;

    /**
     * Page size for paged {@link BatchPresentation}.
     */
    int rangeSize = 10;

    /**
     * Page number for paged {@link BatchPresentation}.
     */
    private int pageNumber = 1;

    private transient byte[] fieldsData;
    private Fields fields;

    /**
     * Holds identifiers for expanded groups.
     */
    final List<String> expandedBlockList = Lists.newArrayList();

    /**
     * Helper to hold fields set (such us fields to display, sort and so on).
     */
    private transient Store storage;

    /**
     * For hibernate
     */
    protected BatchPresentation() {
    }

    /**
     * @param name
     *            of presentation
     * @param classPresentation
     *            tag that supports such presentation
     * @param fieldsToSort
     *            fields to sort (allowed multi field sorting)
     * @param fieldsToDisplayIds
     *            fields to display
     * @param isGroupingEnabled
     *            enables grouping
     */
    public BatchPresentation(String batchPresentationName, String batchPresentationId, ClassPresentation classPresentation, int[] fieldsToSortIds,
            boolean[] fieldsToSortModes, int[] fieldsToDisplayIds, Map<Integer, FilterCriteria> fieldsToFilterMap, int[] fieldsToGroupIds) {
        setName(batchPresentationName);
        setCategory(batchPresentationId);
        this.classPresentationId = ClassPresentations.getClassPresentationId(classPresentation);
        fields = new Fields();
        fields.sortIds = fieldsToSortIds;
        fields.sortModes = fieldsToSortModes;
        fields.displayIds = fieldsToDisplayIds;
        fields.filters.putAll(fieldsToFilterMap);
        fields.groupIds = fieldsToGroupIds;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BATCH_PRESENTATION")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION", nullable = false)
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "CLASS_PRESENTATION_ID")
    protected Integer getClassPresentationId() {
        return classPresentationId;
    }

    protected void setClassPresentationId(Integer classPresentationId) {
        this.classPresentationId = classPresentationId;
    }

    @Column(name = "CATEGORY", length = 128, nullable = false)
    public String getCategory() {
        return category;
    }

    protected void setCategory(String tagName) {
        category = tagName;
    }

    @Column(name = "NAME", length = 128, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = OracleCommons.fixNullString(name);
    }

    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Lob
    @Column(name = "FIELDS")
    public byte[] getFieldsData() {
        if (fields == null) {
            return fieldsData;
        }
        return FieldsSerializer.toData(fields);
    }

    public void setFieldsData(byte[] data) {
        try {
            this.fieldsData = data;
            this.fields = FieldsSerializer.fromData(data);
            storage = null;
        } catch (Exception e) {
            log.error("Unable to load batch presentation state", e);
        }
    }

    @Column(name = "RANGE_SIZE")
    public int getRangeSize() {
        return rangeSize;
    }

    public void setRangeSize(int rangeSize) {
        if (this.rangeSize != rangeSize) {
            this.rangeSize = rangeSize;
            pageNumber = 1;
        }
    }

    @Transient
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Transient
    public int[] getFieldsToDisplayIds() {
        return fields.displayIds;
    }

    @Transient
    public int[] getFieldsToSortIds() {
        return fields.sortIds;
    }

    @Transient
    public boolean[] getFieldsToSortModes() {
        return fields.sortModes;
    }

    @Transient
    public int[] getFieldsToGroupIds() {
        return fields.groupIds;
    }

    @Transient
    public Map<Integer, FilterCriteria> getFilteredFields() {
        return fields.filters;
    }

    @Transient
    public List<DynamicField> getDynamicFields() {
        return fields.dynamics;
    }

    public void setFilteredFields(Map<Integer, FilterCriteria> newFilteredFieldsMap) {
        if (fields.setFilteredFields(newFilteredFieldsMap)) {
            setPageNumber(1);
        }
        storage = null;
    }

    public void setGroupBlockStatus(String key, boolean isExpanded) {
        if (isExpanded) {
            expandedBlockList.add(key);
        } else {
            expandedBlockList.remove(key);
        }
    }

    public boolean isGroupBlockExpanded(String key) {
        return expandedBlockList.contains(key);
    }

    public void setPredefinedRangeSize(int rangeSize) {
        if (ArraysCommons.contains(BatchPresentationConsts.getAllowedViewSizes(), rangeSize)) {
            setRangeSize(rangeSize);
        }
    }

    @Transient
    public boolean isDefault() {
        return BatchPresentationConsts.DEFAULT_NAME.equals(getName());
    }

    public boolean isSortingField(int fieldIndex) {
        if (!getAllFields()[fieldIndex].isSortable) {
            return false;
        }
        return ArraysCommons.findPosition(fields.sortIds, fieldIndex) >= 0;
    }

    public int getSortingFieldPosition(int fieldIndex) {
        return ArraysCommons.findPosition(fields.sortIds, fieldIndex);
    }

    @Transient
    public FieldDescriptor[] getAllFields() {
        return getStore().allFields;
    }

    @Transient
    public FieldDescriptor[] getDisplayFields() {
        return getStore().displayFields;
    }

    @Transient
    public FieldDescriptor[] getSortedFields() {
        return getStore().sortedFields;
    }

    @Transient
    public FieldDescriptor[] getGrouppedFields() {
        return getStore().groupedFields;
    }

    @Transient
    public FieldDescriptor[] getHiddenFields() {
        return getStore().hiddenFields;
    }

    public void setFieldsToDisplayIds(int[] fieldsToDisplayIds) {
        fields.displayIds = fieldsToDisplayIds;
        storage = null;
    }

    public void setFieldsToSort(int[] fieldsToSortIds, boolean[] sortingModes) {
        fields.setFieldsToSort(fieldsToSortIds, sortingModes, getAllFields());
        storage = null;
    }

    public void setFirstFieldToSort(int newSortFieldId) {
        fields.setFirstFieldToSort(newSortFieldId, getAllFields());
        storage = null;
    }

    public void addDynamicField(long fieldIdx, String fieldValue) {
        fields.addDynamicField(fieldIdx, fieldValue);
        storage = null;
    }

    public void removeDynamicField(long fieldIdx) {
        fields.removeDynamicField(fieldIdx);
        storage = null;
    }

    public boolean isFieldFiltered(int fieldId) {
        if (getAllFields()[fieldId].filterMode == FieldFilterMode.NONE) {
            return false;
        }
        return fields.filters.containsKey(fieldId);
    }

    public boolean isFieldGroupped(int fieldId) {
        if (!getAllFields()[fieldId].isSortable) {
            return false;
        }
        return ArraysCommons.contains(fields.groupIds, fieldId);
    }

    public FilterCriteria getFieldFilteredCriteria(int fieldId) {
        FilterCriteria filterCriteria = fields.filters.get(fieldId);
        if (filterCriteria == null) {
            String fieldType = getAllFields()[fieldId].fieldType;
            filterCriteria = FilterCriteriaFactory.getFilterCriteria(fieldType);
        }
        return filterCriteria;
    }

    public void setFieldsToGroup(int[] fieldsToGroupIds) {
        fields.setFieldsToGroup(fieldsToGroupIds, getAllFields());
        storage = null;
    }

    @Transient
    public ClassPresentation getClassPresentation() {
        return ClassPresentations.getClassPresentation(classPresentationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getCategory());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BatchPresentation)) {
            return false;
        }
        BatchPresentation presentation = (BatchPresentation) obj;
        return Objects.equal(getName(), presentation.getName()) && Objects.equal(getCategory(), presentation.getCategory());
    }

    public boolean strongEquals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BatchPresentation)) {
            return false;
        }
        BatchPresentation presentation = (BatchPresentation) obj;
        boolean result = true;
        result &= Objects.equal(getName(), presentation.getName());
        result &= Objects.equal(getCategory(), presentation.getCategory());
        result &= Objects.equal(classPresentationId, presentation.classPresentationId);
        result &= Objects.equal(getFilteredFields(), presentation.getFilteredFields());
        result &= Objects.equal(fields, presentation.fields);
        return result;
    }

    public boolean fieldEquals(BatchPresentation other) {
        return Objects.equal(classPresentationId, other.classPresentationId) && fields.equals(other.fields);
    }

    @Override
    public BatchPresentation clone() {
        BatchPresentation clone = new BatchPresentation();
        clone.category = category;
        clone.name = name;
        clone.classPresentationId = classPresentationId;
        clone.fields = FieldsSerializer.fromData(FieldsSerializer.toData(fields));
        return clone;
    }

    /**
     * Get helper to hold fields set (such us fields to display, sort and so on).
     * 
     * @return Helper to current {@link BatchPresentation}.
     */
    @Transient
    private Store getStore() {
        if (storage == null) {
            storage = new Store(this);
        }
        return storage;
    }

    @Transient
    public boolean isValid() {
        return fields != null;
    }

    protected static class Fields implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Displayed fields indexes in correct order.
         */
        int[] displayIds;

        /**
         * Sorting fields indexes in correct order. Size of array is equals to fieldsToSortModes size.
         */
        int[] sortIds;

        /**
         * Sorting fields modes. Size of array is equals to fieldsToSortIds size.
         */
        boolean[] sortModes;

        /**
         * Grouping fields indexes in correct order.
         */
        int[] groupIds;

        /**
         * {@link Map} from field index to {@link FilterCriteria} for filter.
         */
        final Map<Integer, FilterCriteria> filters = Maps.newHashMap();

        /**
         * Removable fields, created for editable fields (with values, inserted by user).
         */
        final List<DynamicField> dynamics = Lists.newArrayList();

        public boolean setFilteredFields(Map<Integer, FilterCriteria> newFilteredFieldsMap) {
            boolean resetPageNumber = false;
            if (filters.size() == newFilteredFieldsMap.size()) {
                for (Map.Entry<Integer, FilterCriteria> entry : filters.entrySet()) {
                    if (!entry.getValue().equals(newFilteredFieldsMap.get(entry.getKey()))) {
                        resetPageNumber = true;
                        break;
                    }
                }
            } else {
                resetPageNumber = true;
            }
            filters.clear();
            for (Map.Entry<Integer, FilterCriteria> entry : newFilteredFieldsMap.entrySet()) {
                filters.put(entry.getKey(), entry.getValue());
            }
            return resetPageNumber;
        }

        public void setFieldsToSort(int[] fieldsToSortIds, boolean[] sortingModes, FieldDescriptor[] allFields) {
            if (fieldsToSortIds.length != sortingModes.length) {
                throw new IllegalArgumentException("Arrays size differs");
            }
            this.sortIds = fieldsToSortIds;
            this.sortModes = sortingModes;
            setFieldsToGroup(groupIds, allFields);
        }

        public void setFirstFieldToSort(int newSortFieldId, FieldDescriptor[] allFields) {
            int fieldIndex = ArraysCommons.findPosition(sortIds, newSortFieldId);
            boolean alreadyUsed = (fieldIndex == -1) ? false : true;
            boolean[] newFieldsToSortModes = null;
            int[] newFieldsToSortIds = null;
            // Bug fix
            while (sortIds.length < sortModes.length) {
                sortModes = ArraysCommons.remove(sortModes, 0);
            }
            // Bug fix end
            if (alreadyUsed) {
                newFieldsToSortIds = ArraysCommons.changePosition(sortIds, fieldIndex, 0);
                newFieldsToSortModes = ArraysCommons.changePosition(sortModes, fieldIndex, 0);
                newFieldsToSortModes[0] = !sortModes[fieldIndex];
            } else {
                newFieldsToSortIds = ArraysCommons.insert(sortIds, 0, newSortFieldId);
                newFieldsToSortModes = ArraysCommons.insert(sortModes, 0, BatchPresentationConsts.ASC);
            }
            setFieldsToSort(newFieldsToSortIds, newFieldsToSortModes, allFields);
        }

        public void addDynamicField(long fieldIdx, String fieldValue) {
            fieldIdx = fieldIdx - dynamics.size();
            for (DynamicField dynamo : dynamics) {
                if (dynamo.getDynamicValue().equals(fieldValue) && dynamo.getFieldIdx().longValue() == fieldIdx) {
                    return;
                }
            }
            dynamics.add(0, new DynamicField(fieldIdx, fieldValue));
            for (int i = 0; i < groupIds.length; ++i) {
                groupIds[i] = groupIds[i] + 1;
            }
            for (int i = 0; i < sortIds.length; ++i) {
                sortIds[i] = sortIds[i] + 1;
            }
            for (int i = 0; i < displayIds.length; ++i) {
                displayIds[i] = displayIds[i] + 1;
            }
            Map<Integer, FilterCriteria> filteredFieldsMap = new HashMap<Integer, FilterCriteria>();
            for (Map.Entry<Integer, FilterCriteria> entry : this.filters.entrySet()) {
                filteredFieldsMap.put(entry.getKey() + 1, entry.getValue());
            }
            this.filters.clear();
            this.filters.putAll(filteredFieldsMap);
        }

        public void removeDynamicField(long fieldIdx) {
            dynamics.remove((int) fieldIdx);
            if (ArraysCommons.findPosition(groupIds, (int) fieldIdx) != -1) {
                int pos = ArraysCommons.findPosition(groupIds, (int) fieldIdx);
                groupIds = ArraysCommons.remove(groupIds, pos);
            }
            if (ArraysCommons.findPosition(sortIds, (int) fieldIdx) != -1) {
                int pos = ArraysCommons.findPosition(sortIds, (int) fieldIdx);
                sortIds = ArraysCommons.remove(sortIds, pos);
                sortModes = ArraysCommons.remove(sortModes, pos);
            }
            if (ArraysCommons.findPosition(displayIds, (int) fieldIdx) != -1) {
                displayIds = ArraysCommons.remove(displayIds, ArraysCommons.findPosition(displayIds, (int) fieldIdx));
            }
            filters.remove((int) fieldIdx);
            for (int i = 0; i < groupIds.length; ++i) {
                if (groupIds[i] > fieldIdx) {
                    groupIds[i] = groupIds[i] - 1;
                }
            }
            for (int i = 0; i < sortIds.length; ++i) {
                if (sortIds[i] > fieldIdx) {
                    sortIds[i] = sortIds[i] - 1;
                }
            }
            for (int i = 0; i < displayIds.length; ++i) {
                if (displayIds[i] > fieldIdx) {
                    displayIds[i] = displayIds[i] - 1;
                }
            }
            Map<Integer, FilterCriteria> filteredFieldsMap = new HashMap<Integer, FilterCriteria>();
            for (Map.Entry<Integer, FilterCriteria> entry : this.filters.entrySet()) {
                if (entry.getKey() > fieldIdx) {
                    filteredFieldsMap.put(entry.getKey() - 1, entry.getValue());
                }
            }
            this.filters.clear();
            this.filters.putAll(filteredFieldsMap);
        }

        public void setFieldsToGroup(int[] fieldsToGroupIds, FieldDescriptor[] allFields) {
            // calculate newSortingIdList
            List<Integer> sortingIdList = ArraysCommons.createIntegerList(sortIds);
            List<Integer> groupingIdList = ArraysCommons.createIntegerList(fieldsToGroupIds);
            List<Integer> sortingNotGroupingIdList = new ArrayList<Integer>(sortingIdList);
            sortingNotGroupingIdList.removeAll(groupingIdList);
            List<Integer> sortingAndGroupingIdList = new ArrayList<Integer>(sortingIdList);
            sortingAndGroupingIdList.removeAll(sortingNotGroupingIdList);
            List<Integer> groupingNotSortingIdList = new ArrayList<Integer>(groupingIdList);
            groupingNotSortingIdList.removeAll(sortingAndGroupingIdList);

            List<Integer> newSortingIdList = new ArrayList<Integer>(sortingAndGroupingIdList.size() + groupingNotSortingIdList.size()
                    + sortingNotGroupingIdList.size());
            newSortingIdList.addAll(sortingAndGroupingIdList);
            newSortingIdList.addAll(groupingNotSortingIdList);
            newSortingIdList.addAll(sortingNotGroupingIdList);
            // end of calculation of newSortingIdList

            // delete filterable row
            Iterator<Integer> iterator = newSortingIdList.iterator();
            while (iterator.hasNext()) {
                Integer id = iterator.next();
                if (allFields[id].displayName.startsWith(ClassPresentation.filterable_prefix)) {
                    iterator.remove();
                }
            }

            // calculate newSortingModes
            boolean[] newSortingModes = new boolean[newSortingIdList.size()];
            for (int i = 0; i < newSortingIdList.size(); i++) {
                int pos = sortingIdList.indexOf(newSortingIdList.get(i));
                newSortingModes[i] = (pos < 0) ? BatchPresentationConsts.ASC : sortModes[pos];
            }
            // end of calculation of newSortingModes
            this.sortIds = ArraysCommons.createIntArray(newSortingIdList);
            this.sortModes = newSortingModes;

            // calculate newGroupingIds
            List<Integer> newGroupingIdList = new ArrayList<Integer>(sortingAndGroupingIdList.size() + groupingNotSortingIdList.size());
            newGroupingIdList.addAll(sortingAndGroupingIdList);
            newGroupingIdList.addAll(groupingNotSortingIdList);
            // end of calculate newGroupingIds

            // calculate new displayPositionIds
            List<Integer> newDisplayIdList = ArraysCommons.createIntegerList(displayIds);
            List<Integer> oldGroupIdList = ArraysCommons.createIntegerList(this.groupIds);
            for (Integer newGroupingId : newGroupingIdList) {
                if (!oldGroupIdList.contains(newGroupingId)) {
                    newDisplayIdList.remove(newGroupingId);
                }
            }
            this.displayIds = ArraysCommons.createIntArray(newDisplayIdList);
            // end of calculate new displayPositionIds
            this.groupIds = ArraysCommons.createIntArray(newGroupingIdList);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Fields)) {
                return false;
            }
            Fields f = (Fields) obj;
            return Objects.equal(filters, f.filters) && Arrays.equals(groupIds, f.groupIds) && Arrays.equals(sortIds, f.sortIds)
                    && Arrays.equals(sortModes, f.sortModes) && Arrays.equals(displayIds, f.displayIds);
        }

    }
}
