package ru.runa.bpm.ui.infopath;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.validation.ValidatorConfig;

public class ValidationMapper {
    private static String namespace;

    private static String rootGroup;

    private static Map<String, Map<String, ValidatorConfig>> validators;

    private static Map<Pattern, ExpressionAdapter> expressionAdapters = new HashMap<Pattern, ExpressionAdapter>();
    static {
        // is present
        registerAdapter("\\.", new RequiredExpressionAdapter());
        // is not blank
        registerAdapter("\\. != \"\"", new RequiredExpressionAdapter());
        // less and greater than
        registerAdapter(".* [<>] \\d*", new StringLengthExpressionAdapter());
    }

    private static void registerAdapter(String patternString, ExpressionAdapter adapter) {
        try {
            expressionAdapters.put(Pattern.compile(patternString), adapter);
        } catch (Throwable e) {
            DesignerLogger.logError("[InfoPath Validation] Failed to register adapter " + adapter.getClass(), e);
        }
    }

    public static void init(Map<String, Map<String, ValidatorConfig>> validators, String namespace, String rootGroup) {
        ValidationMapper.validators = validators;
        ValidationMapper.namespace = namespace;
        ValidationMapper.rootGroup = rootGroup;
    }

    public static void apply(String match, String expressionContext, String expression, String errorMessage) {
        String[] exprs = expression.split("and");

        String variableName;

        String groupXPath = "/" + namespace + ":" + rootGroup;
        if (groupXPath.equals(match)) {
            // we are having /my:myFields | my:field1 | ${expr}
            variableName = expression.substring(namespace.length() + 1);
        } else {
            // we are having /my:myFields/my:field1 | . | ${expr}
            variableName = match.substring(groupXPath.length() + namespace.length() + 2);
        }

        for (String expr : exprs) {
            ExpressionAdapter adapter = null;
            for (Pattern pattern : expressionAdapters.keySet()) {
                Matcher m = pattern.matcher(expr);
                if (m.matches()) {
                    adapter = expressionAdapters.get(pattern);
                    break;
                }
            }
            if (adapter == null) {
                DesignerLogger.logInfo("[InfoPath Validation DEBUG] : Not found adapter for expression '" + expr + "'");
            } else {
                try {
                    DesignerLogger.logInfo("[InfoPath Validation DEBUG] : Applying adapter " + adapter.getClass() + " to expression '" + expr + "'");
                    ValidatorConfig config = adapter.createConfig(expr, errorMessage);
                    if (validators.containsKey(variableName)) {
                        validators.get(variableName).put(config.getType(), config);
                    } else {
                        DesignerLogger.logInfo("[InfoPath Validation DEBUG] : Variable not found: " + variableName);
                    }
                } catch (Throwable e) {
                    DesignerLogger.logError("[InfoPath Validation] Failed to apply validation: " + adapter.getClass(), e);
                }
            }
        }
    }

}
