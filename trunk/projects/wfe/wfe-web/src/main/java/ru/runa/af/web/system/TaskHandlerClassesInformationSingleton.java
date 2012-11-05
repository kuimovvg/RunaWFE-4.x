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

import java.util.Collections;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ClassLoaderUtil;

/**
 * User: stan79
 * @since 3.0
 */
public class TaskHandlerClassesInformationSingleton {
    private static final Log log = LogFactory.getLog(TaskHandlerClassesInformationSingleton.class);

    private static TaskHandlerClassesInformationSingleton taskHandlerClassesInformationsigleton = null;
    private final SortedSet<String> taskHandlerImplementationClasses = new TreeSet<String>();

    private static final Class<?> TASK_HANDLER_INTERFACE = ClassLoaderUtil.loadClass("ru.runa.wf.logic.bot.TaskHandler");

    private TaskHandlerClassesInformationSingleton() {
        init();
    }

    public static TaskHandlerClassesInformationSingleton getInstance() {
        if (taskHandlerClassesInformationsigleton == null) {
            taskHandlerClassesInformationsigleton = new TaskHandlerClassesInformationSingleton();
        }
        return taskHandlerClassesInformationsigleton;
    }

    private void init() {
        String runawfeEar = System.getProperty("jboss.home.dir") + "/server/" + System.getProperty("jboss.server.name") + "/deploy/runawfe.ear!/lib/";
        String[] botTaskHandlerImplJarFileNames = { "wfe-bots-4.0.0-SNAPSHOT.jar" };
        try {
            for (int i = 0; i < botTaskHandlerImplJarFileNames.length; i++) {
                JarFile jarFile = new JarFile(runawfeEar + botTaskHandlerImplJarFileNames[i].trim());
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    // If we can't load class - just move to next class.
                    try {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.endsWith(".class")) {
                            int lastIndexOfDotSymbol = entryName.lastIndexOf('.');
                            entryName = entryName.substring(0, lastIndexOfDotSymbol).replace('/', '.');

                            Class<?> someClass = Class.forName(entryName);
                            if (TASK_HANDLER_INTERFACE.isAssignableFrom(someClass)) {
                                taskHandlerImplementationClasses.add(someClass.getCanonicalName());
                            }
                        }
                    } catch (Throwable e) {
                        log.warn("Error on loading task handlers list." + e.getMessage());
                    }
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
    

    public SortedSet<String> getTaskHandlerImplementationClasses() {
        return Collections.unmodifiableSortedSet(taskHandlerImplementationClasses);
    }
}
