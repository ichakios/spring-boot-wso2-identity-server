package com.example.spring_is.common.config.security;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true) 
@EnableOAuth2Sso
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ResourceServerConfig.class);
    @Override
    public void configure(HttpSecurity http) throws Exception {
        //super.configure(http);
      /* http
                .requestMatcher(new RequestHeaderRequestMatcher("Authorization"))
                .authorizeRequests().anyRequest().fullyAuthenticated();
        */
     http.requestMatcher(new RequestHeaderRequestMatcher("Authorization"))
        .authorizeRequests()
        .antMatchers("/*").access("#oauth2.clientHasRole('Application/cmbass_mobile')")
        .and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
    
    @Autowired
    private ResourceServerProperties sso;
    
    @Bean
    @Primary
    public ResourceServerTokenServices myUserInfoTokenServices() {
        return new AppUserInfoTokenServices(sso.getUserInfoUri(), sso.getClientId());
    }
    
    @Bean
    public UserInfoRestTemplateCustomizer restTemplateCustomizer() {
    	return new UserInfoRestTemplateCustomizer() {

    		@Override
    		public void customize(OAuth2RestTemplate template) {
    			
    			// Give the RestTemplate a BufferingClientHttpRequestFactory so we can read the response twice
    			template.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    			
    			logger.info("####### Customize OAuth2RestTemplate");
    			ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

    				private final Logger log = LoggerFactory.getLogger(getClass());
					@Override
					public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
							throws IOException {
						
						log.info("######### OAuth2RestTemplate HTTP REQUEST START");
						traceRequest(request, body);
						log.info("######### OAuth2RestTemplate HTTP REQUEST END");
						
						ClientHttpResponse response = execution.execute(request, body);
						traceResponse(response);
						log.info("######### OAuth2RestTemplate HTTP RESPONSE END");
						
						return response;
					}
    				
    			};
    			List<ClientHttpRequestInterceptor> ris = new ArrayList<ClientHttpRequestInterceptor>();
    			ris.add(interceptor);
    			template.setInterceptors(ris);
    			logger.info("####### Customize OAuth2RestTemplate");
    		}
    		
    		private void traceRequest(HttpRequest request, byte[] body) throws IOException {
    	        logger.info("===========================request begin================================================");
    	        logger.info("URI : " + request.getURI());
    	        logger.info("Method : " + request.getMethod());
    	        logger.info("Headers : " + request.getHeaders());
    	        logger.info("Request Body : " + new String(body, "UTF-8"));
    	        logger.info("==========================request end================================================");
    	    }

    	    private void traceResponse(ClientHttpResponse response) throws IOException {
    	        StringBuilder inputStringBuilder = new StringBuilder();
    	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
    	        String line = bufferedReader.readLine();
    	        while (line != null) {
    	            inputStringBuilder.append(line);
    	            inputStringBuilder.append('\n');
    	            line = bufferedReader.readLine();
    	        }
    	        logger.info("============================response begin==========================================");
    	        logger.info("status code: " + response.getStatusCode());
    	        logger.info("status text: " + response.getStatusText());
    	        logger.info("Headers : " + response.getHeaders());
    	        logger.info("Response Body : " + inputStringBuilder.toString());
    	        logger.info("=======================response end=================================================");
    	    }
        	
        };
    }
    
}