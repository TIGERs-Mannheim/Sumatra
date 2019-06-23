/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 19, 2012
 * Author(s): NicolaiO
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.EnhancedFieldAnalyser;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.ETeamSpecRefCmd;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.FreekickArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Let a bot break clear (freispielen).
 * First approach is to choose a target randomly
 * 
 * @author NicolaiO
 * 
 */
public class BreakClearPlay extends ASupportPlay
{
	
	private final List<MoveRole>			roles						= new LinkedList<MoveRole>();
	private final Random						rnd						= new Random(System.nanoTime());
	private static final float				MAX_HEIGHT				= 2000;
	private static final float				FREE_OF_BOTS_RADIUS	= 300;
	private final Map<MoveRole, EWAI>	botMap					= new HashMap<MoveRole, EWAI>();
	private final FreekickArea				freekickArea			= AIConfig.getGeometry().getFreekickAreaTheir();
	private final boolean					freeKick;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public BreakClearPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		RefereeMsg msg = aiFrame.refereeMsg;
		if ((msg != null)
				&& (msg.getTeamSpecRefCmd().equals(ETeamSpecRefCmd.DirectFreeKickTigers) || msg.getTeamSpecRefCmd().equals(
						ETeamSpecRefCmd.IndirectFreeKickTigers)))
		{
			freeKick = true;
		} else
		{
			freeKick = false;
		}
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			MoveRole role = new MoveRole(EMoveBehavior.LOOK_AT_BALL);
			roles.add(role);
			if (i == 0)
			{
				botMap.put(role, EWAI.FIRST);
			} else
			{
				botMap.put(role, EWAI.OTHER);
			}
			addDefensiveRole(role, calcTarget(aiFrame, role));
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame aiFrame)
	{
		for (ARole role : roles)
		{
			if (!checkDestination(aiFrame, role.getDestination(), role))
			{
				role.updateDestination(calcTarget(aiFrame, role));
			}
		}
	}
	
	
	/**
	 * Calculate new random target
	 * 
	 * @return
	 */
	private IVector2 calcTarget(AIInfoFrame aiFrame, ARole role)
	{
		IVector2 target = null;
		target = searchTarget(aiFrame, role);
		if (target == null)
		{
			do
			{
				final int y = (int) ((rnd.nextFloat() * AIConfig.getGeometry().getFieldWidth()) - (AIConfig.getGeometry()
						.getFieldWidth() / 2));
				final int x = (int) (rnd.nextFloat() * MAX_HEIGHT);
				target = new Vector2(x, y);
			} while (!checkDestination(aiFrame, target, role));
		}
		
		
		return target;
	}
	
	
	/**
	 * Search a target for the role, for the first bot search first on the left free side and for the second on the other
	 * 
	 * @author PhilippP {Ph.Posovszky@gmail.com}
	 * 
	 * @param aiFrame
	 * @param role
	 * @return
	 */
	private IVector2 searchTarget(AIInfoFrame aiFrame, ARole role)
	{
		EnhancedFieldAnalyser analyser = aiFrame.tacticalInfo.getEnhancedFieldAnalyser();
		IVector2 target = null;
		
		if (analyser != null)
		{
			List<Integer> listQuadrantsEmpty = analyser.getEmptyTigersQuadrants();
			if (botMap.get(role) == EWAI.FIRST)
			{
				target = getTarget(aiFrame, role, listQuadrantsEmpty, 1, 4);
			}
			
			if (botMap.get(role) == EWAI.OTHER)
			{
				target = getTarget(aiFrame, role, listQuadrantsEmpty, 4, 1);
			}
		}
		return target;
	}
	
	
	/**
	 * Get a target from a empty quadrant
	 * 
	 * @param first - secondQuadrant to check
	 * @param second -firstQuadrant to check
	 * 
	 * @return
	 */
	private IVector2 getTarget(AIInfoFrame aiFrame, ARole role, List<Integer> listQuadrantsEmpty, int first, int second)
	{
		IVector2 target = null;
		if (isInEmpty(listQuadrantsEmpty, first))
		{
			do
			{
				
				final int y = (int) ((rnd.nextFloat() * (AIConfig.getGeometry().getFieldWidth() * 0.5f)));
				final int x = (int) (rnd.nextFloat() * MAX_HEIGHT);
				target = new Vector2(x, y);
			} while (!checkDestination(aiFrame, target, role));
			
		} else if (isInEmpty(listQuadrantsEmpty, second))
		{
			do
			{
				
				final int y = (int) (-(rnd.nextFloat() * (AIConfig.getGeometry().getFieldWidth() * 0.5f)));
				final int x = (int) (rnd.nextFloat() * MAX_HEIGHT);
				target = new Vector2(x, y);
			} while (!checkDestination(aiFrame, target, role));
		}
		return target;
	}
	
	
	/**
	 * Check if the parameter in the list
	 * 
	 * @param listQuadrantsEmpty
	 * @param quadrant
	 * @return
	 */
	private boolean isInEmpty(List<Integer> listQuadrantsEmpty, int quadrant)
	{
		
		for (Integer quadrantElemnt : listQuadrantsEmpty)
		{
			if (quadrant == quadrantElemnt)
			{
				return true;
			}
		}
		return false;
	}
	
	
	private boolean checkDestination(AIInfoFrame aiFrame, IVector2 target, ARole role)
	{
		final ICircle penCirc = new Circlef(target, FREE_OF_BOTS_RADIUS);
		final ICircle ballCirc = new Circlef(target, 590);
		if (ballCirc.isPointInShape(aiFrame.worldFrame.ball.getPos()))
		{
			return false;
		}
		if (freeKick && freekickArea.isPointInShape(target, FREE_OF_BOTS_RADIUS))
		{
			return false;
		}
		for (final TrackedBot bot : aiFrame.worldFrame.tigerBotsAvailable.values())
		{
			if ((bot.getId().getNumber() != role.getBotID().getNumber()) && penCirc.isPointInShape(bot.getPos()))
			{
				return false;
			}
		}
		for (final TrackedBot bot : aiFrame.worldFrame.foeBots.values())
		{
			if (penCirc.isPointInShape(bot.getPos()))
			{
				return false;
			}
		}
		return true;
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
