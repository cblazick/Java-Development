// Import required java libraries
import java.io.*; // this brings in the 'PrintWriter' container
import javax.servlet.*; // this brings in the 'ServletException'
// class, and the 'response' object
import javax.servlet.http.*; // this brings in the 'HttpServlet'
// class we will need to overload

// Extend HttpServlet class
public class HelloUser extends HttpServlet {

    public void init() throws ServletException
    {
        // Do nothing, but we need this because the servlet
        // container will call it
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        // Set response content type
        response.setContentType("text/html");

        // Actual response goes here.
        PrintWriter out = response.getWriter();
        out.println("<form method=\"post\">");
        out.println("What is your name?<br>");
        out.println("<input type=\"text\" name=\"name\" size=\"40\">");
        out.println("<input type=\"submit\" value=\"Say Hello\">");
        out.println("</form>");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException
    {
        // Set response content type
        response.setContentType("text/html");

        // Actual response goes here.
        PrintWriter out = response.getWriter();
        out.println("<p>Hello, " +
                request.getParameter("name") +
                "</p>");
    }

    public void destroy()
    {
        // do nothing, but we need this because the servlet
        // container will call it
    }
}