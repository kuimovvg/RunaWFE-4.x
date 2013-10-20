package ru.cg.runaex.components.util;

import ru.cg.runaex.components.bean.component.*;
import ru.cg.runaex.components.bean.component.field.*;
import ru.cg.runaex.components.bean.component.filter.*;
import ru.cg.runaex.components.bean.component.grid.*;
import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components.bean.component.part.VisibilityRuleComponentPart;
import ru.cg.runaex.components.bean.component.rule.GroovyRule;

/**
 * @author urmancheev
 */
public final class ComponentUtil {

  public static final String LINK_FLEXI_GRID_TAG_NAME = "LinkFlexiGrid";
  public static final String DEPENDENT_FLEXI_GRID_TAG_NAME = "DependentFlexiGrid";
  public static final String AUTOCOMPLETE_TAG_NAME = "Autocomplete";
  public static final String CHECK_BOX_TAG_NAME = "CheckBox";
  public static final String COMBO_BOX_TAG_NAME = "ComboBox";
  public static final String DATE_TIME_PICKER_TAG_NAME = "DateTimePicker";
  public static final String RADIO_BUTTON_GROUP_TAG_NAME = "RadioButtonGroup";
  public static final String TEXT_FIELD_TAG_NAME = "TextField";
  public static final String FLEXI_GRID_TAG_NAME = "FlexiGrid";
  public static final String TREE_GRID_TAG_NAME = "TreeGrid";
  public static final String EDITABLE_TREE_GRID_TAG_NAME = "EditableTreeGrid";
  public static final String SELECT_TREE_GRID_TAG_NAME = "SelectTreeGrid";
  public static final String FILE_UPLOAD_TAG_NAME = "FileUpload";
  public static final String FILE_VIEW_TAG_NAME = "FileView";
  public static final String HIDDEN_INPUT_TAG_NAME = "HiddenInput";
  public static final String FIASS_ADDRESS_TAG_NAME = "FiasAddress";
  public static final String FILTER_FIELD_TAG_NAME = "FilterField";
  public static final String FILTER_AUTOCOMPLETE_TAG_NAME = "FilterAutocomplete";
  public static final String FILTER_CHECK_BOX_TAG_NAME = "FilterCheckBox";
  public static final String FILTER_COMBO_BOX_TAG_NAME = "FilterComboBox";
  public static final String FILTER_DATE_TIME_PICKER_TAG_NAME = "FilterDateTimePicker";
  public static final String FILTER_RADIO_BUTTON_GROUP_TAG_NAME = "FilterRadioButtonGroup";
  public static final String PRINT_BUTTON_TAG_NAME = "PrintButton";
  public static final String ACTION_BUTTON_TAG_NAME = "ActionButton";
  public static final String NAVIGATE_BUTTON_TAG_NAME = "NavigateButton";
  public static final String HELP_BUTTON_TAG_NAME = "HelpButton";
  public static final String NAVIGATION_TREE_MENU_TAG_NAME = "NavigationTreeMenu";
  public static final String LABEL_TAG_NAME = "Label";
  public static final String SELECT_FLEXI_GRID_TAG_NAME = "SelectFlexiGrid";
  public static final String SPHINX_SEARCH_TAG_NAME = "SphinxSearch";
  public static final String FILTER_FIAS_ADDRESS_TAG_NAME = "FilterFiasAddress";
  public static final String SIGN_VERIFY_TAG_NAME = "SignVerify";
  public static final String SIGN_AND_SAVE_BUTTON_TAG_NAME = "SignAndSaveButton";
  public static final String RECORD_NUMBER_GENERATOR_TAG_NAME = "RecordNumberGenerator";
  public static final String TIMER_TAG_NAME = "Timer";
  public static final String NUMBER_FIELD_TAG_NAME = "NumberField";
  public static final String FILTER_NUMBER_FIELD_TAG_NAME = "FilterNumberField";
  public static final String GROOVY_RULE_TAG_NAME = "GroovyRule";
  public static final String EXECUTE_GROOVY_BUTTON = "ExecuteGroovyButton";

