package top.cch.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParameterCleanFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        chain.doFilter(new TrimmedRequest(request), response);
    }

    static final class TrimmedRequest extends HttpServletRequestWrapper {

        private final Map<String, String[]> values;

        TrimmedRequest(HttpServletRequest request) {
            super(request);
            Map<String, String[]> copy = new LinkedHashMap<>();
            request.getParameterMap().forEach((name, origin) -> copy.put(name, trimAll(origin)));
            this.values = Collections.unmodifiableMap(copy);
        }

        private static String[] trimAll(String[] origin) {
            return Arrays.stream(origin).map(v -> v == null ? null : v.trim()).toArray(String[]::new);
        }

        @Override
        public String getParameter(String name) {
            String[] value = values.get(name);
            return value == null || value.length == 0 ? null : value[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            return values.get(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return values;
        }
    }
}
