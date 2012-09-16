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

/**
  * Common constants for {@link BatchPresentation}.
  *
  * @author Konstantinov Aleksey 11.02.2012
  */
public class BatchPresentationConsts {
    /**
     * Allowed sizes for paged {@link BatchPresentation}.
     * This sizes will be available in web interface. 
     */
    private static final int[] ALLOWED_VIEW_SIZES = { 10, 50, 100, 500 };

    /**
     * Identity of default presentation group.
     */
    public static final String DEFAULT_ID = "batch_presentation_default_id";

    /**
     * Struts property to display for as default presentation name. 
     */
    public static final String DEFAULT_NAME = "label.batch_presentation_default_name";

    /**
     * Sort mode: ascending.
     */
    public static final boolean ASC = true;

    /**
     * Sort mode: descending. 
     */
    public static final boolean DSC = false;

    /**
     * Recommended maximum range size for requests with paging.
     */
    public static final int MAX_UNPAGED_REQUEST_SIZE = 10000;

    /**
     * Allowed sizes for paged {@link BatchPresentation}.
     * This sizes will be available in web interface. 
     * @return Allowed sizes for paged {@link BatchPresentation}.
     */
    public static int[] getAllowedViewSizes() {
        return ALLOWED_VIEW_SIZES.clone();
    }
}
