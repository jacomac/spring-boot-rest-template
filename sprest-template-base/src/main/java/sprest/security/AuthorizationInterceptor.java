package sprest.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import sprest.api.RequiredAuthority;
import sprest.user.UserManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static sprest.user.UserRight.values.*;

public class AuthorizationInterceptor implements HandlerInterceptor {

    private final UserManager userManager;

    public HashMap<String, List<String>> authoritiesMap;

    public AuthorizationInterceptor(ApplicationContext appContext, UserManager userManager) {
        this.userManager = userManager;
        var authoritiesMap = new HashMap<String, List<String>>();
        Set<Method> methodsAnnotatedWith = new Reflections("sprest", new MethodAnnotationsScanner())
            .getMethodsAnnotatedWith(RequiredAuthority.class);

        for (Method method : methodsAnnotatedWith) {
            var key = getMethodKey(method);
            var methodAuthorities = new ArrayList<>(Arrays.asList(method.getAnnotation(RequiredAuthority.class).value()));
            if (authoritiesMap.containsKey(key)) {
                authoritiesMap.get(key).addAll(methodAuthorities);
            } else {
                authoritiesMap.put(key, methodAuthorities);
            }
        }

        Map<String, Object> beans = appContext.getBeansWithAnnotation(RequiredAuthority.class);
        for (String bean : beans.keySet()) {
            var beanAuthorities = new ArrayList<>(Arrays.asList(Objects.requireNonNull(appContext.findAnnotationOnBean(bean, RequiredAuthority.class)).value()));
            var cBean = StringUtils.capitalize(bean);
            if (authoritiesMap.containsKey(cBean)) {
                authoritiesMap.get(cBean).addAll(beanAuthorities);
                break;
            }
            authoritiesMap.put(cBean, beanAuthorities);
        }

        this.authoritiesMap = authoritiesMap;
    }

    private String getMethodKey(Method method) {
        var className = method.getDeclaringClass().getName();
        var methodName = method.getName();
        var paramsString = Arrays.stream(method.getParameterTypes())
            .map(c -> c.getName().replace(c.getPackageName() + ".", ""))
            .collect(Collectors.joining("&"));

        return String.join("/", className, methodName, paramsString);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws ResponseStatusException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        return canUseMethod(handlerMethod);
    }

    private boolean canUseMethod(HandlerMethod handlerMethod) {
        var requiredRights = getRequiredRightsByHandlerMethod(handlerMethod);
        if (requiredRights.isEmpty()) {
            return true;
        }
        var ownedRights = getOwnedRights();

        return hasRequiredRights(requiredRights, ownedRights);
    }

    private List<String> getOwnedRights() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var subscribedRights = (List<String>) userManager.getAvailableRights();

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(right -> Objects.equals(right, MANAGE_ANNOUNCEMENTS) || subscribedRights.contains(right))
            .toList();
    }

    private List<String> getRequiredRightsByHandlerMethod(HandlerMethod handlerMethod) {
        var controllerName = handlerMethod.getBeanType().getName()
            .replace(handlerMethod.getBeanType().getPackageName() + ".", "");
        var methodKey = getMethodKey(handlerMethod.getMethod());

        var requiredAuthorities = new ArrayList<String>();

        if (authoritiesMap.containsKey(controllerName)) {
            requiredAuthorities.addAll(authoritiesMap.get(controllerName));
        }
        if (authoritiesMap.containsKey(methodKey)) {
            requiredAuthorities.addAll(authoritiesMap.get(methodKey));
        }

        return requiredAuthorities;
    }

    private boolean hasRequiredRights(List<String> requiredRights, List<String> ownedRights) {
        checkIfHasAnyValidRights(ownedRights);
        return canUseSuperAdminMethod(requiredRights, ownedRights) || canUseRegularMethod(requiredRights, ownedRights);
    }

    private void checkIfHasAnyValidRights(List<String> ownedRights) {
        if (ownedRights.size() == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public static boolean canUseSuperAdminMethod(List<String> requiredRights, List<String> ownedRights) {
        return requiredRights.contains(MANAGE_ANNOUNCEMENTS) && ownedRights.contains(MANAGE_ANNOUNCEMENTS);
    }

    public static boolean canUseRegularMethod(List<String> requiredRights, List<String> ownedRights) {
        if (ownedRights.contains(MANAGE_ALL)) {
            return true;
        }

        if (ownedRights.contains(INVOKE_ALL) && requiredRights.toString().contains("INVOKE")) {
            return true;
        }

        if (ownedRights.contains(ACCESS_ALL) && requiredRights.toString().contains("ACCESS")) {
            return true;
        }

        if (ownedRights.stream().anyMatch(requiredRights::contains)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
