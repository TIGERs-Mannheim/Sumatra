/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 14, 2014
 * Author(s): MarkG <MarkGeiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.awt.Color;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.DebugShapeHacker;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Receive a ball and stop it
 * 
 * @author MarkG <MarkGeiger@dlr.de>
 */
public class ReceiverSkillTraj extends MoveToTrajSkill
{
	private static final Logger	log				= Logger.getLogger(ReceiverSkillTraj.class.getName());
	
	@Configurable(comment = "")
	private static int				dribbleSpeed	= 18000;
	
	private IVector2					ballInitPos;
	
	
	/**
	 * 
	 */
	public ReceiverSkillTraj()
	{
		super(ESkillName.RECEIVER_TRAJ);
	}
	
	
	@Override
	protected void doCalcEntryActions(final List<ACommand> cmds)
	{
		ballInitPos = getWorldFrame().getBall().getPos();
		getDevices().dribble(cmds, dribbleSpeed);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		TrackedBall ball = getWorldFrame().getBall();
		if ((GeoMath.distancePP(ballInitPos, ball.getPos()) > 200))
		{
			if (ball.getVel().getLength2() < 0.1f)
			{
				getDevices().dribble(cmds, false);
				if (getBotType() == EBotType.TIGER_V3)
				{
					TigerBotV3 botV3 = (TigerBotV3) getBot();
					if (Math.abs(botV3.getLatestFeedbackCmd().getDribblerSpeed()) < 1000)
					{
						log.debug("Completed due to Dribble speed: " + botV3.getLatestFeedbackCmd().getDribblerSpeed());
						complete();
					}
				} else
				{
					complete();
				}
			} else
			{
				ILine ballLine = new Line(ball.getPos(), ball.getVel());
				IVector2 dest = GeoMath.leadPointOnLine(getPos(), ballLine);
				float dist = GeoMath.distancePP(getPos(), dest);
				if (dist > 1000)
				{
					log.debug("Completed due to distance to dest: " + dist);
					complete();
					return;
				}
				
				finderInput.setTrackedBot(getTBot());
				finderInput.setDest(dest);
				finderInput.setTargetAngle(ballLine.directionVector().multiplyNew(-1).getAngle());
				final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput);
				getBot().getPathFinder().calcPath(localInput);
				TrajPath path = getBot().getPathFinder().getCurPath();
				driver.setPath(path);
				
				DebugShapeHacker.addDebugShape(new DrawableLine(ballLine, Color.magenta));
				
			}
		} else
		{
			finderInput.setTrackedBot(getTBot());
			finderInput.setDest(getPos());
			finderInput.setTargetAngle(getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle());
			final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput);
			getBot().getPathFinder().calcPath(localInput);
			TrajPath path = getBot().getPathFinder().getCurPath();
			driver.setPath(path);
		}
	}
	
	
	@Override
	protected void doCalcExitActions(final List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
	}
	
}
