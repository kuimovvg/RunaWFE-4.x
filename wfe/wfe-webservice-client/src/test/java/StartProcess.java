import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.WfVariable;

public class StartProcess {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutionAPI executionAPI = new ExecutionWebService().getExecutionAPIPort();
            List<WfVariable> variables = new ArrayList<WfVariable>();

            WfVariable variable1 = new WfVariable();
            variable1.setName("var1");
            GregorianCalendar c = new GregorianCalendar();
            variable1.setDateValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
            variables.add(variable1);

            WfVariable variable2 = new WfVariable();
            variable2.setName("Переменная1");
            variable2.setStringValue("СТРОКА");
            variables.add(variable2);

            WfVariable variable3 = new WfVariable();
            variable3.setName("Переменная2");
            variable3.setLongValue(-3L);
            variables.add(variable3);

            Long processId = executionAPI.startProcessWS(user, "ESCALATIONTEST", variables);
            System.out.println(processId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
