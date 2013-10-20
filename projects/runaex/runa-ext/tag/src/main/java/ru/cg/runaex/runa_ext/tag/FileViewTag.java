package ru.cg.runaex.runa_ext.tag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Locale;
import java.util.regex.Pattern;

import freemarker.template.TemplateModelException;
import org.apache.commons.codec.binary.Base64;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.FileView;

/**
 * @author golovlyev
 */
public class FileViewTag extends BaseFileTag<FileView> {
  private static final long serialVersionUID = 1L;
  private static final Pattern patternImage = Pattern.compile("image");

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILE_VIEW;
  }

  @Override
  protected String executeToHtml(FileView component) throws TemplateModelException {
    super.executeToHtml(component);
    if (component.isDefaultValueSet())
      putReferenceValueAsCurrent(component);

    if (component.getImageWidth() != null)
      html.append(getImage(component.getImageWidth()));
    if (fileName != null && !fileName.isEmpty()) {
      html.append("<a href='#' onclick=\"downloadFile('").append(schema).append("', '").append(table).append("', '")
          .append(getField(component)).append("', ").append(selectedRowId).append(");\">")
          .append(fileName)
          .append("</a>");
    }
    else
      html.append(resourceBundleMessageSource.getMessage("viewFileNotFound", null, Locale.ROOT));

    html.append("</div>");
    return html.toString();
  }

  private String getImage(String imageWidth) {
    String imageTag = "";
    if (contentFile != null && isItImage(contentFile)) {
      imageTag += " <img src=\"data:image/jpg;base64," + new String(new Base64().encode(contentFile)) + "\"";
      imageTag += " style=\"max-width: " + imageWidth + "px\"\"/><br/>";
    }
    return imageTag;
  }

  private boolean isItImage(byte[] fileByte) {
    try {
      String fileType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(fileByte));
      if (fileType != null)
        return patternImage.matcher(fileType).find();
    }
    catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }
}
