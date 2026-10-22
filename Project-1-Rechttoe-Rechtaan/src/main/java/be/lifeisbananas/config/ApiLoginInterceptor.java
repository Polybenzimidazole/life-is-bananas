package be.lifeisbananas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/** De API antwoordt met JSON en 401, de klassieke pagina's blijven doorverwijzen. */
public class ApiLoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var session = request.getSession(false);
        if (session != null && session.getAttribute(SessionKeys.BIO_ENGINEER_ID) != null) return true;
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":401,\"message\":\"Meld je eerst aan\"}");
        return false;
    }
}
