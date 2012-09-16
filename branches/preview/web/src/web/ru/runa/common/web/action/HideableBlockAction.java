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
package ru.runa.common.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.presentation.Profile;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.HideableBlockForm;

/**
 * Created on 26.01.2005
 * 
 * @struts:action path="/hideableBlock" name="hideableBlockForm" validate="false"
 */
public class HideableBlockAction extends Action {

    public static final String ACTION_PATH = "/hideableBlock";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HideableBlockForm hideableBlockForm = (HideableBlockForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        profile.changeBlockVisibility(hideableBlockForm.getHideableBlockId());
        String url = hideableBlockForm.getReturnAction();
        // http://sourceforge.net/tracker/?func=detail&aid=3461404&group_id=125156&atid=701698
        return new ActionForward(url, false);
    }
}
