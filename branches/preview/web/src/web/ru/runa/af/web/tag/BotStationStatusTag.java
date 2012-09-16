package ru.runa.af.web.tag;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.BotStation;
import ru.runa.af.BotStationConfigurePermission;
import ru.runa.af.delegate.bot.impl.BotInvokerServiceDelegateRemoteImpl;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.BotsService;
import ru.runa.af.web.action.StartPeriodicBotsInvocationAction;
import ru.runa.af.web.action.StopPeriodicBotsInvocationAction;
import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.web.tag.GrantReadPermissionOnProcessInstanceLinkTag;

/**
 * @author petrmikheev
 * @jsp.tag name = "botStationStatusTag" body-content = "JSP"
 */
public class BotStationStatusTag extends TitledFormTag {
    private static final Log log = LogFactory.getLog(GrantReadPermissionOnProcessInstanceLinkTag.class);
    private static final long serialVersionUID = 1920713038009470026L;

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

    private boolean stationOn = false;
    private boolean periodicInvocationOn = false;

    private void renewValues() throws JspException {
        BotStation botStation = findBotStation();
        if (botStation == null) {
            throw new JspException();
        }
        BotInvokerServiceDelegateRemoteImpl botInvokerService = new BotInvokerServiceDelegateRemoteImpl(botStation.getAddress());
        try {
            periodicInvocationOn = botInvokerService.isRunning();
            stationOn = true;
        } catch (Exception e) {
            stationOn = false;
            periodicInvocationOn = false;
        }
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        renewValues();
        Input hiddenBotStationID = new Input(Input.HIDDEN, BotStationForm.BOT_STATION_ID, botStationID);
        tdFormElement.addElement(hiddenBotStationID);
        Table table = new Table();
        TR tr = new TR();
        tr.addElement(new TD(stationOn ? Messages.getMessage(Messages.MESSAGE_BOTSTATION_ON, pageContext) : Messages.getMessage(
                Messages.MESSAGE_BOTSTATION_OFF, pageContext)));
        table.addElement(tr);
        if (stationOn) {
            tr = new TR();
            tr.addElement(new TD(periodicInvocationOn ? Messages.getMessage(Messages.MESSAGE_PERIODIC_BOTS_INVOCATION_ON, pageContext) : Messages
                    .getMessage(Messages.MESSAGE_PERIODIC_BOTS_INVOCATION_OFF, pageContext)));
            table.addElement(tr);
        }
        tdFormElement.addElement(table);
    }

    private BotStation findBotStation() {
        BotsService botsService = DelegateFactory.getInstance().getBotsService();
        try {
            return botsService.getBotStation(getSubject(), new BotStation(botStationID));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_BOT_STATION_STATUS, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        if (periodicInvocationOn) {
            return Messages.getMessage(Messages.BUTTON_STOP_PERIODIC_BOTS_INVOCATION, pageContext);
        } else {
            return Messages.getMessage(Messages.BUTTON_START_PERIODIC_BOTS_INVOCATION, pageContext);
        }
    }

    @Override
    public String getButtonAlignment() {
        return "left";
    }

    @Override
    public String getAction() {
        if (periodicInvocationOn) {
            return StopPeriodicBotsInvocationAction.STOP_PERIODIC_BOTS_INVOCATION;
        } else {
            return StartPeriodicBotsInvocationAction.START_PERIODIC_BOTS_INVOCATION;
        }
    }

    @Override
    public boolean isFormButtonEnabled() throws JspException {
        boolean result = false;
        try {
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            result = authorizationService.isAllowed(getSubject(), BotStationConfigurePermission.BOT_STATION_CONFIGURE, BotStation.SECURED_INSTANCE);
        } catch (Exception e) {
            throw new JspException(e);
        }
        if (!result) {
            return false;
        }
        renewValues();
        return stationOn;
    }

}
