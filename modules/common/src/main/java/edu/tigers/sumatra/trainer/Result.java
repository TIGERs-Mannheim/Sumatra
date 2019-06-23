/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer;


public class Result
{
	private String name;
	private String value;
	
	
	public Result()
	{
		// Needed by Json parser
	}
	
	
	public Result(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	public void setName(final String name)
	{
		this.name = name;
	}
	
	
	public String getValue()
	{
		return value;
	}
	
	
	public void setValue(final String value)
	{
		this.value = value;
	}
	
}
