/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.file.def;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.module.def.ModuleDefinition;
import ru.runa.bpm.module.exe.ModuleInstance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

@Entity
@DiscriminatorValue(value = "F")
public class FileDefinition extends ModuleDefinition {
    // TODO optimization?
    // private Map<String, Object> processFiles = Maps.newHashMap();
    private Set<ProcessDefinitionFile> files = Sets.newHashSet();

    public FileDefinition() {
    }

    @Override
    public ModuleInstance createInstance() {
        return null;
    }

    @OneToMany(fetch = FetchType.EAGER, targetEntity = ProcessDefinitionFile.class)
    @Sort(type = SortType.UNSORTED)
    @JoinColumn(name = "DEFINITION_ID_")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<ProcessDefinitionFile> getFiles() {
        return files;
    }

    public void setFiles(Set<ProcessDefinitionFile> files) {
        this.files = files;
    }

    @PostLoad
    public void initializeFilesMap() {
        System.out.println("processFiles.clear();");
        // for (ProcessDefinitionFile processDefinitionFile : files) {
        // processFiles.put(processDefinitionFile.getName(),
        // processDefinitionFile.getBytes());
        // }
    }

    // @OneToMany(fetch = FetchType.EAGER, targetEntity =
    // ByteArrayBlobType.class)
    // @Sort(type = SortType.UNSORTED)
    // @JoinColumn(name = "DEFINITION_ID_", updatable = true)
    // @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    // @MapKey(targetElement = Integer.class, columns = @Column(name = "NAME_"))
    // @Column(name = "BYTES_")
    // public Map<String, Object> getProcessFiles() {
    // return processFiles;
    // }
    //
    // public void setProcessFiles(Map<String, Object> processFiles) {
    // this.processFiles = processFiles;
    // }

    /**
     * add a file to this definition.
     */
    public void addFile(String name, byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteStreams.copy(bais, baos);
            ProcessDefinitionFile processDefinitionFile = new ProcessDefinitionFile();
            processDefinitionFile.setName(name);
            processDefinitionFile.setBytes(baos.toByteArray());
            files.add(processDefinitionFile);
            // processFiles.put(processDefinitionFile.getName(),
            // processDefinitionFile.getBytes());
        } catch (IOException e) {
            throw new InternalApplicationException("Unable to store " + name);
        }
    }

    public byte[] getBytes(String name) {
        Preconditions.checkNotNull(name, "name");
        // return (byte[]) processFiles.get(name);
        for (ProcessDefinitionFile processDefinitionFile : files) {
            if (name.equals(processDefinitionFile.getName())) {
                return processDefinitionFile.getBytes();
            }
        }
        return null;
    }
}
