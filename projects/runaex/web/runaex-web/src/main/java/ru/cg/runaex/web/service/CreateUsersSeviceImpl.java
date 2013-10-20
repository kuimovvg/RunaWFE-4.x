package ru.cg.runaex.web.service;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.csvreader.CsvReader;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.delegate.ExecutorServiceDelegate;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import ru.cg.runaex.web.bean.generate_users.GeneratedUserInfo;
import ru.cg.runaex.web.bean.generate_users.GeneratedUsers;
import ru.cg.runaex.web.security.SecurityUtils;

/**
 * @author golovlyev
 */
@Service
public class CreateUsersSeviceImpl implements CreateUsersSevice {
  private static final SimpleDateFormat fileDateFmt = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
  private static final String OUTPUT_DIRECTORY = System.getProperty("jboss.server.temp.dir") + File.separator;

  @Override
  public void generateUsers(List<MultipartFile> files, HttpServletResponse response) throws Exception {
    GeneratedUsers generatedUsers = new GeneratedUsers();
    ExecutorServiceDelegate executorServiceDelegate = (ExecutorServiceDelegate) Delegates.getExecutorService();
    for (MultipartFile file : files) {
      String originalName = file.getOriginalFilename();
      if (!originalName.endsWith("csv"))
        throw new IllegalArgumentException("The file must have a csv format");

      CsvReader csvReader = new CsvReader(file.getInputStream(), ';', Charset.forName("UTF-8"));
      if (csvReader.readRecord()) {
        while (csvReader.readRecord()) {
          String[] data = csvReader.getValues();
          if (data.length < 3)
            throw new IllegalArgumentException("Can't parse file " + file.getName() + ". Should have 3 data columns.");
          String groupName = data[0];
          String login = data[1];
          String fullUserName = data[2];

          User user = SecurityUtils.getCurrentRunaUser();
          Actor actor = new Actor(login, "", fullUserName);
          if (!executorServiceDelegate.isExecutorExist(user, actor.getName())) {
            executorServiceDelegate.create(user, actor);
            //6-значный пароль
            String password = String.valueOf((int) (Math.random() * 999999) + 100000);
            executorServiceDelegate.setPassword(user, actor, password);

            Group group = new Group(groupName, "");

            if (!executorServiceDelegate.isExecutorExist(user, group.getName()))
              executorServiceDelegate.create(user, group);
            else
              group = executorServiceDelegate.getExecutorByName(user, group.getName());

            executorServiceDelegate.addExecutorToGroups(user, actor.getId(), Arrays.asList(group.getId()));

            GeneratedUserInfo generatedUserInfo = new GeneratedUserInfo();
            generatedUserInfo.setLogin(login);
            generatedUserInfo.setFullName(fullUserName);
            generatedUserInfo.setPassword(password);
            generatedUsers.addGeneratedUserInfo(generatedUserInfo);
          }
        }
      }
      final String fileName = "generatedUsers_" + fileDateFmt.format(new Date());
      serializeObjectToXML(generatedUsers, response, fileName);
    }
  }

  public void serializeObjectToXML(GeneratedUsers generatedUsers, HttpServletResponse response, String fileName) throws Exception {
    File file = new File(OUTPUT_DIRECTORY + fileName);
    JAXBContext jaxbContext = JAXBContext.newInstance(GeneratedUsers.class);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    jaxbMarshaller.marshal(generatedUsers, file);
    response.setContentType("application/xml");
    response.setHeader("Content-Disposition", "attachment; filename=".concat(URLEncoder.encode(fileName, "UTF-8")));
    byte[] bytes = FileUtils.readFileToByteArray(file);
    response.getOutputStream().write(bytes);
  }
}
