package ru.runa.wfe.commons;

import java.util.Map;

import ru.runa.wfe.handler.action.BSHActionHandler;
import ru.runa.wfe.handler.action.CreateOptionActionHandler;
import ru.runa.wfe.handler.action.DebugActionHandler;
import ru.runa.wfe.handler.action.EmailTaskNotifierActionHandler;
import ru.runa.wfe.handler.action.EscalationActionHandler;
import ru.runa.wfe.handler.action.GroovyActionHandler;
import ru.runa.wfe.handler.action.SQLActionHandler;
import ru.runa.wfe.handler.action.SendEmailActionHandler;
import ru.runa.wfe.handler.action.user.ActorNameActionHandler;
import ru.runa.wfe.handler.action.user.AssignSwimlaneActionHandler;
import ru.runa.wfe.handler.action.var.AddObjectToListActionHandler;
import ru.runa.wfe.handler.action.var.ClearVariableActionHandler;
import ru.runa.wfe.handler.action.var.ConvertMapKeysToListActionHandler;
import ru.runa.wfe.handler.action.var.ConvertMapValuesToListActionHandler;
import ru.runa.wfe.handler.action.var.FormulaActionHandler;
import ru.runa.wfe.handler.action.var.GetObjectFromListActionHandler;
import ru.runa.wfe.handler.action.var.GetObjectFromMapActionHandler;
import ru.runa.wfe.handler.action.var.ListAggregateFunctionActionHandler;
import ru.runa.wfe.handler.action.var.MapAggregateFunctionActionHandler;
import ru.runa.wfe.handler.action.var.MergeMapsActionHandler;
import ru.runa.wfe.handler.action.var.PutObjectToMapActionHandler;
import ru.runa.wfe.handler.action.var.RemoveObjectFromListActionHandler;
import ru.runa.wfe.handler.action.var.RemoveObjectFromMapActionHandler;
import ru.runa.wfe.handler.action.var.SetObjectToListActionHandler;
import ru.runa.wfe.handler.action.var.SortListActionHandler;
import ru.runa.wfe.handler.assign.DefaultAssignmentHandler;
import ru.runa.wfe.handler.decision.BSFDecisionHandler;
import ru.runa.wfe.os.func.DemoChiefFunction;
import ru.runa.wfe.os.func.ExecutorByNameFunction;
import ru.runa.wfe.os.func.GetActorsByCodesFunction;
import ru.runa.wfe.os.func.SQLChiefFunction;
import ru.runa.wfe.os.func.SQLChiefRecursiveFunction;
import ru.runa.wfe.os.func.SQLDirectorFunction;
import ru.runa.wfe.os.func.SQLFunction;
import ru.runa.wfe.os.func.SQLSubordinateFunction;
import ru.runa.wfe.os.func.SQLSubordinateRecursiveFunction;

import com.google.common.collect.Maps;

/**
 * This class contains substitution for loaded classes (back compatibility on
 * class loading).
 * 
 * @author dofs
 * @since 4.0
 */
