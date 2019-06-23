/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.sim.SumatraBot;
import edu.tigers.sumatra.sim.SumatraBotPair;


/**
 * Concept: A class which handles the collisions in the Sumatra simulator:
 * It checks the available bots for collisions and updates their position, so that they are not colliding anymore
 *
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class CollisionHandler
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(CollisionHandler.class.getName());
	
	private SumatraBotPair			collidingBotPair;
	
	
	/**
	 * @param collidingBotPair
	 * @return
	 */
	public void correctCollidingBots(final SumatraBotPair collidingBotPair)
	{
		adjustBotPosition(collidingBotPair);
		
		adjustBotVelocity(collidingBotPair);
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
		
		tempBotList.get(0).setPos(Vector3.from2d(botPositionOne, tempBotList.get(0).getPos().z()));
		tempBotList.get(1).setPos(Vector3.from2d(botPositionTwo, tempBotList.get(1).getPos().z()));
	}
	
	
	/**
	 * @param botsInPlay
	 * @return
	 */
	public List<SumatraBotPair> getCollidingBots(final List<SumatraBot> botsInPlay)
	{
		List<SumatraBotPair> collidingBots = new ArrayList<>();
		
		for (int i = 0; i < botsInPlay.size(); i++)
		{
			for (int j = i + 1; j < botsInPlay.size(); j++)
			{
				collidingBotPair = new SumatraBotPair(botsInPlay.get(i), botsInPlay.get(j));
				
				boolean botsCollide = areTwoBotsColliding();
				
				if (botsCollide)
				{
					collidingBots.add(collidingBotPair);
				}
			}
		}
		
		return collidingBots;
	}
	
	
	private boolean areTwoBotsColliding()
	{
		double botDistance;
		
		List<SumatraBot> collidingBots = collidingBotPair.getBotPair();
		
		IVector2 posBotOne = collidingBots.get(0).getPos().getXYVector();
		IVector2 posBotTwo = collidingBots.get(1).getPos().getXYVector();
		
		botDistance = VectorMath.distancePP(posBotOne, posBotTwo);
		
		return botDistance < ((Geometry.getBotRadius() * 2) - 10);
		
	}
}
