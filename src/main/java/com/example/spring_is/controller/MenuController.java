package com.example.spring_is.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_is.entity.MenuItem;
import com.example.spring_is.service.MenuItemRepository;

@RestController
public class MenuController
{
	@Autowired
    private MenuItemRepository menuItemRepository;

    @RequestMapping("/menu2")
    public Iterable<MenuItem> getMenu(Principal principal) {
    	
        return menuItemRepository.findAll();
    }
}
