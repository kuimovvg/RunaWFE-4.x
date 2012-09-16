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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import ru.runa.InternalApplicationException;
import ru.runa.af.util.OracleCommons;

import com.google.common.base.Objects;

/**
 * Created on 17.01.2005
 * 
 */
@Entity
@Table(name = "PROFILES")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class Profile implements Serializable, Cloneable {
    private static final long serialVersionUID = 2605060177331685642L;
    private Long id;

    private Long version;

    private Long actorId;

    private Set<BatchPresentation> batchPresentationSet = new HashSet<BatchPresentation>();

    private Map<String, String> activeBatchPresentationIdsMap = new HashMap<String, String>();

    private Set<String> visibleBlockNameSet = new HashSet<String>();

    /**
     * Hibernate requieres default constractor
     */
    private Profile() {
    }

    public Profile(BatchPresentation[] batchPresentations) {
        for (BatchPresentation batch : batchPresentations) {
            addBatchPresentation(batch);
        }
    }

    public void addBatchPresentation(BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            throw new IllegalArgumentException("Attempt to add NULL batch presentation");
        }

        batchPresentation.removeFromColection(batchPresentationSet);
        batchPresentationSet.add(batchPresentation);

        if (!activeBatchPresentationIdsMap.containsKey(batchPresentation.getBatchPresentationId())) {
            activeBatchPresentationIdsMap.put(batchPresentation.getBatchPresentationId(), batchPresentation.getBatchPresentationName());
        }
    }

    /**
     * returns all batch presentations for specified presentation_id
     */
    public BatchPresentation[] getBatchPresentations(String batchPresentationId) throws BatchPresentationNotFoundException {
        ArrayList<BatchPresentation> retVal = new ArrayList<BatchPresentation>();
        for (BatchPresentation batch : batchPresentationSet) {
            if (Objects.equal(batch.getBatchPresentationId(), batchPresentationId)) {
                retVal.add(batch);
            }
        }
        if (retVal.size() == 0) {
            throw new BatchPresentationNotFoundException("Batch presentations not found, id: " + batchPresentationId);
        }
        return retVal.toArray(new BatchPresentation[retVal.size()]);
    }

    public void setActiveBatchPresentation(String batchPresentationId, String batchPresentationName) throws BatchPresentationNotFoundException {
        for (BatchPresentation batch : batchPresentationSet) {
            if (batch.getBatchPresentationId().equals(batchPresentationId) && batch.getBatchPresentationName().equals(batchPresentationName)) {
                activeBatchPresentationIdsMap.put(batchPresentationId, batchPresentationName);
                return;
            }
        }
        throw new BatchPresentationNotFoundException(batchPresentationId, batchPresentationName);
    }

    public BatchPresentation getActiveBatchPresentation(String batchPresentationId) throws BatchPresentationNotFoundException {
        String name = activeBatchPresentationIdsMap.get(batchPresentationId);
        name = OracleCommons.fixNullString(name);
        for (BatchPresentation batch : batchPresentationSet) {
            if (batch.getBatchPresentationId().equals(batchPresentationId) && batch.getBatchPresentationName().equals(name)) {
                return batch;
            }
        }

        throw new BatchPresentationNotFoundException(batchPresentationId, name);
    }

    public void deleteBatchPresentation(BatchPresentation batchPresentation) {
        if (batchPresentation.isDefault()) {
            return;
        }

        if (activeBatchPresentationIdsMap.get(batchPresentation.getBatchPresentationId()).equals(batchPresentation.getBatchPresentationName())) {
            activeBatchPresentationIdsMap.put(batchPresentation.getBatchPresentationId(), BatchPresentationConsts.DEFAULT_NAME);
        }

        for (BatchPresentation batch : batchPresentationSet) {
            if (batch.getBatchPresentationId().equals(batchPresentation.getBatchPresentationId())
                    && batch.getBatchPresentationName().equals(batchPresentation.getBatchPresentationName())) {
                batchPresentationSet.remove(batch);
                return;
            }
        }
    }

    @CollectionOfElements(fetch = FetchType.EAGER)
    @JoinTable(name = "ACTIVE_BATCH_PRESENTATIONS", joinColumns = @JoinColumn(name = "PROFILE_ID", nullable = false, updatable = false))
    @MapKey(columns = @Column(name = "PRESENTATION_ID", nullable = false))
    @Column(name = "PRESENTATION_NAME", updatable = false)
    protected Map<String, String> getActiveBatchPresentationIdsMap() {
        return activeBatchPresentationIdsMap;
    }

    protected void setActiveBatchPresentationIdsMap(Map<String, String> activeBatchPresentationIdsMap) {
        this.activeBatchPresentationIdsMap = activeBatchPresentationIdsMap;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = BatchPresentation.class)
    @Sort(type = SortType.UNSORTED)
    @JoinColumn(name = "PROFILE_ID")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<BatchPresentation> getBatchPresentationSet() {
        return batchPresentationSet;
    }

    protected void setBatchPresentationSet(Set<BatchPresentation> batchPresentationSet) {
        this.batchPresentationSet = batchPresentationSet;
    }

    protected void setVisibleBlockNameSet(Set<String> visibleBlockNameSet) {
        this.visibleBlockNameSet = visibleBlockNameSet;
    }

    public boolean isBlockVisible(String blockId) {
        return visibleBlockNameSet.contains(blockId);
    }

    public void changeBlockVisibility(String blockId) {
        if (isBlockVisible(blockId)) {
            visibleBlockNameSet.remove(blockId);
        } else {
            visibleBlockNameSet.add(blockId);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PROFILES")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Column(name = "ACTOR_ID", unique = true, nullable = false)
    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    @Version
    @Column(name = "VERSION", nullable = false)
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Profile)) {
            return false;
        }
        Profile profile = (Profile) obj;
        return Objects.equal(actorId, profile.actorId)
                && Objects.equal(visibleBlockNameSet, profile.visibleBlockNameSet)
                && Objects.equal(batchPresentationSet, profile.batchPresentationSet)
                && Objects.equal(activeBatchPresentationIdsMap, profile.activeBatchPresentationIdsMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(visibleBlockNameSet, batchPresentationSet, activeBatchPresentationIdsMap, actorId);
    }

