/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.11.2010
 * Author(s): torn8
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.dss;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;

/**
 * TODO torn8, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author torn8
 * 
 */
public class Test
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	static Float floatClass = null;
	static float f = 0;
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Test()
	{
		Vector2 vector = new Vector2(3, 3);
		meth(vector);
		System.out.println("vector:" + vector);
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public static void main(String[] args)
	{
		new Test();
	}
	
	public void meth(Vector2 v)
	{
		v.x = 5;
		v.y = 7;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
