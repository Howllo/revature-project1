package net.revature.project1.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionValidationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("email") == null || session.getAttribute("user") == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid session or missing required attributes.");
            return false;
        }
        return true;
    }
}
