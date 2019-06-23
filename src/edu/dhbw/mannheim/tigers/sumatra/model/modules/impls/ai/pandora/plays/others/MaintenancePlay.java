/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This play moves all bots to the maintenance position.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class MaintenancePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable(comment = "first maintenance position")
	private static IVector2	startingPos	= new Vector2(-3100, -1800);
	
	@Configurable(comment = "Direction from startingPos with length")
	private static IVector2	direction	= new Vector2(0, 200);
	
	@Configurable(comment = "Orientation of bots")
	private static float		orientation	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public MaintenancePlay()
	{
		super(EPlay.MAINTENANCE);
	}
	
	
	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		MoveRole role = new MoveRole(EMoveBehavior.DO_COMPLETE);
		role.getMoveCon().updateTargetAngle(orientation);
		role.getMoveCon().setPenaltyAreaAllowed(true);
		
		
		return role;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		IVector2 dest = startingPos.subtractNew(direction);
		
		IBotIDMap<TrackedTigerBot> otherBots = new BotIDMap<>();
		otherBots.putAll(frame.getWorldFrame().getFoeBots());
		otherBots.putAll(AiMath.getOtherBots(frame));
		
		List<ARole> roles = new ArrayList<ARole>(getRoles());
		Collections.sort(roles, new RoleComparator());
		
		Circle shape;
		for (ARole aRole : roles)
		{
			MoveRole moveRole = (MoveRole) aRole;
			do
			{
				dest = dest.addNew(direction);
				shape = new Circle(dest, AIConfig.getGeometry().getBotRadius() * 2);
			} while (!AiMath.isShapeFreeOfBots(shape, otherBots, null));
			moveRole.getMoveCon().updateDestination(dest);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private static class RoleComparator implements Comparator<ARole>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= -3790249541686387658L;
		
		
		@Override
		public int compare(final ARole o1, final ARole o2)
		{
			return o1.getBotID().compareTo(o2.getBotID());
		}
		
	}
	
}
