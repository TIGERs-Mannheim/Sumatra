/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 12, 2016
 * Author(s): florian
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.math;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.WorldFrameFactory;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author florian
 */
public class ProperbilityMathTest
{
	
	private WorldFrame	wf			= null;
	private IVector2		vector	= null;
	private long			seed		= 45456;
	
	
	/**
	 * This method sets up a WorldFrame with a constant seed for the Random function
	 */
	@Before
	public void setup()
	{
		WorldFrameFactory.setRandomSeed(seed);
		wf = WorldFrameFactory.createWorldFrame(0, 0);
	}
	
	
	/**
	 * This is a test to catch changes made by a refactoring of the class under test
	 */
	@Test
	public void firstGetDirectShootScoreChanceTest()
	{
		
		vector = new Vector2(0, 0);
		
		double result = ProbabilityMath.getDirectShootScoreChance(wf, vector, false);
		Assert.assertEquals(0.9184842959915077, result, 0);
	}
	
	
	/**
	 * This is a test to catch changes made by a refactoring of the class under test
	 */
	@Test
	public void secondGetDirectShootScoreChanceTest()
	{
		
		vector = new Vector2(-287 - Geometry.getBotRadius(), 44 - Geometry.getBotRadius());
		
		double result = ProbabilityMath.getDirectShootScoreChance(wf, vector, false);
		Assert.assertEquals(0.6792616747972737, result, 0);
		
	}
	
	
	/**
	 * This is a test to catch changes made by a refactoring of the class under test
	 */
	@Test
	public void firstGetFoeScoreChanceWithDefenderTest()
	{
		vector = new Vector2(0, 0);
		
		double result = ProbabilityMath.getFoeScoreChanceWithDefender(wf, vector);
		
		Assert.assertEquals(0.9184842959915086, result, 0);
	}
	
	
	/**
	 * This is a test to catch changes made by a refactoring of the class under test
	 */
	@Test
	public void secondGetFoeScoreChanceWithDefenderTest()
	{
		vector = new Vector2(3743 + Geometry.getBotRadius(), 665 + Geometry.getBotRadius());
		
		double result = ProbabilityMath.getFoeScoreChanceWithDefender(wf, vector);
		
		Assert.assertEquals(0, result, 0);
	}
	
	
	/**
	 * This is a test to catch changes made by a refactoring of the class under test
	 */
	@Test
	public void firstGetDirectHitChanceTest()
	{
		vector = new Vector2(0, 0);
		IVector2 endVector1 = new Vector2(100, 100);
		IVector2 endVector2 = new Vector2(100, 200);
		
		double result = ProbabilityMath.getDirectHitChance(wf, vector, endVector1, endVector2, false);
		
		Assert.assertEquals(0.9375711076816111, result, 0);
	}
	
	
	/**
	 * This is a test to catch changes made by a refactoring of the class under test
	 */
	@Test
	public void secondGetDirectHitChanceTest()
	{
		vector = new Vector2(0, 0);
		IVector2 endVector1 = new Vector2(1000, 1000);
		IVector2 endVector2 = new Vector2(900, 600);
		
		double result = ProbabilityMath.getDirectHitChance(wf, vector, endVector1, endVector2, false);
		
		Assert.assertEquals(0.3271777300376465, result, 0);
	}
}
