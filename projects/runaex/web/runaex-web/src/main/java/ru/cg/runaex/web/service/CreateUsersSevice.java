package ru.cg.runaex.web.service;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author golovlyev
 */
public interface CreateUsersSevice {

  void generateUsers(List<MultipartFile> files, HttpServletResponse response) throws Exception;
}
