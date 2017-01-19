// Import required java libraries
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// bring in our custom package
import com.CB.PasswordEncryptionService;

// Extend HttpLogin class
public class HelloLogin extends HttpServlet {

    private PasswordEncryptionService pes;

    public void init() throws ServletException
    {
        // do nothing, but we need this because the servlet
        // container will call it
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        // get the session object. the 'true' creates a new
        // session if one doesn't exist
        HttpSession session = request.getSession(true);
        // set the session timeout length
        session.setMaxInactiveInterval(120);

        // Set response content type
        response.setContentType("text/html");

        // Actual response goes here.
        PrintWriter out = response.getWriter();

        // determin which type of page we need
        if (session.getAttribute("USER") == null) {
            // this happens when the USER is not set
            // which can happen when the site is hit for the
            // first time or a login was unsuccessful, or the
            // session has timed out
            out.println("<form method=\"post\">");
            out.print("<p>User Name: <input type=\"text\"  " +
                    "name=\"user\"");
            out.println(" size=\"40\"></p>");
            out.print("<p>Password: <input type=\"password\"  " +
                    "name=\"password\"");
            out.println(" size=\"40\"></p>");
            out.println("<input type=\"submit\" value=\"login\">");
            out.println("</form>");
        }
        else {
            // we have the users successful login
            String user = session.getAttribute("USER").toString();
            String salutation = "<h1>";
            if ((boolean)session.getAttribute("firstLogin")) {
                salutation += "Hello, ";
                session.setAttribute("firstLogin", false);
            }
            salutation += user + "</h1>";
            out.println(salutation);
        }
    }

    // I will only use he doPost for the actual authentication
    // step, then things will get handed back to doGet
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/html");

        // Actual response goes here.
        PrintWriter out = response.getWriter();

        // initialize the PasswordEncryptionService package
        pes = new PasswordEncryptionService();
        pes.readFile(
                getServletContext().getRealPath("/WEB-INF/users.db"));

        // retrieve variables
        String user = request.getParameter("user");
        String pass = request.getParameter("password");
        HttpSession session = request.getSession(true);

        // verify the user name and password...
        boolean valid = false;
        try {
            valid = pes.authenticate(user, pass);
        }
        // I am assuming this works. It compiles. The plan is for
        // my package to _not_ throw an exception
        catch (Throwable e) {
            e.printStackTrace(out);
        }

        if (valid) {
            // if this is a valid authentication, set the user ...
            session.setAttribute("USER", user);
            session.setAttribute("firstLogin", true);
            // and call the user page with a redirect to the doGet
            String url = response.encodeRedirectURL("./HelloLogin");
            response.sendRedirect(url);
        }
        // else, print invalid, and call doGet without setting user
        else {
            session.setAttribute("USER", null);
            session.setAttribute("firstLogin", null);
            out.println("<p><font color=\"red\">login " +
                    "invalid</font></p>");
            // and call the user login page
            doGet(request, response);
        }
    }

    public void destroy()
    {
        // do nothing, but we need this because the servlet
        // container will call it
    }
}
