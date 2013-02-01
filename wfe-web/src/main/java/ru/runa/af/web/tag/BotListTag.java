package ru.runa.af.web.tag;

import java.util.List;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.web.action.DeleteBotAction;
import ru.runa.af.web.html.BotTableBuilder;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationPermission;

/**
 * @author petrmikheev
 * @jsp.tag name = "botListTag" body-content = "JSP"
 */
public class BotListTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;
    private Long botStationID;

    public void setBotStationID(Long botStationID) {
        this.botStationID = botStationID;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getBotStationID() {
        return botStationID;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        tdFormElement.addElement(new Input(Input.hidden, IdsForm.ID_INPUT_NAME, Long.toString(botStationID)));
        List<Bot> bots = Delegates.getBotService().getBots(getUser(), botStationID);
        tdFormElement.addElement(new BotTableBuilder(pageContext).buildBotTable(bots));
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_BOT_LIST, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    public String getAction() {
        return DeleteBotAction.DELETE_BOT_ACTION_PATH;
    }

    @Override
    public boolean isFormButtonEnabled() {
        try {
            AuthorizationService authorizationService = Delegates.getAuthorizationService();
            return authorizationService.isAllowed(getUser(), BotStationPermission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_BOT_PARAMETER;
    }
}
