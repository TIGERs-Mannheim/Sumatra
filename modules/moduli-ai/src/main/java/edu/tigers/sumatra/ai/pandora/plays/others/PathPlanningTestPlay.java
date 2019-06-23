/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.03.2015
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.ai.sisyphus.errt.ERRTFinder;
import edu.tigers.sumatra.ai.sisyphus.errt.TuneableParameter;
import edu.tigers.sumatra.ai.sisyphus.finder.AFinder;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * TODO dirk, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author dirk
 */
public class PathPlanningTestPlay extends APlay
{
	private static final Logger	log				= Logger.getLogger(PathPlanningTestPlay.class.getName());
																
	private MoveRole					moveRole1;
	private MoveRole					moveRole2;
	private final List<MoveRole>	randomDrivers	= new ArrayList<MoveRole>(10);
																
	private EState						state				= EState.INIT;
																
	ERRTFinder							errtFinder1		= null;
																
	private Random						rnd				= new Random();
																
	private enum EState
	{
		INIT,
		PREPARE,
		DO,
	}
	
	
	/**
	 */
	public PathPlanningTestPlay()
	{
		this(EPlay.PATH_PLANNING_TEST);
	}
	
	
	/**
	 * @param type
	 */
	public PathPlanningTestPlay(final EPlay type)
	{
		super(type);
		
		moveRole1 = new MoveRole(EMoveBehavior.NORMAL);
		moveRole2 = new MoveRole(EMoveBehavior.NORMAL);
		randomDrivers.add(new MoveRole(EMoveBehavior.NORMAL));
		randomDrivers.add(new MoveRole(EMoveBehavior.NORMAL));
		randomDrivers.add(new MoveRole(EMoveBehavior.NORMAL));
		randomDrivers.add(new MoveRole(EMoveBehavior.NORMAL));
		
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		switch (getRoles().size())
		{
			case 1:
				return moveRole1;
			case 2:
				return moveRole2;
			case 3:
			case 4:
			case 5:
			case 6:
				return randomDrivers.get(getRoles().size() - 3);
			default:
				throw new IllegalStateException();
		}
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		switch (getRoles().size())
		{
			case 0:
				moveRole1 = new MoveRole(EMoveBehavior.NORMAL);
				return moveRole1;
			case 1:
				moveRole2 = new MoveRole(EMoveBehavior.NORMAL);
				return moveRole2;
			case 2:
			case 3:
			case 4:
			case 5:
				randomDrivers.set(getRoles().size() - 2, new MoveRole(EMoveBehavior.NORMAL));
				return randomDrivers.get(getRoles().size() - 2);
			default:
				throw new IllegalStateException();
		}
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
		// TODO dirk: Auto-generated method stub
		
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (getRoles().size() != 6)
		{
			return;
		}
		switch (state)
		{
			case INIT:
				moveRole1.getMoveCon().updateDestination(new Vector2(-2000, 1500));
				moveRole2.getMoveCon().updateDestination(new Vector2(2000, 1900));
				for (MoveRole randomDriver : randomDrivers)
				{
					randomDriver.getMoveCon().updateDestination(Geometry.getField().getRandomPointInShape(rnd));
					randomDriver.getMoveCon().setPenaltyAreaAllowedOur(true);
				}
				state = EState.PREPARE;
				break;
			case PREPARE:
				MoveToSkill moveToSkill = (MoveToSkill) moveRole1.getCurrentSkill();
				if (moveToSkill != null)
				{
					AFinder finder = (AFinder) moveToSkill.getSisyphus().getPathFinder();
					TuneableParameter tp = new TuneableParameter();
					tp.setpGoal(0f);
					tp.setpWaypoint(0f);
					finder.setAdjustableParams(tp);
					errtFinder1 = (ERRTFinder) finder;
					errtFinder1.getParamDebug().setTesting(true);
				}
				frame.getWorldFrame();
				if (moveRole1.isDestinationReached() && moveRole2.isDestinationReached())
				{
					log.warn(errtFinder1.getParamDebug().toString());
					moveRole1.getMoveCon().updateDestination(new Vector2(2000, 1500));
					moveRole2.getMoveCon().updateDestination(new Vector2(-2000, 1900));
					for (MoveRole randomDriver : randomDrivers)
					{
						randomDriver.getMoveCon()
								.updateDestination(Geometry.getField().getRandomPointInShape(rnd));
					}
					state = EState.DO;
				}
				break;
			case DO:
				if (moveRole1.isDestinationReached() && moveRole2.isDestinationReached())
				{
					moveRole1.getMoveCon().updateDestination(new Vector2(-2000, 1500));
					moveRole2.getMoveCon().updateDestination(new Vector2(2000, 1900));
					for (MoveRole randomDriver : randomDrivers)
					{
						randomDriver.getMoveCon()
								.updateDestination(Geometry.getField().getRandomPointInShape(rnd));
					}
					state = EState.PREPARE;
				}
				break;
		}
		moveRole1.getMoveCon().updateLookAtTarget(new Vector2(0, 0));
		moveRole2.getMoveCon().updateLookAtTarget(new Vector2(0, 0));
	}
	
}
