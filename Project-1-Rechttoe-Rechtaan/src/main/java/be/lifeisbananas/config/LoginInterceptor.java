package be.lifeisbananas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Houdt wie niet aangemeld is weg van de receptpagina's en stuurt hem naar
 * het aanmeldscherm.
 */
public class LoginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		HttpSession session = request.getSession(false);
		boolean aangemeld = session != null && session.getAttribute(SessionKeys.BIO_ENGINEER_ID) != null;
		if (aangemeld) {
			return true;
		}
		response.sendRedirect(request.getContextPath() + "/login.html");
		return false;
	}
}
