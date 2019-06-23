/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.09.2010
 * Author(s):
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.performance.base;

import java.util.ArrayList;
import java.util.Random;


/**
 * This class is meant to represent a simple test-case, as part of a {@link ATestSuite}.
 * 
 * @author Gero
 * 
 */
public abstract class ATestCase implements Runnable
{
	private final ATestSuite		parent;
	private final String				name;
	protected final ArrayList<?>	list	= new ArrayList<Object>();
	
	
	public ATestCase(String name, ATestSuite parent)
	{
		this.parent = parent;
		this.name = name;
	}
	

	public abstract void prepare();
	

	public String getName()
	{
		return this.name;
	}
	

	public void teardown()
	{
	}
	
	
	public void writeRandomData() {
		// Prevent compiler from erasing the whole method
		Random r = new Random();
		int randomIndex = r.nextInt(list.size());
		Object randomObj = list.get(randomIndex);
		parent.write(randomObj.toString());
	}
}
