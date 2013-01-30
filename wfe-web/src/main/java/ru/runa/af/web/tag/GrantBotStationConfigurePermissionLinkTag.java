package ru.runa.af.web.tag;

import javax.servlet.jsp.JspException;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationPermission;

/**
 * User: stan79 Date: 27.05.2008 Time: 12:38:49
 * 
 * @jsp.tag name = "grantBotStationConfigurePermissionLink" body-content = "empty"
 */
public class GrantBotStationConfigurePermissionLinkTag extends LinkTag {

    private static final long serialVersionUID = 8676461606886894804L;

    @Override
    protected boolean isLinkEnabled() throws JspException {
        try {
            AuthorizationService authorizationService = Delegates.getAuthorizationService();
            return authorizationService.isAllowed(getUser(), BotStationPermission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.BUTTON_BOT_STATION_CONFIGURE_PERMISSION, pageContext);
    }

}