public class BackCompatibilityClassNames {
    private static Map<String, String> bcc = Maps.newHashMap();
    static {
        // formats renamed
        bcc.put("ru.runa.web.formgen.format.DoubleFormat", "ru.runa.wfe.var.format.DoubleFormat");
        bcc.put("org.jbpm.web.formgen.format.DoubleFormat", "ru.runa.wfe.var.format.DoubleFormat");
        bcc.put("org.jbpm.web.formgen.format.DefaultFormat", "ru.runa.wfe.var.format.StringFormat");
        bcc.put("ru.runa.wf.web.forms.format.ArrayListFormat", "ru.runa.wfe.var.format.ListFormat");
        bcc.put("ru.runa.wf.web.forms.format.BooleanFormat", "ru.runa.wfe.var.format.BooleanFormat");
        bcc.put("ru.runa.wf.web.forms.format.DateFormat", "ru.runa.wfe.var.format.DateFormat");
        bcc.put("ru.runa.wf.web.forms.format.DateTimeFormat", "ru.runa.wfe.var.format.DateTimeFormat");
        bcc.put("ru.runa.wf.web.forms.format.DoubleFormat", "ru.runa.wfe.var.format.DoubleFormat");
        bcc.put("ru.runa.wf.web.forms.format.FileFormat", "ru.runa.wfe.var.format.FileFormat");
        bcc.put("ru.runa.wf.web.forms.format.LongFormat", "ru.runa.wfe.var.format.LongFormat");
        bcc.put("ru.runa.wf.web.forms.format.StringArrayFormat", "ru.runa.wfe.var.format.ListFormat");
        bcc.put("ru.runa.wf.web.forms.format.StringFormat", "ru.runa.wfe.var.format.StringFormat");
        bcc.put("ru.runa.wf.web.forms.format.TimeFormat", "ru.runa.wfe.var.format.TimeFormat");
        // assignment handler renamed
        bcc.put("ru.runa.wf.jbpm.delegation.assignment.AssignmentHandler", DefaultAssignmentHandler.class.getName());
        // decision handler renamed
        bcc.put("ru.runa.wf.jbpm.delegation.decision.BSFDecisionHandler", BSFDecisionHandler.class.getName());
        // action handlers renamed
        bcc.put("ru.runa.wf.jbpm.delegation.action.SetSubProcessPermissionsActionHandler", DebugActionHandler.class.getName());
        bcc.put("ru.runa.wf.jbpm.delegation.action.BotInvokerActionHandler", "ru.runa.service.bot.handler.BotInvokerActionHandler");
        bcc.put("ru.runa.wfe.bp.commons.ExecuteFormulaActionHandler", FormulaActionHandler.class.getName());
        bcc.put("ru.runa.wf.swimlane.AssignSwimlaneActionHandler", AssignSwimlaneActionHandler.class.getName());
        bcc.put("ru.runa.wf.users.ActorNameActionHandler", ActorNameActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.AddObjectToListActionHandler", AddObjectToListActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.SetObjectToListActionHandler", SetObjectToListActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.GetObjectFromListActionHandler", GetObjectFromListActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.ListAggregateFunctionActionHandler", ListAggregateFunctionActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.GetObjectFromMapActionHandler", GetObjectFromMapActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.PutObjectToMapActionHandler", PutObjectToMapActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.RemoveObjectFromMapActionHandler", RemoveObjectFromMapActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.MapAggregateFunctionActionHandler", MapAggregateFunctionActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.ConvertMapKeysToListActionHandler", ConvertMapKeysToListActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.ConvertMapValuesToListActionHandler", ConvertMapValuesToListActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.MergeMapsActionHandler", MergeMapsActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.ClearVariableActionHandler", ClearVariableActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.RemoveObjectFromListActionHandler", RemoveObjectFromListActionHandler.class.getName());
        bcc.put("ru.runa.wf.var.SortListActionHandler", SortListActionHandler.class.getName());
        bcc.put("ru.runa.wf.BSHActionHandler", BSHActionHandler.class.getName());
        bcc.put("ru.runa.wf.CreateOptionActionHandler", CreateOptionActionHandler.class.getName());
        bcc.put("ru.runa.wf.EmailTaskNotifierActionHandler", EmailTaskNotifierActionHandler.class.getName());
        bcc.put("ru.runa.wf.EscalationActionHandler", EscalationActionHandler.class.getName());
        bcc.put("ru.runa.wf.GroovyActionHandler", GroovyActionHandler.class.getName());
        bcc.put("ru.runa.wf.SendEmailActionHandler", SendEmailActionHandler.class.getName());
        bcc.put("ru.runa.wf.SQLActionHandler", SQLActionHandler.class.getName());
        // org functions renamed
        bcc.put("ru.runa.af.organizationfunction.DemoChiefFunction", DemoChiefFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.ExecutorByCodeFunction", GetActorsByCodesFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.ExecutorByNameFunction", ExecutorByNameFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.SQLFunction", SQLFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.ChiefFunction", SQLChiefFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.ChiefRecursiveFunction", SQLChiefRecursiveFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.DirectorFunction", SQLDirectorFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.SubordinateFunction", SQLSubordinateFunction.class.getName());
        bcc.put("ru.runa.af.organizationfunction.SubordinateRecursiveFunction", SQLSubordinateRecursiveFunction.class.getName());
        // custom html tags
        bcc.put("ru.runa.wf.web.html.vartag.GroupMembersAutoCompletionVarTag", "ru.runa.wf.web.customtag.impl.GroupMembersAutoCompletionVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.DemoSubordinateAutoCompletingComboboxVarTag",
                "ru.runa.wf.web.customtag.impl.DemoSubordinateAutoCompletingComboboxVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.ActorComboboxVarTag", "ru.runa.wf.web.customtag.impl.ActorComboboxVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.GroupMembersComboboxVarTag", "ru.runa.wf.web.customtag.impl.GroupMembersComboboxVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.ActorFullNameDisplayVarTag", "ru.runa.wf.web.customtag.impl.ActorFullNameDisplayVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.ActorNameDisplayVarTag", "ru.runa.wf.web.customtag.impl.ActorNameDisplayVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.DateInputVarTag", "ru.runa.wf.web.customtag.impl.DateInputVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.DateTimeInputVarTag", "ru.runa.wf.web.customtag.impl.DateTimeInputVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.DateTimeValueDisplayVarTag", "ru.runa.wf.web.customtag.impl.DateTimeValueDisplayVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.DateValueDisplayVarTag", "ru.runa.wf.web.customtag.impl.DateValueDisplayVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.FileVariableValueDownloadVarTag", "ru.runa.wf.web.customtag.impl.FileVariableValueDownloadVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.TimeValueDisplayVarTag", "ru.runa.wf.web.customtag.impl.TimeValueDisplayVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.TimeInputVarTag", "ru.runa.wf.web.customtag.impl.TimeInputVarTag");
        bcc.put("ru.runa.wf.web.html.vartag.VariableValueDisplayVarTag", "ru.runa.wf.web.customtag.impl.VariableValueDisplayVarTag");
    }

    /**
     * Gets back-compatible class name if found.
     * 
     * @param className
     *            original class name
     * @return adjusted class name or original class name
     */
    public static String getClassName(String className) {
        if (bcc.containsKey(className)) {
            return bcc.get(className);
        }
        return className;
    }
}