  public static ComponentType getComponentType(String componentName) {
    if (LINK_FLEXI_GRID_TAG_NAME.equals(componentName)) {
      return ComponentType.LINK_FLEXI_GRID;
    }
    else if (DEPENDENT_FLEXI_GRID_TAG_NAME.equals(componentName)) {
      return ComponentType.DEPENDENT_FLEXI_GRID;
    }
    else if (AUTOCOMPLETE_TAG_NAME.equals(componentName)) {
      return ComponentType.AUTOCOMPLETE;
    }
    else if (CHECK_BOX_TAG_NAME.equals(componentName)) {
      return ComponentType.CHECK_BOX;
    }
    else if (COMBO_BOX_TAG_NAME.equals(componentName)) {
      return ComponentType.COMBO_BOX;
    }
    else if (DATE_TIME_PICKER_TAG_NAME.equals(componentName)) {
      return ComponentType.DATE_TIME_PICKER;
    }
    else if (RADIO_BUTTON_GROUP_TAG_NAME.equals(componentName)) {
      return ComponentType.RADIO_BUTTON_GROUP;
    }
    else if (TEXT_FIELD_TAG_NAME.equals(componentName)) {
      return ComponentType.TEXT_FIELD;
    }
    else if (FLEXI_GRID_TAG_NAME.equals(componentName)) {
      return ComponentType.FLEXI_GRID;
    }
    else if (TREE_GRID_TAG_NAME.equals(componentName)) {
      return ComponentType.TREE_GRID;
    }
    else if (EDITABLE_TREE_GRID_TAG_NAME.equals(componentName)) {
      return ComponentType.EDITABLE_TREE_GRID;
    }
    else if (SELECT_TREE_GRID_TAG_NAME.equals(componentName)) {
      return ComponentType.SELECT_TREE_GRID;
    }
    else if (FILE_UPLOAD_TAG_NAME.equals(componentName)) {
      return ComponentType.FILE_UPLOAD;
    }
    else if (FILE_VIEW_TAG_NAME.equals(componentName)) {
      return ComponentType.FILE_VIEW;
    }
    else if (HIDDEN_INPUT_TAG_NAME.equals(componentName)) {
      return ComponentType.HIDDEN_INPUT;
    }
    else if (FIASS_ADDRESS_TAG_NAME.equals(componentName)) {
      return ComponentType.FIAS_ADDRESS;
    }
    else if (FILTER_FIELD_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_FIELD;
    }
    else if (FILTER_AUTOCOMPLETE_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_AUTOCOMPLETE;
    }
    else if (FILTER_CHECK_BOX_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_CHECKBOX;
    }
    else if (FILTER_COMBO_BOX_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_COMBO_BOX;
    }
    else if (FILTER_DATE_TIME_PICKER_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_DATE_TIME_PICKER;
    }
    else if (FILTER_RADIO_BUTTON_GROUP_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_RADIO_BUTTON_GROUP;
    }
    else if (PRINT_BUTTON_TAG_NAME.equals(componentName)) {
      return ComponentType.PRINT_BUTTON;
    }
    else if (ACTION_BUTTON_TAG_NAME.equals(componentName)) {
      return ComponentType.ACTION_BUTTON;
    }
    else if (NAVIGATE_BUTTON_TAG_NAME.equals(componentName)) {
      return ComponentType.NAVIGATE_BUTTON;
    }
    else if (HELP_BUTTON_TAG_NAME.equals(componentName)) {
      return ComponentType.HELP_BUTTON;
    }
    else if (NAVIGATION_TREE_MENU_TAG_NAME.equals(componentName)) {
      return ComponentType.NAVIGATION_TREE_MENU;
    }
    else if (LABEL_TAG_NAME.equals(componentName)) {
      return ComponentType.LABEL;
    }
    else if (SELECT_FLEXI_GRID_TAG_NAME.equals(componentName)) {
      return ComponentType.SELECT_FLEXI_GRID;
    }
    else if (SPHINX_SEARCH_TAG_NAME.equals(componentName)) {
      return ComponentType.SPHINX_SEARCH;
    }
    else if (FILTER_FIAS_ADDRESS_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_FIAS_ADDRESS;
    }
    else if (SIGN_VERIFY_TAG_NAME.equals(componentName)) {
      return ComponentType.SIGN_VERIFY;
    }
    else if (SIGN_AND_SAVE_BUTTON_TAG_NAME.equals(componentName)) {
      return ComponentType.SIGN_AND_SAVE_BUTTON;
    }
    else if (RECORD_NUMBER_GENERATOR_TAG_NAME.equals(componentName))
      return ComponentType.RECORD_NUMBER_GENERATOR;
    else if (TIMER_TAG_NAME.equals(componentName)) {
      return ComponentType.TIMER;
    }
    else if (NUMBER_FIELD_TAG_NAME.equals(componentName)) {
      return ComponentType.NUMBER_FIELD;
    }
    else if (FILTER_NUMBER_FIELD_TAG_NAME.equals(componentName)) {
      return ComponentType.FILTER_NUMBER_FIELD;
    }
    else if (GROOVY_RULE_TAG_NAME.equals(componentName)) {
      return ComponentType.GROOVY_RULE;
    }
    else if (EXECUTE_GROOVY_BUTTON.equals(componentName)) {
      return ComponentType.EXECUTE_GROOVY_BUTTON;
    }
    return ComponentType.UNKNOWN;
  }

