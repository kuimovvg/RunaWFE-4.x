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
package ru.runa.common.web.tag;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.Profile;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.ChangeActiveBatchPresentationAction;
import ru.runa.common.web.action.HideableBlockAction;
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.common.web.form.ReturnActionForm;

/**
 * Created 18.05.2005
 * 
 * @jsp.tag name = "viewControlsHideableBlock" body-content = "JSP"
 */
public class ViewControlsHideableBlockTag extends HideableBlockTag {
    private static final long serialVersionUID = -4644961104278379700L;

    public String getHideTitle() {
        return Messages.getMessage(Messages.LABEL_HIDE_CONTROLS, pageContext);
    }

    public boolean isShowManagingBatchPresentationBlock() {
        return true;
    }

    public String getShowTitle() {
        return Messages.getMessage(Messages.LABEL_SHOW_CONTROLS, pageContext);
    }

    public String getAction() {
        return HideableBlockAction.ACTION_PATH;
    }

    public void addEndOptionalContent(TD td, boolean isVisible) throws JspException {
        td.addElement(Entities.NBSP);
        Select select = new Select(BatchPresentationForm.BATCH_PRESENTATION_NAME);
        try {
            Profile profile = ProfileHttpSessionHelper.getProfile(pageContext.getSession());
            BatchPresentation[] batchPresentations = profile.getBatchPresentations(getHideableBlockId());
            BatchPresentation activeBatchPresentation = profile.getActiveBatchPresentation(getHideableBlockId());

            for (int i = 0; i < batchPresentations.length; i++) {
                String batchPresentationName = batchPresentations[i].getBatchPresentationName();
                Option option = new Option();
                Map<String, String> params = new HashMap<String, String>();
                params.put(BatchPresentationForm.BATCH_PRESENTATION_ID, activeBatchPresentation.getBatchPresentationId());
                params.put(BatchPresentationForm.BATCH_PRESENTATION_NAME, batchPresentations[i].getBatchPresentationName());
                params.put(ReturnActionForm.RETURN_ACTION, getReturnAction());
                String actionUrl = Commons.getActionUrl(ChangeActiveBatchPresentationAction.ACTION_PATH, params, pageContext, PortletUrl.Action);
                option.setValue(actionUrl);
                if (batchPresentations[i].isDefault()) {
                    option.addElement(Messages.getMessage(batchPresentationName, pageContext));
                } else {
                    option.addElement(batchPresentationName);
                }
                if (batchPresentationName.equals(activeBatchPresentation.getBatchPresentationName())) {
                    option.setSelected(true);
                }
                select.addElement(option);
            }
        } catch (BatchPresentationNotFoundException e) {
            throw new JspException(e);
        }
        select.setOnChange("document.location=this.options[this.selectedIndex].value");
        td.addElement(select);
        td.addElement(Entities.NBSP);
    }
}
