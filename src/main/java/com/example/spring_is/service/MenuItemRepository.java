package com.example.spring_is.service;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.spring_is.entity.MenuItem;

public interface MenuItemRepository extends PagingAndSortingRepository<MenuItem, Long>
{

}
