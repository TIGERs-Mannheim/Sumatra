/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 19, 2013
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.ai.pandora.roles.offense.KickoffShooterRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * The offensive play handles only one role, the OffensiveRole
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class KickoffPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2					movePointRightBase		= new Vector2(-Geometry.getBotRadius() - 20,
																					(Geometry.getFieldWidth() / 2.0)
																							- Geometry.getBotRadius());
	private IVector2					movePointLeftBase			= new Vector2(-Geometry.getBotRadius() - 20,
																					(-Geometry.getFieldWidth() / 2.0)
																							+ Geometry.getBotRadius());
	
	private double						initialDistanceShooter	= 0;
	
	private Map<ESides, MoveRole>	movers						= new HashMap<>();
	KickoffShooterRole				shooter;
	
	private enum ESides
	{
		LEFT,
		RIGHT
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public KickoffPlay()
	{
		super(EPlay.KICKOFF);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		for (ARole role : getRoles())
		{
			if (role.getType() != ERole.KICKOFF_SHOOTER)
			{
				return role;
			}
		}
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		for (ARole role : getRoles())
		{
			if (role.getType() == ERole.KICKOFF_SHOOTER)
			{
				return new MoveRole(EMoveBehavior.LOOK_AT_BALL);
			}
		}
		return new KickoffShooterRole();
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
		
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		sortMovers();
		
		IVector2 movePointLeft = calculateFollowingPointForShooter(movePointLeftBase);
		IVector2 movePointRight = calculateFollowingPointForShooter(movePointRightBase);
		
		movers.get(ESides.LEFT).getMoveCon().updateDestination(movePointLeft);
		movers.get(ESides.RIGHT).getMoveCon().updateDestination(movePointRight);
		
		
		IVector2 shooterTarget = shooter.getShotTarget();
		
		if (shooterTarget != null)
		{
			frame.getAICom().setOffensiveRolePassTarget(shooterTarget);
			
			if (shooterTarget.get(1) < 0)
			{
				frame.getAICom().setOffensiveRolePassTargetID(movers.get(ESides.LEFT).getBotID());
			} else
			{
				frame.getAICom().setOffensiveRolePassTargetID(movers.get(ESides.RIGHT).getBotID());
			}
		}
	}
	
	
	private void sortMovers()
	{
		/**
		 * Will update the move positions for the sidewards defenders.
		 * They will stick to the point if they get close to it
		 */
		
		List<MoveRole> moveRoles = new ArrayList<>();
		
		for (ARole role : getRoles())
		{
			if (role instanceof MoveRole)
			{
				moveRoles.add((MoveRole) role);
			}
			
			if (role instanceof KickoffShooterRole)
			{
				shooter = (KickoffShooterRole) role;
				initialDistanceShooter = GeoMath.distancePP(shooter.getMoveDestination(), Geometry.getCenter());
			}
		}
		
		double firstDistanceRight = GeoMath.distancePP(movePointRightBase, moveRoles.get(0).getPos());
		double secondDistanceRight = GeoMath.distancePP(movePointRightBase, moveRoles.get(1).getPos());
		
		if (firstDistanceRight < secondDistanceRight)
		{
			movers.put(ESides.RIGHT, moveRoles.get(0));
			movers.put(ESides.LEFT, moveRoles.get(1));
		} else
		{
			movers.put(ESides.RIGHT, moveRoles.get(1));
			movers.put(ESides.LEFT, moveRoles.get(0));
		}
	}
	
	
	IVector2 calculateFollowingPointForShooter(final IVector2 basePosition)
	{
		double distanceShooterCenter = GeoMath.distancePP(shooter.getPos(), Geometry.getCenter());
		distanceShooterCenter -= Geometry.getBotRadius();
		
		if (distanceShooterCenter < (initialDistanceShooter * 2))
		{
			double factorToScalePosition = distanceShooterCenter / initialDistanceShooter;
			
			double valueToSubtractFromX = 10 * Geometry.getBotRadius() * factorToScalePosition;
			return basePosition.subtractNew(new Vector2(valueToSubtractFromX, 0));
		}
		return basePosition;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
