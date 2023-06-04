package ee.ria.govsso.enduserselfservice.logging;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure that logging attributes are set as early as possible.
public class ClientIpLoggingFilter extends OncePerRequestFilter {

    private static final String MDC_ATTRIBUTE_KEY_CLIENT_IP = "client.ip";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = request.getRemoteAddr();
        boolean ipAddressExists = StringUtils.isNotEmpty(ipAddress);

        if (ipAddressExists) {
            MDC.put(MDC_ATTRIBUTE_KEY_CLIENT_IP, ipAddress);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (ipAddressExists) {
                MDC.remove(MDC_ATTRIBUTE_KEY_CLIENT_IP);
            }
        }
    }
}
