1.	Introduction
This document provides the technical overview needed in order to let a spring boot instance being secure with a third party identity provider.
Before dive in in this document you should know that a snapshot of the source code is found here.
I advise you to get familiar with Spring Boot before continuing reading this document.
Here is the high level architecture of a boot deployment that we are going to build.
 



 
2.	Working with a spring boot
I am going to create a service called Menu Service which is going to allow users to manipulate Menu items of imaginary restaurant. The service is backed by a persistence layer. First let’s use Spring Boot to create the REST service.
Add the MenuItem entity class to the project.
         @Entity
	public class MenuItem {
	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Long id;
	    private String name;
	    private String description;
	    private Double price;
	    private String image;
}

Then add MenuItem repository JPA interface to create your data access object.
public interface MenuItemRepository extends CrudRepository<MenuItem, Long> {
}	}

Please refer following guide to learn more about how to use JPA with Spring:
https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-jpa-and-spring-data
Now we are ready to add the service class which is going to act as a controller class to handle actual web service requests. Add a controller class for Menu resource with menu repository injected into.
@RestController
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;


    @RequestMapping("/menu")
    public Iterable<MenuItem> getMenu() {
        return menuItemRepository.findAll();
    }

}
A basic service is now ready without security.The next step is to setup spring security.Here we need to create a class by extending ResourceServerConfigurerAdapter to configure resource server for the Spring Boot app.

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true) 
@EnableOAuth2Sso
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ResourceServerConfig.class);
    @Override
    public void configure(HttpSecurity http) throws Exception {
        //super.configure(http);
     
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







The value of the access method inside the expression clientHasrole(‘<ROLE_NAME>’) in the configure method is the role name that should be granted by the user in order to be secure.
This instructs spring security to secure every request to the service and expect the access token from http header with name “Authorization”.
Add the Spring Boot Application class with CommandLineRunner to add some values to menu entity.

@EnableOAuth2Sso
@SpringBootApplication
public class MenuServiceApplication {
    private static final Logger log = LoggerFactory.getLogger(MenuServiceApplication.class);


    public static void main(String[] args) {
        SpringApplication.run(MenuServiceApplication.class, args);
    }


    @Bean
    public CommandLineRunner demo(MenuItemRepository menuItemRepository) {
        return args -> {
            //save couple of menu items
            MenuItem item1 = new MenuItem();
            item1.setName("Chicken Parmesan");
            item1.setDescription("Grilled chicken, fresh tomatoes, feta and mozzarella cheese");
            item1.setPrice(12.09);
            item1.setImage("000001");
            menuItemRepository.save(item1);


            MenuItem item2 = new MenuItem();
            item2.setName("Spicy Italian");
            item2.setDescription("Pepperoni and a double portion of spicy Italian sausage");
            item2.setPrice(11.23);
            item2.setImage("000002");
            menuItemRepository.save(item2);


            MenuItem item3 = new MenuItem();
            item3.setName("Garden Fresh");
            item3.setDescription("Slices onions and green peppers, gourmet " +
                    "mushrooms, black olives and ripe Roma tomatoes");
            item3.setPrice(13.00);
            item3.setImage("000003");
            menuItemRepository.save(item3);


            MenuItem item4 = new MenuItem();
            item4.setName("Tuscan Six Cheese");
            item4.setDescription("Six cheese blend of mozzarella, Parmesan, Romano, Asiago and Fontina");
            item4.setPrice(10.22);
            item4.setImage("000004");
            menuItemRepository.save(item4);


            MenuItem item5 = new MenuItem();
            item5.setName("Spinach Alfredo");
            item5.setDescription("Rich and creamy blend of spinach and garlic Parmesan with Alfredo sauce");
            item5.setPrice(9.98);
            item5.setImage("000005");
            menuItemRepository.save(item5);


            log.info("Getting all menu");


            for (MenuItem menuItem : menuItemRepository.findAll()) {
                log.info("Menu item :" + menuItem.toString());
            }


        };
    }
}
Here important annotation is @EnableOAuth2Sso which instructs Spring security to engage OAuth2 filter to the resources.
Last thing is to add a property file src/main/resources/application.properties with following details

server.port=8080
spring.application.name=menu-service

security.oauth2.client.client-id=<CLIENT_ID>
security.oauth2.client.client-secret=<CLIENT_SECRET>
security.oauth2.client.access-token-uri=<IS_HOST>:<IS_PORT>/oauth2/token
security.oauth2.client.user-authorization-uri=<IS_HOST>:<IS_PORT>/oauth2/authorize
security.oauth2.client.scope=openid
security.oauth2.resource.filter-order=3
security.oauth2.resource.user-info-uri=<IS_HOST>:<IS_PORT>/scim2/Me?attributes=groups,userName
security.oauth2.resource.token-info-uri=<IS_HOST>:<IS_PORT>//oauth2/introspect
security.oauth2.resource.prefer-token-info=true

logging.level.org.springframework.security=INFO



where the client_id and the client_secret are granted in the next section and the is_host and is_post are known upon wso2 identity server setup.
3.	Working with wso2 identity server.
Access the management console by typing below URL and login as default admin user (username: admin and password:admin).
https://localhost:9443/carbon/
Note that this is the default host and port provided by wso2 identity server.
 
After success full login click on ‘home’ then ‘main’ and click on add button under service providers.
 
Give a meaningfull service provider name and description and register it.
 
Click on inbound authentication, then OAuth2/OpenID Connect and then click configure.
 
 
 
In the next screen fill the callback URL with a dummy URL and leave the rest with default values and then click add.
 
From the next screen get the client id and client secret. It will be used to generate access. token while we testing the service.
 
At this state our service and authorization server is up and running. We can test the service by calling the service from any REST API testing tool. Here I am going to use CURL to test the API.
The first step in testing the API is to get access token. In order to get the access token, we have to use OAuth2 token API provided by WSO2 Identity Server. There is multiple way you can get the token using different grant types. In this example we are going to use resource owner password grant type. Here we need to supply a valid username and password from the WSO2 identity server. 
Get the token API call EX:
URL: https://cmbass.capital-banking.com:9445/oauth2/token?grant_type=password&username=<username>&password=<passowrd>&client_id=<client_id>&client_secret=<client_secret>&scope=openid
Method type: POST
Header: Content-Type: application/x-www-form-urlencoded

Copy the access_token returned from the above response, and place it as an authorization header with bearer authentication type with the menu service WS developed in a previous section,
EX:
URL: http://localhost:8080/menu
Method type: GET
Header: Authorization: Bearer <ACCESS_TOKEN>

We have configured WSO2 Identity server as authorization server for a Spring Boot service and invoked the service successfully using access token we got from WSO2 Identity server.


