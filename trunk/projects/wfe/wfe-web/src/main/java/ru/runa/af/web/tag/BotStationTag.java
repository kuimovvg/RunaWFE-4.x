package ru.runa.af.web.tag;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.UpdateBotStationAction;
import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationPermission;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

/**
 * @author petrmikheev
 * @jsp.tag name = "botStationTag" body-content = "JSP"
 */
public class BotStationTag extends TitledFormTag {
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
    protected void fillFormElement(TD tdFormElement) throws JspException {
        BotStation botStation = Delegates.getBotService().getBotStation(botStationID);
        Table table = new Table();
        Input nameInput = new Input(Input.TEXT, BotStationForm.BOT_STATION_NAME, botStation.getName());
        String address = botStation.getAddress() != null ? botStation.getAddress() : "";
        Input botStationAddressInput = new Input(Input.TEXT, BotStationForm.BOT_STATION_RMI_ADDRESS, address);

        Input hiddenBotStationID = new Input(Input.HIDDEN, BotStationForm.BOT_STATION_ID, String.valueOf(botStationID));
        tdFormElement.addElement(hiddenBotStationID);

        TR tr = new TR();
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_BOT_STATION_NAME, pageContext)));
        tr.addElement(new TD(nameInput));
        table.addElement(tr);
        tr = new TR();
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_BOT_STATION_ADDRESS, pageContext)));
        tr.addElement(new TD(botStationAddressInput));
        table.addElement(tr);
        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_BOT_STATION_DETAILS, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    @Override
    public String getButtonAlignment() {
        return "right";
    }

    @Override
    public String getAction() {
        return UpdateBotStationAction.UPDATE_BOT_STATION_ACTION_PATH;
    }

    @Override
    public boolean isFormButtonEnabled() throws JspException {
        boolean result = false;
        try {
            AuthorizationService authorizationService = Delegates.getAuthorizationService();
            result = authorizationService.isAllowed(getSubject(), BotStationPermission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
        } catch (AuthorizationException e) {
            throw new JspException(e);
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
        return result;
    }
}