  public static Component createComponent(ComponentType type) {
    Component component;

    switch (type) {
      case FLEXI_GRID:
        component = new FlexiGrid();
        break;
      case LINK_FLEXI_GRID:
        component = new LinkFlexiGrid();
        break;
      case DEPENDENT_FLEXI_GRID:
        component = new DependentFlexiGrid();
        break;
      case TREE_GRID:
        component = new TreeGrid();
        break;
      case EDITABLE_TREE_GRID:
        component = new EditableTreeGrid();
        break;
      case SELECT_TREE_GRID:
        component = new SelectTreeGrid();
        break;
      case AUTOCOMPLETE:
        component = new Autocomplete();
        break;
      case CHECK_BOX:
        component = new CheckBox();
        break;
      case COMBO_BOX:
        component = new ComboBox();
        break;
      case DATE_TIME_PICKER:
        component = new DateTimePicker();
        break;
      case RADIO_BUTTON_GROUP:
        component = new RadioButtonGroup();
        break;
      case TEXT_FIELD:
        component = new TextField();
        break;
      case FILE_UPLOAD:
        component = new FileUpload();
        break;
      case FILE_VIEW:
        component = new FileView();
        break;
      case HIDDEN_INPUT:
        component = new HiddenInput();
        break;
      case FIAS_ADDRESS:
        component = new FiasAddress();
        break;
      case FILTER_FIELD:
        component = new FilterField();
        break;
      case FILTER_AUTOCOMPLETE:
        component = new FilterAutocomplete();
        break;
      case FILTER_CHECKBOX:
        component = new FilterCheckbox();
        break;
      case FILTER_COMBO_BOX:
        component = new FilterComboBox();
        break;
      case FILTER_DATE_TIME_PICKER:
        component = new FilterDateTimePicker();
        break;
      case FILTER_RADIO_BUTTON_GROUP:
        component = new FilterRadioButtonGroup();
        break;
      case FILTER_NUMBER_FIELD:
        component = new FilterNumberField();
        break;
      case PRINT_BUTTON:
        component = new PrintButton();
        break;
      case ACTION_BUTTON:
        component = new ActionButton();
        break;
      case SIGN_AND_SAVE_BUTTON:
        component = new SignAndSaveButton();
        break;
      case NAVIGATE_BUTTON:
        component = new NavigateButton();
        break;
      case HELP_BUTTON:
        component = new HelpButton();
        break;
      case NAVIGATION_TREE_MENU:
        component = new NavigationTreeMenu();
        break;
      case LABEL:
        component = new Label();
        break;
      case SELECT_FLEXI_GRID:
        component = new SelectFlexiGrid();
        break;
      case SPHINX_SEARCH:
        component = new SphinxSearch();
        break;
      case FILTER_FIAS_ADDRESS:
        component = new FilterFiasAddress();
        break;
      case SIGN_VERIFY:
        component = new SignVerify();
        break;
      case RECORD_NUMBER_GENERATOR:
        component = new RecordNumberGenerator();
        break;
      case TIMER:
        component = new Timer();
        break;
      case NUMBER_FIELD:
        component = new NumberField();
        break;
      case EXECUTE_GROOVY_BUTTON:
        component = new ExecuteGroovyButton();
        break;
      case GROOVY_RULE:
        component = new GroovyRule();
        break;
      default:
        component = new Component() {
          private static final long serialVersionUID = 302210861071542889L;

          @Override
          public int getParametersNumber() {
            return 0;
          }

          @Override
          public VisibilityRuleComponentPart getVisibilityRule() {
            return VisibilityRuleComponentPart.ALWAYS_TRUE_RULE;
          }

          @Override
          protected int getVisibilityRuleParameterIndex() {
            return -1;
          }
        };
    }

    return component;
  }

  public static String createRequireRule(RequireRuleComponentPart requireRule) {
    String groovyScript = requireRule.getGroovyScript();

    if (groovyScript != null) {
      StringBuilder stringBuilder = new StringBuilder("${");
      stringBuilder.append(ComponentUtil.GROOVY_RULE_TAG_NAME)
          .append("(\"").append(groovyScript).append("\")}");
      return stringBuilder.toString();
    }

    return requireRule.isUnconditionallyRequired() ? "true" : null;
  }

  //todo change in gpd
  public static String replaceCharacters(String value) {
    if (value != null) {
      value = value.replaceAll("&quot;", "\"");
      value = value.replaceAll("&amp;", "&");
      value = value.replaceAll("&gt;", ">");
      value = value.replaceAll("&lt;", "<");
      value = value.replaceAll("&apos;", "'");
    }
    return value;
  }
}
