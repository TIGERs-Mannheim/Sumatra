/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 5, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sim.util;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.SumatraBot;
import edu.tigers.sumatra.sim.SumatraBotPair;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class CollisionCalculator
{
	private List<Double>		masses				= new ArrayList<>();
	private List<Vector3>	velocitiesBegin	= new ArrayList<>();
	private List<Vector3>	velocitiesResult	= new ArrayList<>();
	
	
	// private final double collisionNumber = 0;
	
	
	/**
	 * @param collidingBots
	 */
	public CollisionCalculator(final SumatraBotPair collidingBots)
	{
		List<SumatraBot> botPair = collidingBots.getBotPair();
		
		masses.add(botPair.get(0).getMass());
		masses.add(botPair.get(1).getMass());
		
		velocitiesBegin.add(Vector3.copy(botPair.get(0).getVel()));
		velocitiesBegin.add(Vector3.copy(botPair.get(1).getVel()));
		
		calculateCollisionResult();
	}
	
	
	/**
	 * This function will calculate the collision for given member variables.
	 */
	private void calculateCollisionResult()
	{
		for (int i = 0; i < masses.size(); i++)
		{
			Vector3 resultingVelocity = velocitiesBegin.get(0).multiplyNew(masses.get(0));
			resultingVelocity.add(velocitiesBegin.get(1).multiplyNew(masses.get(1)));
			resultingVelocity.multiply(1 / (masses.get(0) + masses.get(1)));
			
			velocitiesResult.add(resultingVelocity);
		}
	}
	
	
	/**
	 * @return
	 */
	public List<Vector3> getResultVelocities()
	{
		return velocitiesResult;
	}
}
