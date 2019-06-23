/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KickoffShooterRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * The offensive play handles only one role, the OffensiveRole
 *
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class KickoffPlay extends APlay
{
	@Configurable(comment = "This is the distance of each bot to the center line", defValue = "100")
	private static double distanceToCenterLine = 100;
	
	@Configurable(comment = "This is the distance of the single bots to the side line", defValue = "100")
	private static double distanceToSideLine = 100;
	
	private IVector2 movePointRightBase = Vector2.fromXY(-Geometry.getBotRadius() - distanceToCenterLine,
			(Geometry.getFieldWidth() / 2.0)
					- Geometry.getBotRadius() - distanceToSideLine);
	private IVector2 movePointLeftBase = Vector2.fromXY(-Geometry.getBotRadius() - distanceToCenterLine,
			(-Geometry.getFieldWidth() / 2.0)
					+ Geometry.getBotRadius() + distanceToSideLine);
	
	private Map<ESides, MoveRole>	movers					= new EnumMap<>(ESides.class);
	
	private enum ESides
	{
		LEFT,
		RIGHT
	}
	
	
	/**
	 * Default
	 */
	public KickoffPlay()
	{
		super(EPlay.KICKOFF);
	}
	
	
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
	protected ARole onAddRole()
	{
		for (ARole role : getRoles())
		{
			if (role.getType() == ERole.KICKOFF_SHOOTER)
			{
				return new MoveRole();
			}
		}
		return new KickoffShooterRole();
	}
	
	
	@Override
	protected void onGameStateChanged(final GameState gameState)
	{
		// This is not neccessary in this context, because a change of gamestate ends this play
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (getRoles().size() != 3)
		{
			return;
		}
		
		sortMovers();
		
		movers.get(ESides.LEFT).getMoveCon().updateDestination(movePointLeftBase);
		movers.get(ESides.RIGHT).getMoveCon().updateDestination(movePointRightBase);
		
		Map<BotID, IPassTarget> bestPositions = frame.getTacticalField().getKickoffStrategy().getBestMovementPositions();
		
		for (MoveRole mover : movers.values())
		{
			if (bestPositions.containsKey(mover.getBotID()))
			{
				mover.getMoveCon().updateDestination(bestPositions.get(mover.getBotID()).getKickerPos());
			}
		}
		
		for (MoveRole mover : movers.values())
		{
			mover.getMoveCon().updateLookAtTarget(frame.getSimpleWorldFrame().getBall().getPos());
		}
	}
	
	
	private void sortMovers()
	{
		/*
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
		}
		
		double firstDistanceRight = VectorMath.distancePP(movePointRightBase, moveRoles.get(0).getPos());
		double secondDistanceRight = VectorMath.distancePP(movePointRightBase, moveRoles.get(1).getPos());
		
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
}
