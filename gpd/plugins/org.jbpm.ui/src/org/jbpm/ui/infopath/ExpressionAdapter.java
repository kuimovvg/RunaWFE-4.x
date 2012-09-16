package ru.runa.bpm.ui.infopath;

import ru.runa.bpm.ui.validation.ValidatorConfig;

public interface ExpressionAdapter {

    ValidatorConfig createConfig(String expr, String errorMessage) throws Exception;

}
