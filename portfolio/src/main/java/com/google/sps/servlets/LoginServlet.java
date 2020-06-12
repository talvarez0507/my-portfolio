// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceConfig.Builder;  
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.ReadPolicy.Consistency; 

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;  
import java.util.ArrayList;  
import com.google.gson.Gson;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  private static final String REDIRECT_URL = "/";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    UserService userService = UserServiceFactory.getUserService();
    List<String> html = new ArrayList<>();
    // User is not logged in then display a login link.
    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL(REDIRECT_URL);
      // This is here so I know whether to load comments or not.
      out.println("1");
      out.println("<p>Hello stranger.</p>");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
      return;
    }
    String nickname = getUserNickname(userService.getCurrentUser().getUserId());
    String logoutUrl = userService.createLogoutURL(REDIRECT_URL);  
    // User has not set a nickname, display the form for creating a nickname.
    if (nickname == null) {
      // This is here so I know whether to load comments or not.
      out.println("0");
      out.println("<p>Hello stranger!</p>");
      out.println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
      out.println("<h1>Set Nickname</h1>");
      out.println("<h2>It appears that you're logged in, but have no nickname.</h2>");
      out.println( "<p>Create your nickname here:</p>");
      out.println("<form method=\"POST\" action=\"/nickname\">");
      out.println("<input name=\"nickname\" value=\"" + nickname + "\" />");
      out.println("<br/>");
      out.println("<button class=\"button\">Create</button>");
      out.println("</form>");
      return;
    } 
    // This is here so I know whether to load comments or not.
    out.println("2");
    out.println("<p>Hello " + nickname + "!</p>");
    out.println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
    return;
  }

  private String getUserNickname(String id) {
    DatastoreService datastore = getDatastore();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
 
  private static DatastoreService getDatastore() {
    DatastoreServiceConfig datastoreConfig =
    DatastoreServiceConfig.Builder.withReadPolicy(
        new ReadPolicy(Consistency.STRONG)).deadline(5.0);
    return DatastoreServiceFactory.getDatastoreService(datastoreConfig);
  }
}
