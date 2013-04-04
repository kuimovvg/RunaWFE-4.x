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
package ru.runa.af.web.system;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.extension.TaskHandler;

/**
 * User: stan79
 * 
 * @since 3.0
 */
public class TaskHandlerClassesInformation {
    private static final Log log = LogFactory.getLog(TaskHandlerClassesInformation.class);
    private static final SortedSet<String> taskHandlerImplementationClasses = new TreeSet<String>();

    static {
        init();
    }

    private static void init() {
        String earFilePath = System.getProperty("jboss.home.dir") + "/server/" + System.getProperty("jboss.server.name") + "/deploy/runawfe.ear";
        try {
            ZipInputStream earStream = new ZipInputStream(new FileInputStream(earFilePath));
            ZipEntry entry;
            while ((entry = earStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".jar")) {
                    log.debug("Searching in " + entry.getName());
                    searchInJar(earStream);
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void searchInJar(InputStream jarStream) throws IOException {
        JarInputStream jis = new JarInputStream(jarStream);
        ZipEntry entry;
        while ((entry = jis.getNextEntry()) != null) {
            if (entry.getName().endsWith(".class")) {
                try {
                    String className = entry.getName();
                    int lastIndexOfDotSymbol = className.lastIndexOf('.');
                    className = className.substring(0, lastIndexOfDotSymbol).replace('/', '.');
                    // If we can't load class - just move to next class.
                    Class<?> someClass = ClassLoaderUtil.loadClass(className);
                    if (TaskHandler.class.isAssignableFrom(someClass) && !Modifier.isAbstract(someClass.getModifiers())) {
                        taskHandlerImplementationClasses.add(someClass.getCanonicalName());
                    }
                } catch (Throwable e) {
                    log.warn("Error on loading task handler for " + e.getMessage());
                }
            }
        }
    }

    public static SortedSet<String> getClassNames() {
        return Collections.unmodifiableSortedSet(taskHandlerImplementationClasses);
    }

    public static boolean isValid(String taskHandlerClassName) {
        return taskHandlerImplementationClasses.contains(taskHandlerClassName);
    }
}
