package org.example.mvc;

import org.example.mvc.controller.RequestMethod;
import org.example.mvc.view.JspViewResolver;
import org.example.mvc.view.ModelAndView;
import org.example.mvc.view.View;
import org.example.mvc.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

// 모든 경로로 요청이 올때 실행
@WebServlet("/")
public class DispatcherServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private List<ViewResolver> viewResolvers;

    // 서버 시작시 실행
    @Override
    public void init() {
        // Controller 인터페이스 상속 받아서 가져오는 방식
 //     RequestMappingHandlerMapping rmhm = new RequestMappingHandlerMapping();
 //     rmhm.init();

        // Controller 어노테이션을 Reflection으로 읽어서 가져오는 방식
        AnnotationHandlerMapping ahm = new AnnotationHandlerMapping("org.example");
        ahm.initialize();

        handlerMappings = List.of(ahm);
        handlerAdapters = List.of(new AnnotationHandlerAdapter());
        System.out.println(handlerMappings+ ", " +handlerAdapters);
        viewResolvers = Collections.singletonList(new JspViewResolver());
    }

    // Request시 실행
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String requestURI = request.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());
        System.out.println(requestURI + ", " + request.getMethod());

        Object handler = handlerMappings.stream()
                .filter(hm -> hm.findHandler(new HandlerKey(requestURI, requestMethod)) != null)
                .map(hm -> hm.findHandler(new HandlerKey(requestURI, requestMethod)))
                .findFirst()
                .orElseThrow(() -> new ServletException("No handler for [" + requestMethod + ", " + requestURI + "]"));

        try {
            HandlerAdapter handlerAdapter = handlerAdapters.stream()
                .filter(ha -> ha.supports(handler))
                .findFirst()
                .orElseThrow(() -> new ServletException("No adapter for handler [" + handler + "]"));

            ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);

            for (ViewResolver viewResolver : this.viewResolvers) {
                View view = viewResolver.resolveViewName(modelAndView.getViewName());
                view.render(modelAndView.getModel(), request, response);
            }
        } catch (Throwable e) {
            logger.error("exception occurred: [{}]", e.getMessage(), e);
            throw new ServletException(e);
        }
    }
}
