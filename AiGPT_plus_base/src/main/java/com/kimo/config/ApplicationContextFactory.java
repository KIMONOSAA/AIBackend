//package com.kimo.config;
//
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//
///**
// * @author Mr.kimo
// */
//@Component
//public class ApplicationContextFactory implements ApplicationContextAware {
//
//        private static ApplicationContext context;
//
//        @Override
//        public void setApplicationContext(ApplicationContext applicationContext) {
//            context = applicationContext;
//        }
//
//        public static <T> T getBean(Class<T> beanClass) {
//            return context.getBean(beanClass);
//        }
//
//        public static <T> T getBean(String beanName, Class<T> beanClass) {
//            return context.getBean(beanName, beanClass);
//        }
//
//}