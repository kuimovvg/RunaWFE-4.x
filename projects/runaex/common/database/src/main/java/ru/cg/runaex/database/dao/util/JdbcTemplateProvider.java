package ru.cg.runaex.database.dao.util;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author urmancheev
 */
public interface JdbcTemplateProvider {

  JdbcTemplate getMetadataTemplate();

  JdbcTemplate getTemplate(Long processDefinitionId);
}
