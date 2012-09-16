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
package ru.runa.jboss.DeploymentSorter;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.deployment.DeploymentSorter;

/*
 * Deploy not runawfe libs first and deploy after this runawfe libs in specified order 
 */
public class RunaDeploymentSorter extends DeploymentSorter {
    private final Log log = LogFactory.getLog(RunaDeploymentSorter.class);

    private String[] runawfe_libs = null;

    public RunaDeploymentSorter() {
        try {
            runawfe_libs = ResourceBundle.getBundle("runa_loading").getString("loading_order").split(",");
        } catch (Exception e) {
            log.warn("Couldn't find runawfe jar loading order.");
        }
    }

    public int compare(Object o1, Object o2) {
        int runaIdx1 = getLibIndex((URL) o1);
        int runaIdx2 = getLibIndex((URL) o2);
        if (runaIdx1 == -1 && runaIdx2 == -1) {
            return super.compare(o1, o2);
        }
        if (runaIdx1 < runaIdx2) {
            return -1;
        }
        if (runaIdx1 == runaIdx2) {
            return 0;
        }
        return 1;
    }

    private int getLibIndex(URL url) {
        if (runawfe_libs == null) {
            return -1;
        }
        for (int i = 0; i < runawfe_libs.length; ++i) {
            if (url.getFile().contains(runawfe_libs[i])) {
                return i;
            }
        }
        return -1;
    }
}
