package comp3911.cwk2;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {

  private static final String CONNECTION_URL = "jdbc:sqlite:db.sqlite3";
  private static final String AUTH_QUERY = "select * from user where username= ?"; // removed asking for password
  private static final String HASH_QUERY =  "select * from user where username= ? and password = ?";
  private static final String SEARCH_QUERY = "select * from patient where surname= ? collate nocase";
  // don't store in database
  private static final String PEPPER = "f5c9966d4ad5b448be2c579bb9c8f5db906f8fd3471815931291ef191ca80226";

  private final Configuration fm = new Configuration(Configuration.VERSION_2_3_28);
  private Connection database;

  @Override
  public void init() throws ServletException {
    configureTemplateEngine();
    connectToDatabase();
  }

  private void configureTemplateEngine() throws ServletException {
    try {
      fm.setDirectoryForTemplateLoading(new File("./templates"));
      fm.setDefaultEncoding("UTF-8");
      fm.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
      fm.setLogTemplateExceptions(false);
      fm.setWrapUncheckedExceptions(true);
    }
    catch (IOException error) {
      throw new ServletException(error.getMessage());
    }
  }

  private void connectToDatabase() throws ServletException {
    try {
      database = DriverManager.getConnection(CONNECTION_URL);
    }
    catch (SQLException error) {
      throw new ServletException(error.getMessage());
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
    try {
      Template template = fm.getTemplate("login.html");
      template.process(null, response.getWriter());
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    }
    catch (TemplateException error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
     // Get form parameters
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String surname = request.getParameter("surname");

    try {
      if (authenticated(username, password)) {
        // Get search results and merge with template
        Map<String, Object> model = new HashMap<>();
        model.put("records", searchResults(surname));
        Template template = fm.getTemplate("details.html");
        template.process(model, response.getWriter());
      }
      else {
        Template template = fm.getTemplate("invalid.html");
        template.process(null, response.getWriter());
      }
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private boolean authenticated(String username, String password) throws SQLException {
    PreparedStatement query = database.prepareStatement(AUTH_QUERY);
    query.setString(1,username);

    try {
      ResultSet results = query.executeQuery();
      String salt = results.getString(5);

      String newPassword =  SHAExample.get_SHA_512_SecurePassword(password+PEPPER, salt);

      query = database.prepareStatement(HASH_QUERY);
      query.setString(1,username);
      query.setString(2,newPassword);
      results = query.executeQuery();
      return results.next();
    } catch (Exception e){
      return false;
    }
  }

  private List<Record> searchResults(String surname) throws SQLException {
    List<Record> records = new ArrayList<>();
    PreparedStatement query = database.prepareStatement(SEARCH_QUERY

    );
    query.setString(1,surname);

    try {
      ResultSet results = query.executeQuery();
      while (results.next()) {
        Record rec = new Record();
        rec.setSurname(results.getString(2));
        rec.setForename(results.getString(3));
        rec.setAddress(results.getString(4));
        rec.setDateOfBirth(results.getString(5));
        rec.setDoctorId(results.getString(6));
        rec.setDiagnosis(results.getString(7));
        records.add(rec);
      }

    }catch (Exception ignored){

    }
    return records;
  }
}
