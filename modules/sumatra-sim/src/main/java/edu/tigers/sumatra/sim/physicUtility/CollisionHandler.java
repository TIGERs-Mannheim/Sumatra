/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.11.2015
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sim.physicUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.sim.SumatraBot;
import edu.tigers.sumatra.sim.SumatraBotPair;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class CollisionHandler
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(CollisionHandler.class.getName());
	
	private SumatraBotPair			collidingBotPair;
	
	@Configurable(comment = "Constant with which the velocity is inverted")
	private static float				velocityConstant	= 0.5f;
	
	
	// Concept: A class which handles the collisions in the Sumatra simulator:
	// It checks the available bots for collisions and updates their position, so that they are not colliding anymore
	
	
	/**
	 * @param collidingBotPair
	 * @return
	 */
	public SumatraBotPair correctCollidingBots(final SumatraBotPair collidingBotPair)
	{
		adjustBotPosition(collidingBotPair);
		
		adjustBotVelocity(collidingBotPair);
		
		return this.collidingBotPair;
	}
	
	
	private void adjustBotVelocity(final SumatraBotPair collidingBotPair)
	{
		CollisionCalculator collisionCalculator = new CollisionCalculator(collidingBotPair);
		
		List<SumatraBot> collidingBots = collidingBotPair.getBotPair();
		
		List<Vector3> resultingVelocities = collisionCalculator.getResultVelocities();
		
		for (int i = 0; i < collidingBots.size(); i++)
		{
			collidingBots.get(i).setVel(resultingVelocities.get(i));
		}
	}
	
	
	private void adjustBotPosition(final SumatraBotPair tempPair)
	{
		IVector2 botDistanceVector = tempPair.getVectorBetweenBots();
		botDistanceVector = botDistanceVector.normalizeNew();
		botDistanceVector = botDistanceVector.multiplyNew(3);
		
		List<SumatraBot> tempBotList = tempPair.getBotPair();
		
		IVector2 botPositionOne = tempBotList.get(0).getPos().getXYVector();
		IVector2 botPositionTwo = tempBotList.get(1).getPos().getXYVector();
		
		botPositionOne = botPositionOne.addNew(botDistanceVector);
		botPositionTwo = botPositionTwo.subtractNew(botDistanceVector);
		
		tempBotList.get(0).setPos(new Vector3(botPositionOne, tempBotList.get(0).getPos().getXYZVector().z()));
		tempBotList.get(1).setPos(new Vector3(botPositionTwo, tempBotList.get(1).getPos().getXYZVector().z()));
	}
	
	
	/**
	 * @param botsInPlay
	 * @return
	 */
	public List<SumatraBotPair> getCollidingBots(final List<SumatraBot> botsInPlay)
	{
		List<SumatraBotPair> collidingBots = new ArrayList<SumatraBotPair>();
		
		for (int i = 0; i < botsInPlay.size(); i++)
		{
			for (int j = i + 1; j < botsInPlay.size(); j++)
			{
				collidingBotPair = new SumatraBotPair(botsInPlay.get(i), botsInPlay.get(j));
				
				boolean botsCollide = areTwoBotsColliding();
				
				if (true == botsCollide)
				{
					collidingBots.add(collidingBotPair);
				}
			}
		}
		
		return collidingBots;
	}
	
	
	private boolean areTwoBotsColliding()
	{
		double botDistance = 0;
		
		List<SumatraBot> collidingBots = collidingBotPair.getBotPair();
		
		IVector2 posBotOne = collidingBots.get(0).getPos().getXYVector();
		IVector2 posBotTwo = collidingBots.get(1).getPos().getXYVector();
		
		botDistance = GeoMath.distancePP(posBotOne, posBotTwo);
		
		if (botDistance < ((Geometry.getBotRadius() * 2) - 10))
		{
			return true;
		}
		
		return false;
	}
}
