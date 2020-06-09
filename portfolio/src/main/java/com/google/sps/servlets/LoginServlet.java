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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    UserService userService = UserServiceFactory.getUserService();
    List<String> html = new ArrayList<>();
    // User is not logged in then display a login link
    if (!userService.isUserLoggedIn()) {
      //System.err.println("Not logged in");
      html.add("needLogin");
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      html.add("<p>Hello stranger.</p>");
      html.add("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
      out.println(convertToJson(html));
      return;
    }
    // User has not set a nickname, redirect to the set nickname page
    String nickname = getUserNickname(userService.getCurrentUser().getUserId());
    if (nickname == null) {
      html.add("needNickname");
      // Only for UI purposes, to display "stranger" for their logout link
      nickname = "stranger";
      //response.sendRedirect("/nickname");
    } 
    // User is logged in so display a logout link
    String urlToRedirectToAfterUserLogsOut = "/";
    String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);  
    html.add("<p>Hello " + nickname + "!</p>");
    html.add("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
    out.println(convertToJson(html));
    return;
  }

  private String convertToJson(List<String> html) {
    Gson gson = new Gson();
    String json = gson.toJson(html);
    return json;
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
 
  private DatastoreService getDatastore() {
    DatastoreServiceConfig datastoreConfig =
    DatastoreServiceConfig.Builder.withReadPolicy(
        new ReadPolicy(Consistency.STRONG)).deadline(5.0);
    return DatastoreServiceFactory.getDatastoreService(datastoreConfig);
  }


}
