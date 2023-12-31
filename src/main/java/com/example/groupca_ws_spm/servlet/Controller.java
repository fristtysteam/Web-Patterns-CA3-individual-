package com.example.groupca_ws_spm.servlet;

import java.io.*;

import com.example.groupca_ws_spm.business.User;
import com.example.groupca_ws_spm.repositories.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "controller", value = "/controller")
public class Controller extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        processRequest(request,response);

    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        processRequest(request,response);

    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String forwardToJsp = "index.html";
        String action = request.getParameter("action");
        if (action != null) {
            switch (action) {
                case "login":
                    forwardToJsp = loginCommand(request, response);
                    break;
                case "register":
                    forwardToJsp = registerCommand(request, response);
                    break;
                case "changePassword":
                    forwardToJsp = changePasswordCommand(request, response);
                    break;
                default:
                    forwardToJsp = "error.jsp";
                    String error = "No such action defined for this application. Please try again.";
                    session.setAttribute("errorMessage", error);
            }
        } else {
            forwardToJsp = "error.jsp";
            String error = "No action supplied. Please try again.";
            session.setAttribute("errorMessage", error);
        }
        response.sendRedirect(forwardToJsp);
    }
    private String loginCommand(HttpServletRequest request, HttpServletResponse response) {
        String forwardToJsp = "index.jsp";
        HttpSession session = request.getSession(true);
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            UserDao userDao = new UserDao("user_database");
            User u = userDao.findUserByUsernamePassword(username, password);
            if (u == null) {
                forwardToJsp = "error.jsp";
                String error = "Incorrect credentials supplied. Please <a href=\"login.jsp\">try again.</a>";
                session.setAttribute("errorMessage", error);
            } else {
                forwardToJsp = "loginSuccessful.jsp";
                session.setAttribute("username", username);
                session.setAttribute("user", u);
            }
        } else {
            forwardToJsp = "error.jsp";
            String error = "No username and/or password supplied. Please <a href=\"login.jsp\">try again.</a>";
            session.setAttribute("errorMessage", error);
        }
        return forwardToJsp;
    }

    private String registerCommand(HttpServletRequest request, HttpServletResponse response) {
        String forwardToJsp = "index.jsp";
        HttpSession session = request.getSession(true);
        String uname = request.getParameter("username");
        String pword = request.getParameter("password");
        String first = request.getParameter("fName");
        String last = request.getParameter("lName");

        if (uname != null && pword != null && !uname.isEmpty() && !pword.isEmpty() && first != null && !first.isEmpty() && last != null && !last.isEmpty()) {
            UserDao userDao = new UserDao("user_database");
            int id = userDao.addUser(uname, pword, first, last);
            if (id == -1) {
                forwardToJsp = " ";
                String error = "This user could not be added. Please <a href=\"register.jsp\">try again.</a>";
                session.setAttribute("errorMessage", error);
            } else {
                forwardToJsp = "loginSuccessful.jsp";
                session.setAttribute("username", uname);
                User u = new User(id, first, last, uname, pword);
                session.setAttribute("user", u);
                String msg = "Registration successful, you are now logged in!";
                session.setAttribute("msg", msg);
            }
        } else {
            forwardToJsp = "error.jsp";
            String error = "Some information was not supplied. Please <a href=\"register.jsp\">try again.</a>";
            session.setAttribute("errorMessage", error);
        }
        return forwardToJsp;
    }

    private String changePasswordCommand(HttpServletRequest request, HttpServletResponse response){
        String forwardToJsp = "index.jsp";
        HttpSession session = request.getSession(true);
        User u = (User) session.getAttribute("user");
        if(u != null){
            String oldPass = request.getParameter("oldPassword");
            String newPassOne = request.getParameter("newPassword");
            String newPassTwo = request.getParameter("newPasswordCopy");
            if(oldPass != null && newPassOne != null && newPassTwo != null && !oldPass.isBlank() && !newPassOne.isBlank() && !newPassTwo.isBlank()){
                // Real password info was provided for all fields
                if(newPassOne.equals(newPassTwo)){
                    // New passwords match, continue to do database action
                    UserDao userDao = new UserDao("user_database");
                    int result = userDao.changePassword(u.getUsername(), oldPass, newPassOne);
                    if(result == 1){
                        // Password appears successfully changed
                        forwardToJsp = "loginSuccessful.jsp";
                        String msg = "Your password has been changed successfully!";
                        session.setAttribute("msg", msg);
                    }else{
                        forwardToJsp = "error.jsp";
                        String error = "The password could not be changed at this time. Did you correctly enter your " +
                                "old password? Please <a href=\"changePassword.jsp\">go " +
                                "back" + "</a> and try again.";
                        session.setAttribute("errorMessage", error);
                    }
                }else{
                    forwardToJsp = "error.jsp";
                    String error = "Supplied new passwords do not match. Please <a href=\"changePassword.jsp\">go " +
                            "back" + "</a> and try again.";
                    session.setAttribute("errorMessage", error);
                }
            }else{
                forwardToJsp = "error.jsp";
                String error = "One or more password fields were not provided. Please <a href=\"changePassword" +
                        ".jsp\">go back</a>" +
                        " and try again.";
                session.setAttribute("errorMessage", error);
            }
        }else{
            forwardToJsp = "error.jsp";
            String error = "You are not currently logged in. Please <a href=\"login.jsp\">login</a> and try again.";
            session.setAttribute("errorMessage", error);
        }
        return forwardToJsp;
    }

    public void destroy() {
    }
}