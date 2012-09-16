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
 * Created 19.10.2005
 * 
 */
public class BatchPresentationNotFoundException extends Exception {

    private static final long serialVersionUID = -532308995146301329L;

    public BatchPresentationNotFoundException(String batchPresentationId, String batchPresentationName) {
        super("Batch presentations not found, id:" + batchPresentationId + ", name: " + batchPresentationName);
    }

    public BatchPresentationNotFoundException(String batchPresentationId) {
        super("Batch presentations not found, id:" + batchPresentationId);
    }
}
