package cn.lmjia.market.dealer.mvc;

import cn.lmjia.market.core.entity.AgentLevel;
import cn.lmjia.market.core.entity.Login;
import cn.lmjia.market.core.repository.AgentLevelRepository;
import cn.lmjia.market.dealer.repository.AgentRepository;
import cn.lmjia.market.dealer.repository.GeneralAgentRepository;
import cn.lmjia.market.dealer.repository.SubAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.annotation.Annotation;

/**
 * @author CJ
 */
@Component
public class AgentLevelArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private GeneralAgentRepository generalAgentRepository;
    @Autowired
    private SubAgentRepository subAgentRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentLevelRepository agentLevelRepository;

    /**
     * Obtains the specified {@link Annotation} on the specified {@link MethodParameter}.
     *
     * @param annotationClass the class of the {@link Annotation} to find on the
     *                        {@link MethodParameter}
     * @param parameter       the {@link MethodParameter} to search for an {@link Annotation}
     * @return the {@link Annotation} that was found or null.
     */
    private <T extends Annotation> T findMethodAnnotation(Class<T> annotationClass,
                                                          MethodParameter parameter) {
        T annotation = parameter.getParameterAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        Annotation[] annotationsToSearch = parameter.getParameterAnnotations();
        for (Annotation toSearch : annotationsToSearch) {
            annotation = AnnotationUtils.findAnnotation(toSearch.annotationType(),
                    annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return findMethodAnnotation(HighestAgent.class, parameter) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal != null && principal instanceof Login) {
            final Login login = (Login) principal;
            AgentLevel agentLevel = agentLevelRepository.findByLogin(login).stream()
                    .findAny().orElse(null);
            if (agentLevel != null && findMethodAnnotation(HighestAgent.class, parameter) != null) {
                // 最高级别的
                AgentLevel current = agentLevel;
                while (current.getSuperior() != null && current.getSuperior().getLogin().equals(login)) {
                    current = current.getSuperior();
                }
                return current;
            }
            return agentLevel;
        }
        return null;
    }
}