//    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
//        objectOutputStream.defaultWriteObject();
//        objectOutputStream.writeLong(id);
//        objectOutputStream.writeLong(version);
//        objectOutputStream.writeLong(actorId);
//        SerializationCommons.writeCollection(objectOutputStream, getBatchPresentationSet());
//        SerializationCommons.writeMap(objectOutputStream, activeBatchPresentationIdsMap);
//        SerializationCommons.writeCollection(objectOutputStream, visibleBlockNameSet);
//    }
//
//    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
//        objectInputStream.defaultReadObject();
//        id = objectInputStream.readLong();
//        version = objectInputStream.readLong();
//        actorId = objectInputStream.readLong();
//        setBatchPresentationSet(SerializationCommons.readSet(objectInputStream));
//        activeBatchPresentationIdsMap = SerializationCommons.readMap(objectInputStream);
//        Set<String> tempVisibleBlocksSet = Sets.newHashSet();
//        SerializationCommons.readCollection(objectInputStream, tempVisibleBlocksSet);
//        setVisibleBlockNameSet(tempVisibleBlocksSet);
//    }

    @Override
    public Profile clone() {
        Profile retVal = new Profile();
        retVal.actorId = actorId;
        retVal.id = id;
        retVal.version = version;
        retVal.activeBatchPresentationIdsMap.putAll(activeBatchPresentationIdsMap);
        retVal.visibleBlockNameSet.addAll(visibleBlockNameSet);
        for (BatchPresentation batch : batchPresentationSet) {
            retVal.batchPresentationSet.add(batch.clone());
        }
        return retVal;
    }

    public void syncWith(Profile other) {
        if (!Objects.equal(actorId, other.actorId)) {
            throw new InternalApplicationException("Can't sync profiles from different users");
        }
        activeBatchPresentationIdsMap.clear();
        activeBatchPresentationIdsMap.putAll(other.activeBatchPresentationIdsMap);
        visibleBlockNameSet.clear();
        visibleBlockNameSet.addAll(other.visibleBlockNameSet);
        Set<BatchPresentation> deleted = new HashSet<BatchPresentation>();
        for (BatchPresentation batch : batchPresentationSet) {
            if (!batch.syncWith(other.batchPresentationSet)) {
                deleted.add(batch);
            }
        }
        batchPresentationSet.removeAll(deleted);
        for (BatchPresentation batch : other.batchPresentationSet) {
            if (!batch.isInColection(batchPresentationSet)) {
                batchPresentationSet.add(batch.clone());
            }
        }
    }

    public void getOmmitedBatchPesentations() {
        for (BatchPresentation batch : ProfileFactory.getInstance().getDefaultProfile(actorId).getBatchPresentationSet()) {
            try {
                getBatchPresentations(batch.getBatchPresentationId());
            } catch (BatchPresentationNotFoundException e) {
                addBatchPresentation(batch.clone());
            }
        }
    }
}
