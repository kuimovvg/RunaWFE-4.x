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
package ru.runa.af.web.html;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.Bot;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;

/**
 * @author petrmikheev
 */
public class BotTableBuilder {

    private final PageContext pageContext;

    public BotTableBuilder(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public Table buildBotTable(List<Bot> bots) throws JspException {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setWidth("100%");
        table.addElement(createTableHeaderTR());
        for (Iterator<Bot> iterator = bots.iterator(); iterator.hasNext();) {
            table.addElement(createTR(iterator.next()));
        }
        return table;
    }

    private TR createTR(Bot bot) throws JspException {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        Input input = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, Long.toString(bot.getId()));
        input.setChecked(false);
        String path = Commons.getActionUrl("bot.do", "botID", new Long(bot.getId()), pageContext, PortletUrl.Render);
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        //tr.addElement(new TD(new A(path, new Long(bot.getId()).toString()).toString()).setWidth("10%").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new A(path, bot.getWfeUser())).setWidth("90%").setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private TR createTableHeaderTR() {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TD("").setClass(Resources.CLASS_LIST_TABLE_TD));
        //        tr.addElement(new TD("id").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_BOT_WFE_USER, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }
}
