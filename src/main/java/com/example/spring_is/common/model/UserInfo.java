package com.example.spring_is.common.model;

import java.util.List;

public class UserInfo
{
	private String id,userName;
	private List<Group> groups;
	
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public String getUserName()
	{
		return userName;
	}
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	public List<Group> getGroups()
	{
		return groups;
	}
	public void setGroups(List<Group> groups)
	{
		this.groups = groups;
	}
	
	
}
