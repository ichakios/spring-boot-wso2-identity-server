package com.example.spring_is;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;

import com.example.spring_is.entity.MenuItem;
import com.example.spring_is.service.MenuItemRepository;

@SpringBootApplication
@EnableOAuth2Sso
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