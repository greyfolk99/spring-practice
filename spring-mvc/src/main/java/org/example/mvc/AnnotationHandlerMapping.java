package org.example.mvc;

import org.example.mvc.annotation.RequestMapping;
import org.example.mvc.controller.RequestMethod;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnnotationHandlerMapping implements HandlerMapping{
    private final Object[] basePackage;
    private final Map<HandlerKey, AnnotationHandler> handlers = new HashMap<>();
    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
    }

    public void initialize() {
        // Object[]로 리플렉션 생성
        Reflections reflections = new Reflections(basePackage);
        // @Controller가 있는 클래스 Set으로 가져옴
        Set<Class<?>> clazzesWithControllerAnnotation = reflections.getTypesAnnotatedWith(org.example.mvc.annotation.Controller.class, true);
        // 그 클래스마다
        clazzesWithControllerAnnotation.forEach(clazz ->
        // 메소드마다
        Arrays.stream(clazz.getDeclaredMethods()).forEach(declaredMethod -> {
            // RequestMapping 어노테이션을 가져와서
            RequestMapping requestMappingAnnotation = declaredMethod.getDeclaredAnnotation(RequestMapping.class);
            // RequestMethod 형식(GET,POST 등등)마다
            Arrays.stream(getRequestMethods(requestMappingAnnotation))
                .forEach(requestMethod -> handlers.put(
                        // 핸들러키(맵)에다가 어노테이션의 value 값 및 해당 RequestMethod 형식을 저장
                        new HandlerKey(requestMappingAnnotation.value(), requestMethod), new AnnotationHandler(clazz, declaredMethod)
                ));}));
        System.out.println(handlers);
    }

    private RequestMethod[] getRequestMethods(RequestMapping requestMappingAnnotation) {
        return requestMappingAnnotation.method();
    }

    @Override
    public Object findHandler(HandlerKey handlerKey) {
        return handlers.get(handlerKey);
    }
}
