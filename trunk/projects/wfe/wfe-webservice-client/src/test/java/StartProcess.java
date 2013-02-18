import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.VariableDefinition;
import ru.runa.wfe.webservice.WfVariable;

public class StartProcess {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutionAPI executionAPI = new ExecutionWebService().getExecutionAPIPort();
            List<WfVariable> variables = new ArrayList<WfVariable>();

            WfVariable variable1 = new WfVariable();
            variable1.setDefinition(new VariableDefinition());
            variable1.getDefinition().setName("var1");
            variable1.getDefinition().setFormatClassName("ru.runa.wfe.var.format.DateTimeFormat");
            variable1.setValue("14.02.2013 20:20");
            variables.add(variable1);

            WfVariable variable2 = new WfVariable();
            variable2.setDefinition(new VariableDefinition());
            variable2.getDefinition().setName("Переменная1");
            variable2.setValue("СТРОКА");
            variables.add(variable2);

            WfVariable variable3 = new WfVariable();
            variable3.setDefinition(new VariableDefinition());
            variable3.getDefinition().setName("Переменная2");
            variable3.setValue(-3L);
            variables.add(variable3);

            Long processId = executionAPI.startProcessWS(user, "ESCALATIONTEST", variables);
            System.out.println(processId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
