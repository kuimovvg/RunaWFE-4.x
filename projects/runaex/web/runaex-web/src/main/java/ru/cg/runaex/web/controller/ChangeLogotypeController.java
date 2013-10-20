package ru.cg.runaex.web.controller;

import com.cg.jul.core.resources.properties.service.PropertiesSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import ru.cg.runaex.web.utils.SessionUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * @author golovlyev
 */
@Controller
public class ChangeLogotypeController extends BaseController {

  private final Logger logger = LoggerFactory.getLogger(ChangeLogotypeController.class);

  private final static String JPG_FORMAT = "jpg";

  @Autowired
  @Qualifier("cachedPropertiesSource")
  private PropertiesSource propertiesSource;

  private Properties properties;

  @Autowired
  private ServletContext servletContext;

  /**
   * Go to the change logo form
   */
  @RequestMapping(value = "/changeLogotype", method = RequestMethod.GET)
  public ModelAndView changeLogotype() throws IOException {
    this.properties = propertiesSource.getProperties("/ru/cg/runaex/web/messages/config.xml");
    String projectName = SessionUtils.getProjectName();
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "change_logotype");
    mv.addObject("logo", projectName);
    return mv;
  }

  /**
   * Save logo file
   */
  @RequestMapping(value = "/uploadLogotype", method = RequestMethod.POST)
  public ModelAndView uploadLogotype(MultipartHttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(value = "logo", required = false) String projectName) throws IOException {
    FileOutputStream fileOutputStream = null;
    BufferedWriter out = null;
    InputStream in = null;
    try {
      List<MultipartFile> logoFiles = request.getFiles("logotype");
      removeEmptyFiles(logoFiles);
      File directory = new File(SessionUtils.pathToDir);
      if (!directory.exists())
        directory.mkdir();

      for (MultipartFile logofile : logoFiles) {
        String originalName = logofile.getOriginalFilename();
          if (!(originalName.endsWith(JPG_FORMAT) ||
            originalName.endsWith("png") ||
            originalName.endsWith("bmp") ||
            originalName.endsWith("gif")))
          return getErrorView(messages.getMessage("logotypeFormat", null, Locale.ROOT));

          String fileName = logofile.getName() + "." + JPG_FORMAT;
        in = new ByteArrayInputStream(logofile.getBytes());
        BufferedImage bImageFromConvert = ImageIO.read(in);
        bImageFromConvert = resize(bImageFromConvert, Integer.valueOf(properties.getProperty("width")),
            Integer.valueOf(properties.getProperty("height")), true);
        File file = new File(directory.getPath() + File.separator + fileName);
        if (file.exists())
          file.delete();
          ImageIO.write(bImageFromConvert, JPG_FORMAT, file);
      }
      File file = new File(SessionUtils.pathToDir + File.separator + "projectName.dat");
      if (!file.exists())
        file.createNewFile();
      fileOutputStream = new FileOutputStream(file);
      out = new BufferedWriter(new FileWriter(file));
      out.write(projectName);
      out.flush();
      response.sendRedirect("tasks");
    }
    finally {
      try {
        if (out != null) {
          out.close();
        }
      }
      catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      }
      try {
        if (fileOutputStream != null) {
          fileOutputStream.close();
        }
      }
      catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      }
      try {
        if (in != null) {
          in.close();
        }
      }
      catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
    return null;
  }

  /**
   * Download the logo file
   */
  @RequestMapping(value = "/downloadLogo", method = RequestMethod.GET)
  public void handleRequest(HttpServletResponse response, HttpServletRequest request) throws IOException {
    InputStream is = null;
    OutputStream os = null;
    OutputStream out = null;
    try {
      File file = new File(SessionUtils.pathToDir + File.separator + "logotype." + JPG_FORMAT);
      if (file.exists()) {
        BufferedImage bufferedImage = ImageIO.read(file);
        out = response.getOutputStream();
        ImageIO.write(bufferedImage, JPG_FORMAT, out);
        out.flush();
      }
      else {
        response.setContentType(JPG_FORMAT);
        is = servletContext.getResourceAsStream("/resources/images/logo.png");
        os = response.getOutputStream();
        IOUtils.copy(is, os);
        os.flush();
      }
    }
    finally {
      try {
        if (out != null) {
          out.close();
        }
      }
      catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      }
      try {
        if (is != null) {
          is.close();
        }
      }
      catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      }
      try {
        if (os != null) {
          os.close();
        }
      }
      catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  /**
   * Reduction to the single size
   */
  public static BufferedImage resize(BufferedImage image, int width, int height, boolean aa) {
    BufferedImage resizedImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = resizedImage.createGraphics();

    if (aa) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY);
    }
    g.drawImage(image, 0, 0, width, height, null);
    g.dispose();
    return resizedImage;
  }

  private ModelAndView getErrorView(String error) {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "change_logotype");
    mv.addObject("error", error);
    return mv;
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
