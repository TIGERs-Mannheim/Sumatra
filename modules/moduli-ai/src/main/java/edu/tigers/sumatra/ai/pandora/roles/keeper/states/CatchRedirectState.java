/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;


/**
 * If the KeeperStateCalc detect a redirect of the enemy, the keeper drives direct between the redirect foe and the
 * goal center
 *
 * @author ChrisC
 */
public class CatchRedirectState extends AKeeperState
{
	private MoveToTrajSkill posSkill;
	private boolean stayInGoOut = false;
	
	
	/**
	 * @param parent the parent keeper role
	 */
	public CatchRedirectState(KeeperRole parent)
	{
		super(parent);
	}
	
	
	@Override
	public void doEntryActions()
	{
		posSkill = new MoveToTrajSkill();
		posSkill.getMoveCon().updateDestination(Vector2.zero());
		posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		posSkill.getMoveCon().setBotsObstacle(false);
		posSkill.getMoveCon().setBallObstacle(false);
		posSkill.getMoveCon().getMoveConstraints().setAccMax(KeeperRole.getKeeperAcc());
		posSkill.getMoveCon().setGoalPostObstacle(false);
		posSkill.getMoveCon().setFastPosMode(true);
		setNewSkill(posSkill);
		
		stayInGoOut = false;
	}
	
	
	@Override
	public void doExitActions()
	{
		// Nothing to do here
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 redirectBot = getRedirectBotPosition();
		IVector2 destination = LineMath.stepAlongLine(Geometry.getGoalOur().getCenter(), redirectBot,
				KeeperRole.getDistToGoalCenter());
		
		if (VectorMath.distancePP(getPos(), destination) < (Geometry.getBotRadius() / 2))
		{
			posSkill.getMoveCon().updateLookAtTarget(redirectBot);
		}
		if ((isKeeperBetweenRedirectAndGoalCenter(redirectBot) || stayInGoOut) && isGoOutUseful(redirectBot))
		{
			stayInGoOut = true;
			destination = GoOutState.calcBestDefensivePositionInPE(redirectBot);
			posSkill.getMoveCon().updateLookAtTarget(redirectBot);
		}
		posSkill.getMoveCon().updateDestination(destination);
	}
	
	
	private IVector2 getRedirectBotPosition()
	{
		IVector2 redirectBot;
		BotID redirectBotId = OffensiveMath.getBestRedirector(getWFrame(), getWFrame().getFoeBots());
		if (redirectBotId != null)
		{
			redirectBot = getWFrame().getFoeBot(redirectBotId).getBotKickerPos();
		} else
		{
			redirectBot = getAiFrame().getTacticalField().getEnemyClosestToBall().getBot().getBotKickerPos();
		}
		return redirectBot;
	}
	
	
	private boolean isGoOutUseful(IVector2 redirectBot)
	{
		return redirectBot.x() < KeeperRole.getGoOutWhileRedirectMargin();
	}
	
	
	private boolean isKeeperBetweenRedirectAndGoalCenter(IVector2 redirectBot)
	{
		return LineMath.distancePL(getPos(),
				Line.fromPoints(Geometry.getGoalOur().getCenter(),
						redirectBot)) < Geometry.getBotRadius();
	}
}
