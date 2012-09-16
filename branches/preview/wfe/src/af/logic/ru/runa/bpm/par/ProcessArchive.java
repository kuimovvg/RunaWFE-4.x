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
package ru.runa.bpm.par;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.wf.ProcessDefinitionArchiveFormatException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class ProcessArchive extends FileDataProvider {
    public static List<String> UNSECURED_FILE_NAMES = Lists.newArrayList();
    static {
        UNSECURED_FILE_NAMES.add(FORM_CSS_FILE_NAME);
        UNSECURED_FILE_NAMES.add(START_IMAGE_FILE_NAME);
        UNSECURED_FILE_NAMES.add(START_DISABLED_IMAGE_FILE_NAME);
    }

    static List<ProcessArchiveParser> processArchiveParsers = new ArrayList<ProcessArchiveParser>();
    static {
        // TODO move to Spring?
        processArchiveParsers.add(new JpdlArchiveParser());
        processArchiveParsers.add(new FileArchiveParser());
        processArchiveParsers.add(new VariableDefinitionParser());
        processArchiveParsers.add(new InteractionsParser());
    }

    private Map<String, byte[]> fileData = Maps.newHashMap();

    public ProcessArchive(byte[] data) {
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                byte[] bytes = ByteStreams.toByteArray(zis);
                if (bytes != null) {
                    fileData.put(entryName, bytes);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.close();
        } catch (IOException e) {
            throw new ProcessDefinitionArchiveFormatException(e);
        }
    }

    public ExecutableProcessDefinition parseProcessDefinition(ArchievedProcessDefinition processDefinitionDBImpl) {
        ExecutableProcessDefinition processDefinition = new ExecutableProcessDefinition(processDefinitionDBImpl);
        for (ProcessArchiveParser processArchiveParser : processArchiveParsers) {
            processArchiveParser.readFromArchive(this, processDefinition);
        }
        return processDefinition;
    }

    public Map<String, byte[]> getFileData() {
        return fileData;
    }

    @Override
    public byte[] getFileData(String fileName) {
        return fileData.get(fileName);
    }

}
