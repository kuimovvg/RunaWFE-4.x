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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.MapKey;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.collection.PersistentCollection;

import ru.runa.InternalApplicationException;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.presentation.filter.FilterCriteriaFactory;
import ru.runa.af.util.OracleCommons;
import ru.runa.commons.ArraysCommons;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Presentation, contains rules to get/display objects.
 * Contains object sorting rules, filter rules and so on.
 * Used to get objects from database and display them.   
 */
@Entity
@Table(name = "BATCH_PRESENTATIONS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class BatchPresentation implements Cloneable, Serializable {
    private static final long serialVersionUID = 6631653373163613071L;

    /**
     * Helper to hold fields set (such us fields to display, sort and so on).   
     */
    private transient Store storage;

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
    private int classPresentationId = -1;

    /**
     * Presentation group identity. Such as tasksList, processInstanceLists and so on.
     * Each group refers to some page in web interface.
     */
    private String batchPresentationId;

    /**
     * Presentation name. Displays in web interface. 
     */
    private String batchPresentationName;

    /**
     * Displayed fields indexes in correct order.
     */
    int[] fieldsToDisplayIds;

    /**
     * Sorting fields indexes in correct order. 
     * Size of array is equals to fieldsToSortModes size.
     */
    int[] fieldsToSortIds;

    /**
     * Sorting fields modes.
     * Size of array is equals to fieldsToSortIds size.
     */
    private boolean[] fieldsToSortModes;

    /**
     * Grouping fields indexes in correct order.
     */
    int[] fieldsToGroupIds;

    /**
     * {@link Map} from field index to {@link FilterCriteria} for filter.
     */
    private Map<Integer, FilterCriteria> filteredFieldsMap;

    /**
     * Page size for paged {@link BatchPresentation}.
     */
    private int rangeSize = 10;

    /**
     * Page number for paged {@link BatchPresentation}. 
     */
    private int pageNumber = 1;

    /**
     * Holds identifiers for expanded groups.
     */
    private List<String> expandedBlockList = new ArrayList<String>();

    /**
     * Removable fields, created for editable fields (with values, inserted by user).
     */
    List<DynamicField> dynamicFields = new ArrayList<DynamicField>();

    /**
     * Is needed by hibernate
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
    public BatchPresentation(String batchPresentationName, String batchPresentationId, int classPresentationId, int[] fieldsToSortIds,
            boolean[] fieldsToSortModes, int[] fieldsToDisplayIds, Map<Integer, FilterCriteria> fieldsToFilterMap, int[] fieldsToGroupIds) {
        setBatchPresentationName(batchPresentationName);
        setBatchPresentationId(batchPresentationId);
        setClassPresentationId(classPresentationId);
        setFieldsToSortIds(fieldsToSortIds);
        setFieldsToSortModes(fieldsToSortModes);
        setFieldsToDisplayIds(fieldsToDisplayIds);
        setFilteredFieldsMap(fieldsToFilterMap);
        setFieldsToGroupIds(fieldsToGroupIds);
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

    @Transient
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
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

    public void setPredefinedRangeSize(int rangeSize) {
        if (ArraysCommons.contains(getAllowedViewSizes(), rangeSize)) {
            setRangeSize(rangeSize);
        }
    }

    @Transient
    public boolean isDefault() {
        return batchPresentationName.equals(BatchPresentationConsts.DEFAULT_NAME);
    }

    public boolean isSortingField(int fieldIndex) {
        if (!getAllFields()[fieldIndex].isSortable) {
            return false;
        }
        return ArraysCommons.findPosition(fieldsToSortIds, fieldIndex) >= 0;
    }

    public int getSortingFieldPosition(int fieldIndex) {
        return ArraysCommons.findPosition(fieldsToSortIds, fieldIndex);
    }

    @CollectionOfElements
    @JoinTable(name = "DISPLAY_FIELDS", joinColumns = { @JoinColumn(name = "BATCH_PRESENTATION_ID", nullable = false, updatable = false) })
    @IndexColumn(name = "ARRAY_INDEX")
    @Column(name = "FIELD")
    public int[] getFieldsToDisplayIds() {
        return fieldsToDisplayIds;
    }

    public void setFieldsToDisplayIds(int[] fieldsToDisplayIds) {
        storage = null;
        this.fieldsToDisplayIds = fieldsToDisplayIds;
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

    @Column(name = "PRESENTATION_NAME", length = 128, nullable = false)
    @Index(name = "PRESENTATION_NAME_ID_IDX")
    public String getBatchPresentationName() {
        return batchPresentationName;
    }

    public void setBatchPresentationName(String name) {
        batchPresentationName = OracleCommons.fixNullString(name);
    }

    @Column(name = "PRESENTATION_ID", length = 128, nullable = false)
    @Index(name = "PRESENTATION_NAME_ID_IDX")
    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    private void setBatchPresentationId(String tagName) {
        batchPresentationId = tagName;
    }

    @CollectionOfElements
    @JoinTable(name = "SORTED_FIELDS", joinColumns = { @JoinColumn(name = "BATCH_PRESENTATION_ID", nullable = false, updatable = false) })
    @IndexColumn(name = "ARRAY_INDEX")
    @Column(name = "FIELD", updatable = false)
    public int[] getFieldsToSortIds() {
        return fieldsToSortIds;
    }

    private void setFieldsToSortIds(int[] fieldsToSortIds) {
        this.fieldsToSortIds = fieldsToSortIds;
        storage = null;
    }

    public void setFieldsToSort(int[] fieldsToSortIds, boolean[] sortingModes) {
        if (fieldsToSortIds.length != sortingModes.length) {
            throw new IllegalArgumentException("Arrays size differs");
        }
        setFieldsToSortIds(fieldsToSortIds);
        setFieldsToSortModes(sortingModes);
        setFieldsToGroup(getFieldsToGroupIds());
        storage = null;
    }

    public void setFirstFieldToSort(int newSortFieldId) {
        int fieldIndex = ArraysCommons.findPosition(fieldsToSortIds, newSortFieldId);
        boolean alreadyUsed = (fieldIndex == -1) ? false : true;
        boolean[] newFieldsToSortModes = null;
        int[] newFieldsToSortIds = null;
        // Bug fix
        while (fieldsToSortIds.length < fieldsToSortModes.length) {
            fieldsToSortModes = ArraysCommons.remove(fieldsToSortModes, 0);
        }
        // Bug fix end
        if (alreadyUsed) {
            newFieldsToSortIds = ArraysCommons.changePosition(fieldsToSortIds, fieldIndex, 0);
            newFieldsToSortModes = ArraysCommons.changePosition(fieldsToSortModes, fieldIndex, 0);
            newFieldsToSortModes[0] = !fieldsToSortModes[fieldIndex];
        } else {
            newFieldsToSortIds = ArraysCommons.insert(fieldsToSortIds, 0, newSortFieldId);
            newFieldsToSortModes = ArraysCommons.insert(fieldsToSortModes, 0, BatchPresentationConsts.ASC);
        }
        setFieldsToSort(newFieldsToSortIds, newFieldsToSortModes);
        storage = null;
    }

    @CollectionOfElements
    @JoinTable(name = "SORTING_MODES", joinColumns = { @JoinColumn(name = "BATCH_PRESENTATION_ID", nullable = false, updatable = false) })
    @IndexColumn(name = "ARRAY_INDEX")
    @Column(name = "SORTING_MODE", updatable = false)
    public boolean[] getFieldsToSortModes() {
        return fieldsToSortModes;
    }

    private void setFieldsToSortModes(boolean[] fieldsToSortModes) {
        this.fieldsToSortModes = fieldsToSortModes;
        storage = null;
    }

    @CollectionOfElements(fetch = FetchType.EAGER)
    @JoinTable(name = "GROUP_FIELDS_EXPANDED", joinColumns = { @JoinColumn(name = "BATCH_PRESENTATION_ID", nullable = false, updatable = false) })
    @IndexColumn(name = "ARRAY_INDEX")
    @Column(name = "EXPANDED", updatable = false)
    public List<String> getExpandedIds() {
        if (fieldsToGroupIds == null || fieldsToGroupIds.length == 0) {
            expandedBlockList.clear();
        }
        return expandedBlockList;
    }

    protected void setExpandedIds(List<String> expandedIds) {
        expandedBlockList = expandedIds;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = DynamicField.class)
    @Sort(type = SortType.NATURAL)
    @JoinColumn(name = "BATCH_PRESENTATION_ID")
    @IndexColumn(name = "ARRAY_INDEX")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<DynamicField> getDynamicFields() {
        return dynamicFields;
    }

    protected void setDynamicFields(List<DynamicField> dynamicFields) {
        storage = null;
        this.dynamicFields = dynamicFields;
    }

    public void addDynamicField(long fieldIdx, String fieldValue) {
        fieldIdx = fieldIdx - dynamicFields.size();
        for (DynamicField dynamo : dynamicFields) {
            if (dynamo.getDynamicValue().equals(fieldValue) && dynamo.getFieldIdx().longValue() == fieldIdx) {
                return;
            }
        }
        dynamicFields.add(0, new DynamicField(fieldIdx, fieldValue));
        for (int i = 0; i < fieldsToGroupIds.length; ++i) {
            fieldsToGroupIds[i] = fieldsToGroupIds[i] + 1;
        }
        for (int i = 0; i < fieldsToSortIds.length; ++i) {
            fieldsToSortIds[i] = fieldsToSortIds[i] + 1;
        }
        for (int i = 0; i < fieldsToDisplayIds.length; ++i) {
            fieldsToDisplayIds[i] = fieldsToDisplayIds[i] + 1;
        }
        Map<Integer, FilterCriteria> filteredFieldsMap = new HashMap<Integer, FilterCriteria>();
        for (Map.Entry<Integer, FilterCriteria> entry : this.filteredFieldsMap.entrySet()) {
            filteredFieldsMap.put(entry.getKey() + 1, entry.getValue());
        }
        this.filteredFieldsMap.clear();
        this.filteredFieldsMap.putAll(filteredFieldsMap);
        storage = null;
    }

    public void removeDynamicField(long fieldIdx) {
        dynamicFields.remove((int) fieldIdx);
        if (ArraysCommons.findPosition(fieldsToGroupIds, (int) fieldIdx) != -1) {
            int pos = ArraysCommons.findPosition(fieldsToGroupIds, (int) fieldIdx);
            fieldsToGroupIds = ArraysCommons.remove(fieldsToGroupIds, pos);
        }
        if (ArraysCommons.findPosition(fieldsToSortIds, (int) fieldIdx) != -1) {
            int pos = ArraysCommons.findPosition(fieldsToSortIds, (int) fieldIdx);
            fieldsToSortIds = ArraysCommons.remove(fieldsToSortIds, pos);
            fieldsToSortModes = ArraysCommons.remove(fieldsToSortModes, pos);
        }
        if (ArraysCommons.findPosition(fieldsToDisplayIds, (int) fieldIdx) != -1) {
            fieldsToDisplayIds = ArraysCommons.remove(fieldsToDisplayIds, ArraysCommons.findPosition(fieldsToDisplayIds, (int) fieldIdx));
        }
        filteredFieldsMap.remove(fieldIdx);
        for (int i = 0; i < fieldsToGroupIds.length; ++i) {
            if (fieldsToGroupIds[i] > fieldIdx) {
                fieldsToGroupIds[i] = fieldsToGroupIds[i] - 1;
            }
        }
        for (int i = 0; i < fieldsToSortIds.length; ++i) {
            if (fieldsToSortIds[i] > fieldIdx) {
                fieldsToSortIds[i] = fieldsToSortIds[i] - 1;
            }
        }
        for (int i = 0; i < fieldsToDisplayIds.length; ++i) {
            if (fieldsToDisplayIds[i] > fieldIdx) {
                fieldsToDisplayIds[i] = fieldsToDisplayIds[i] - 1;
            }
        }
        Map<Integer, FilterCriteria> filteredFieldsMap = new HashMap<Integer, FilterCriteria>();
        for (Map.Entry<Integer, FilterCriteria> entry : this.filteredFieldsMap.entrySet()) {
            if (entry.getKey() > fieldIdx) {
                filteredFieldsMap.put(entry.getKey() - 1, entry.getValue());
            }
        }
        this.filteredFieldsMap.clear();
        this.filteredFieldsMap.putAll(filteredFieldsMap);
        storage = null;
    }

    public boolean isFieldFiltered(int fieldId) {
        if (getAllFields()[fieldId].filterMode == FieldFilterMode.NONE) {
            return false;
        }
        return filteredFieldsMap.containsKey(new Integer(fieldId));
    }

    public boolean isFieldGroupped(int fieldId) {
        if (!getAllFields()[fieldId].isSortable) {
            return false;
        }
        return ArraysCommons.contains(fieldsToGroupIds, fieldId);
    }

    public FilterCriteria getFieldFilteredCriteria(int fieldId) {
        FilterCriteria filterCriteria = filteredFieldsMap.get(new Integer(fieldId));
        if (filterCriteria == null) {
            String fieldType = getAllFields()[fieldId].fieldType;
            filterCriteria = FilterCriteriaFactory.getFilterCriteria(fieldType);
        }
        return filterCriteria;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = FilterCriteria.class)
    @Sort(type = SortType.UNSORTED)
    @JoinColumn(name = "BATCH_PRESENTATION_ID", updatable = true)
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @MapKey(targetElement = Integer.class, columns = @Column(name = "FIELD_INDEX"))
    public Map<Integer, FilterCriteria> getFilteredFieldsMap() {
        return filteredFieldsMap;
    }

    public void setFilteredFieldsMap(Map<Integer, FilterCriteria> newFilteredFieldsMap) {
        try {
            if (newFilteredFieldsMap instanceof HashMap && (newFilteredFieldsMap == null || newFilteredFieldsMap.isEmpty())) {
                // This case is for RTN - do not create dependence from hibernate
                setPageNumber(1);
                if (filteredFieldsMap == null) {
                    filteredFieldsMap = newFilteredFieldsMap;
                } else {
                    filteredFieldsMap.clear();
                }
                return;
            }
            if (newFilteredFieldsMap instanceof PersistentCollection && !(filteredFieldsMap instanceof PersistentCollection)) {
                filteredFieldsMap = newFilteredFieldsMap;
                return;
            }
            if (filteredFieldsMap != null && filteredFieldsMap != newFilteredFieldsMap) {
                if (filteredFieldsMap.size() == newFilteredFieldsMap.size()) {
                    for (Map.Entry<Integer, FilterCriteria> entry : filteredFieldsMap.entrySet()) {
                        if (!entry.getValue().equals(newFilteredFieldsMap.get(entry.getKey()))) {
                            setPageNumber(1);
                            break;
                        }
                    }
                } else {
                    setPageNumber(1);
                }
                filteredFieldsMap.clear();
                for (Map.Entry<Integer, FilterCriteria> entry : newFilteredFieldsMap.entrySet()) {
                    filteredFieldsMap.put(entry.getKey(), entry.getValue());
                }
            } else {
                filteredFieldsMap = newFilteredFieldsMap;
            }
        } finally {
            storage = null;
        }
    }

    @CollectionOfElements
    @JoinTable(name = "GROUP_FIELDS", joinColumns = { @JoinColumn(name = "BATCH_PRESENTATION_ID", nullable = false, updatable = false) })
    @IndexColumn(name = "ARRAY_INDEX")
    @Column(name = "FIELD", updatable = false)
    public int[] getFieldsToGroupIds() {
        return fieldsToGroupIds;
    }

    private void setFieldsToGroupIds(int[] fieldsToGroupIds) {
        this.fieldsToGroupIds = fieldsToGroupIds;
        //if (fieldsToGroupIds == null || fieldsToGroupIds.length == 0)
        //	this.expandedBlockList.clear();
        storage = null;
    }

    public void setFieldsToGroup(int[] fieldsToGroupIds) {
        // calculate newSortingIdList
        List<Integer> sortingIdList = ArraysCommons.createIntegerList(fieldsToSortIds);
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
            if (getAllFields()[id].displayName.startsWith(ClassPresentation.filterable_prefix)) {
                iterator.remove();
            }
        }

        // calculate newSortingModes
        boolean[] newSortingModes = new boolean[newSortingIdList.size()];
        for (int i = 0; i < newSortingIdList.size(); i++) {
            int pos = sortingIdList.indexOf(newSortingIdList.get(i));
            newSortingModes[i] = (pos < 0) ? BatchPresentationConsts.ASC : fieldsToSortModes[pos];
        }
        // end of calculation of newSortingModes
        setFieldsToSortIds(ArraysCommons.createIntArray(newSortingIdList));
        setFieldsToSortModes(newSortingModes);

        // calculate newGroupingIds
        List<Integer> newGroupingIdList = new ArrayList<Integer>(sortingAndGroupingIdList.size() + groupingNotSortingIdList.size());
        newGroupingIdList.addAll(sortingAndGroupingIdList);
        newGroupingIdList.addAll(groupingNotSortingIdList);
        // end of calculate newGroupingIds

        // calculate new displayPositionIds
        List<Integer> newDisplayIdList = ArraysCommons.createIntegerList(fieldsToDisplayIds);
        List<Integer> oldGroupIdList = ArraysCommons.createIntegerList(this.fieldsToGroupIds);
        for (Integer newGroupingId : newGroupingIdList) {
            if (!oldGroupIdList.contains(newGroupingId)) {
                newDisplayIdList.remove(newGroupingId);
            }
        }
        setFieldsToDisplayIds(ArraysCommons.createIntArray(newDisplayIdList));
        // end of calculate new displayPositionIds

        setFieldsToGroupIds(ArraysCommons.createIntArray(newGroupingIdList));
        storage = null;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BATCH_PRESENTATIONS")
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

    @Transient
    ClassPresentation getClassPresentation() {
        return ClassPresentationFinderFactory.getClassPresentationFinder().getClassPresentationById(classPresentationId);
    }

    @Column(name = "CLASS_PRESENTATION_ID")
    private int getClassPresentationId() {
        return classPresentationId;
    }

    private void setClassPresentationId(int classPresentationId) {
        this.classPresentationId = classPresentationId;
    }

    @Transient
    public Class<?> getClassPresentationClass() {
        return getClassPresentation().getPresentationClass();
    }

    @Transient
    public String getClassPresentationRestrictions() {
        return getClassPresentation().getRestrictions();
    }

    @Transient
    public int[] getAllowedViewSizes() {
        return BatchPresentationConsts.getAllowedViewSizes();
    }

    @Transient
    public boolean isWithPaging() {
        return getClassPresentation().isWithPaging();
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
        return Objects.equal(getBatchPresentationName(), presentation.getBatchPresentationName())
                && Objects.equal(getBatchPresentationId(), presentation.getBatchPresentationId());
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
        result &= Objects.equal(getBatchPresentationName(), presentation.getBatchPresentationName());
        result &= Objects.equal(getBatchPresentationId(), presentation.getBatchPresentationId());
        result &= Objects.equal(classPresentationId, presentation.classPresentationId);
        result &= Objects.equal(getFilteredFieldsMap(), presentation.getFilteredFieldsMap());
        result &= Arrays.equals(fieldsToGroupIds, presentation.getFieldsToGroupIds());
        result &= Arrays.equals(fieldsToSortIds, presentation.fieldsToSortIds);
        result &= Arrays.equals(fieldsToSortModes, presentation.fieldsToSortModes);
        result &= Objects.equal(dynamicFields, presentation.dynamicFields);
//        for (int i = 0; i < dynamicFields.size(); ++i) {
//            if (!(dynamicFields.get(i).getFieldIdx() == presentation.dynamicFields.get(i).getFieldIdx() 
//                    && dynamicFields.get(i).getDynamicValue() == presentation.dynamicFields.get(i).getDynamicValue())) {
//                return false;
//            }
//        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBatchPresentationName(), getBatchPresentationId());
    }

    @Override
    public BatchPresentation clone() {
        try {
            BatchPresentation clone = (BatchPresentation) super.clone();
            clone.id = null;
            clone.version = null;
            clone.fieldsToGroupIds = fieldsToGroupIds.clone();
            clone.fieldsToSortIds = fieldsToSortIds.clone();
            clone.fieldsToSortModes = fieldsToSortModes.clone();
            clone.fieldsToDisplayIds = fieldsToDisplayIds.clone();
            clone.filteredFieldsMap = new HashMap<Integer, FilterCriteria>();
            for (Integer idx : filteredFieldsMap.keySet()) {
                clone.filteredFieldsMap.put(idx, filteredFieldsMap.get(idx).clone());
            }
            clone.dynamicFields = new ArrayList<DynamicField>();
            for (DynamicField field : dynamicFields) {
                clone.dynamicFields.add(field.clone());
            }
            clone.expandedBlockList = Lists.newArrayList(expandedBlockList);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalApplicationException(e);
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeInt(getClassPresentationId());
        objectOutputStream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        setClassPresentationId(objectInputStream.readInt());
        objectInputStream.defaultReadObject();
    }

    public boolean syncWith(Set<BatchPresentation> batchSet) {
        if (batchSet == null) {
            return false;
        }
        for (BatchPresentation batch : batchSet) {
            if (!isBatchSame(batch)) {
                continue;
            }
            rangeSize = batch.rangeSize;
            pageNumber = batch.pageNumber;
            fieldsToGroupIds = batch.fieldsToGroupIds.clone();
            fieldsToSortIds = batch.fieldsToSortIds.clone();
            fieldsToSortModes = batch.fieldsToSortModes.clone();
            fieldsToDisplayIds = batch.fieldsToDisplayIds.clone();
            filteredFieldsMap.clear();
            for (Integer idx : batch.filteredFieldsMap.keySet()) {
                filteredFieldsMap.put(idx, batch.filteredFieldsMap.get(idx).clone());
            }
            expandedBlockList.clear();
            expandedBlockList.addAll(batch.expandedBlockList);
            dynamicFields.clear();
            for (DynamicField field : batch.dynamicFields) {
                dynamicFields.add(field.clone());
            }
            classPresentationId = batch.classPresentationId;
            return true;
        }
        return false;
    }

    public boolean isInColection(Collection<BatchPresentation> collection) {
        for (BatchPresentation batch : collection) {
            if (isBatchSame(batch)) {
                return true;
            }
        }
        return false;
    }

    public void removeFromColection(Collection<BatchPresentation> collection) {
        for (BatchPresentation batch : collection) {
            if (isBatchSame(batch)) {
                collection.remove(batch);
                return;
            }
        }
    }

    @Transient
    private boolean isBatchSame(BatchPresentation other) {
        return Objects.equal(other.batchPresentationId, batchPresentationId) && Objects.equal(other.batchPresentationName, batchPresentationName);
    }

    @Transient
    public FieldDescriptor[] getDisplayedFieldsDescription() {
        return (FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(getAllFields(), getFieldsToDisplayIds());
    }

    @Transient
    public FieldDescriptor[] getFieldsDescription() {
        return getClassPresentation().getFields();
    }

    public boolean fieldEquals(BatchPresentation other) {
        return classPresentationId == other.classPresentationId && getFilteredFieldsMap().equals(other.getFilteredFieldsMap())
                && Arrays.equals(fieldsToGroupIds, other.getFieldsToGroupIds()) && Arrays.equals(fieldsToSortIds, other.getFieldsToSortIds())
                && Arrays.equals(fieldsToSortModes, other.getFieldsToSortModes()) && Arrays.equals(fieldsToDisplayIds, other.fieldsToDisplayIds);
    }

    /**
     * Get helper to hold fields set (such us fields to display, sort and so on).
     * @return Helper to current {@link BatchPresentation}. 
     */
    @Transient
    private Store getStore() {
        if (storage == null) {
            storage = new Store(this);
        }
        return storage;
    }
}
