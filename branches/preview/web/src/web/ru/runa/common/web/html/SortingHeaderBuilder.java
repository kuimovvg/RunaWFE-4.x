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
package ru.runa.common.web.html;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.ClassPresentation;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.af.presentation.FieldState;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.SetSortingAction;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.common.web.form.SetSortingForm;
import ru.runa.commons.ArraysCommons;

/**
 * 
 * Created on 17.11.2005
 * 
 */
public class SortingHeaderBuilder implements HeaderBuilder {
    private final BatchPresentation batchPresentation;

    private final PageContext pageContext;

    private final String returnActionName;

    private final String[] prefixNames;

    private final String[] suffixNames;

    private void fillHeaderTR(FieldDescriptor[] fields, Map sortedFieldsIdModeMap, TR tr) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].displayName.startsWith(ClassPresentation.editable_prefix) || fields[i].fieldState != FieldState.ENABLED) {
                continue;
            }

            TH header = new TH();
            tr.addElement(header);
            header.setClass(Resources.CLASS_LIST_TABLE_TH);
            IMG sortingImage = null;
            Boolean sortingMode = (Boolean) sortedFieldsIdModeMap.get(new Integer(fields[i].fieldIdx));
            if (sortingMode != null) {//i.e. there is at least on sorting field
                if (sortingMode.booleanValue() == BatchPresentationConsts.ASC) {
                    sortingImage = new IMG(Commons.getUrl(Resources.SORT_ASC_IMAGE, pageContext, PortletUrl.Resource));
                    sortingImage.setAlt(Resources.SORT_ASC_ALT);
                } else {
                    sortingImage = new IMG(Commons.getUrl(Resources.SORT_DESC_IMAGE, pageContext, PortletUrl.Resource));
                    sortingImage.setAlt(Resources.SORT_DESC_ALT);
                }
            }

            Map<String, String> params = new HashMap<String, String>();
            params.put(SetSortingForm.BATCH_PRESENTATION_ID, batchPresentation.getBatchPresentationId());
            params.put(IdForm.ID_INPUT_NAME, String.valueOf(fields[i].fieldIdx));
            params.put(ReturnActionForm.RETURN_ACTION, returnActionName);
            if (fields[i].isSortable) {
                String url = Commons.getActionUrl(SetSortingAction.ACTION_PATH, params, pageContext, PortletUrl.Action);
                A link = new A(url, getDisplayString(fields[i]));
                header.addElement(link);
                if (sortingImage != null) {
                    header.addElement(Entities.NBSP);
                    header.addElement(sortingImage);
                }
            } else {
                header.addElement(getDisplayString(fields[i]));
            }
        }
    }

    private String getDisplayString(FieldDescriptor field) {
        if (field.displayName.startsWith(ClassPresentation.removable_prefix)) {
            return field.displayName.substring(field.displayName.lastIndexOf(':') + 1);
        } else if (field.displayName.startsWith(ClassPresentation.filterable_prefix)) {
            return Messages.getMessage(field.displayName.substring(field.displayName.lastIndexOf(':') + 1), pageContext);
        } else {
            return Messages.getMessage(field.displayName, pageContext);
        }
    }

    public TR build() {
        FieldDescriptor[] sortingFields = batchPresentation.getSortedFields();
        boolean[] sortingModes = batchPresentation.getFieldsToSortModes();
        Map<Integer, Boolean> sortedFieldsIdModeMap = new HashMap<Integer, Boolean>();
        for (int i = 0; i < sortingFields.length; i++) {
            sortedFieldsIdModeMap.put(new Integer(sortingFields[i].fieldIdx), new Boolean(sortingModes[i]));
        }
        TR tr = new TR();
        createCells(tr, createEmptySrtings(getAditionalNumberOfPrefixEmptyCells()));
        createCells(tr, prefixNames);
        fillHeaderTR(batchPresentation.getDisplayFields(), sortedFieldsIdModeMap, tr);
        createCells(tr, suffixNames);
        return tr;
    }

    public SortingHeaderBuilder(BatchPresentation batchPresentation, int numberOfPrefixCells, int numberOfSuffixCells, String returnActionName,
            PageContext pageContext) {
        this(batchPresentation, createEmptySrtings(numberOfPrefixCells), createEmptySrtings(numberOfSuffixCells), returnActionName, pageContext);
    }

    private static String[] createEmptySrtings(int size) {
        return (String[]) ArraysCommons.fillArray(new String[size], Entities.NBSP);
    }

    public SortingHeaderBuilder(BatchPresentation batchPresentation, String[] prefixNames, String[] suffixNames, String returnActionName,
            PageContext pageContext) {
        this.batchPresentation = batchPresentation;
        this.prefixNames = prefixNames;
        this.suffixNames = suffixNames;
        this.returnActionName = returnActionName;
        this.pageContext = pageContext;
    }

    private int getAditionalNumberOfPrefixEmptyCells() {
        return batchPresentation.getGrouppedFields().length;
    }

    private void createCells(TR tr, String[] names) {
        for (int i = 0; i < names.length; i++) {
            TH header = new TH();
            header.addElement(names[i]);
            tr.addElement(header);
            if (names[i] == Entities.NBSP) {
                header.setClass(Resources.CLASS_EMPTY20_TABLE_TD);
            } else {
                header.setClass(Resources.CLASS_LIST_TABLE_TH);
            }
        }
    }
}
