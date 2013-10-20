package ru.cg.runaex.web.bean.generate_users;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author golovlyev
 */
@XmlRootElement
public class GeneratedUsers {

  @XmlElement(required = true)
  private List<GeneratedUserInfo> generatedUserInfos;

  public void addGeneratedUserInfo(GeneratedUserInfo generatedUserInfo) {
    if (this.generatedUserInfos == null)
      this.generatedUserInfos = new ArrayList<GeneratedUserInfo>();
    this.generatedUserInfos.add(generatedUserInfo);
  }
}
