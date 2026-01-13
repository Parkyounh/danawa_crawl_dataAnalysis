package com.du.script1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. CORS 설정: Vue(8081)에서 오는 모든 요청을 허용합니다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8081")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 이미지 폴더 매핑 (어느 컴퓨터에서나 작동하는 상대 경로)
        registry.addResourceHandler("/danawa_db_image/**")
                .addResourceLocations("classpath:/static/danawa_db_image/");

        // 2. Vue 빌드 파일 매핑
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

//    // 2. 외부 이미지 경로 매핑: 서버 외부 폴더의 이미지를 URL로 노출합니다.
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/danawa_db_image/**")
//                // 1. 경로 맨 앞에 file:/// 확인
//                // 2. 모든 역슬래시(\)를 슬래시(/)로 변경
//                // 3. 마지막 경로 끝에 반드시 / 추가
//                .addResourceLocations("file:///C:/Users/DU/Desktop/clone/script_image_vue_v1/script_image1/src/main/resources/static/danawa_db_image/")
//                .setCachePeriod(0); // 즉시 반영을 위해 캐시 끔
//    }


}