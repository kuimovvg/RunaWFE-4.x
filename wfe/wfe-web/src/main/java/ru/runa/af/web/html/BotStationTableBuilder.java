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

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * User: stan79 Date: 29.05.2008 Time: 12:35:18 $Id
 */
public class BotStationTableBuilder {

    private final PageContext pageContext;

    public BotStationTableBuilder(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public Table buildBotStationTable(List<BotStation> botStations) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setWidth("100%");
        table.addElement(createTableHeaderTR());
        for (Iterator<BotStation> botStationIterator = botStations.iterator(); botStationIterator.hasNext();) {
            BotStation botStation = botStationIterator.next();
            table.addElement(createTR(botStation));
        }
        return table;
    }

    private TR createTR(BotStation botStation) {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        Input input = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, Long.toString(botStation.getId()));
        input.setChecked(false);
        String path = Commons.getActionUrl("bot_station.do", "botStationId", new Long(botStation.getId()), pageContext, PortletUrlType.Render);
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new A(path, new Long(botStation.getId()).toString())).setWidth("10%").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new A(path, botStation.getName())).setWidth("40%").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new A(path, botStation.getAddress())).setWidth("50%").setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private TR createTableHeaderTR() {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TH("").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH("id").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH(Messages.getMessage(Messages.LABEL_BOT_STATION_NAME, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH(Messages.getMessage(Messages.LABEL_BOT_STATION_ADDRESS, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    public static Table createBotStationDetailsTable(PageContext pageContext, String name, String address) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        Input nameInput = new Input(Input.TEXT, BotStationForm.BOT_STATION_NAME, name);
        Input botStationRMIAddress = new Input(Input.TEXT, BotStationForm.BOT_STATION_RMI_ADDRESS, address);
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_BOT_STATION_NAME, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(nameInput).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(tr);
        tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_BOT_STATION_ADDRESS, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(botStationRMIAddress).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(tr);
        return table;
    }

}
