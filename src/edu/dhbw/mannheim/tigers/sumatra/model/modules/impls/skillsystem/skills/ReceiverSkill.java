/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.DebugShapeHacker;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Receive a ball and stop it
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReceiverSkill extends PositionSkill
{
	private static final Logger	log				= Logger.getLogger(ReceiverSkill.class.getName());
	
	@Configurable(comment = "")
	private static int				dribbleSpeed	= 30000;
	
	private IVector2					ballInitPos;
	
	
	/**
	 * 
	 */
	public ReceiverSkill()
	{
		super(ESkillName.RECEIVER);
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
		if ((GeoMath.distancePP(ballInitPos, ball.getPos()) > 50))
		{
			if (ball.getVel().getLength2() < 0.4f)
			{
				getDevices().dribble(cmds, false);
				if (getBotType() == EBotType.TIGER_V3)
				{
					TigerBotV3 botV3 = (TigerBotV3) getBot();
					float dribbleSpeed = Math.abs(botV3.getLatestFeedbackCmd().getDribblerSpeed());
					if (dribbleSpeed < 2000)
					{
						log.debug("Completed due to Dribble speed: " + botV3.getLatestFeedbackCmd().getDribblerSpeed());
						complete();
					} else
					{
						log.debug("Waiting for dribbler: " + dribbleSpeed);
					}
				} else
				{
					complete();
				}
			} else
			{
				// ILine ballLine = new Line(ball.getPos(), ball.getVel());
				ILine ballLine = Line.newLine(ballInitPos, getWorldFrame().getBall().getPos());
				// float angleDiff = GeoMath.angleBetweenVectorAndVector(ball.getVel(), ballTravelLine.directionVector());
				// if (angleDiff > AngleMath.PI_QUART)
				// {
				// complete();
				// return;
				// }
				IVector2 dest = GeoMath.leadPointOnLine(getPos(), ballLine);
				float dist = GeoMath.distancePP(getPos(), dest);
				if (dist > 1000)
				{
					log.debug("Completed due to distance to dest: " + dist);
					complete();
					return;
				}
				setDestination(dest);
				setOrientation(ballLine.directionVector().multiplyNew(-1).getAngle());
				DebugShapeHacker.addDebugShape(new DrawableLine(ballLine, Color.magenta));
				
				// float ball2BotDist = GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos());
				// if (ball2BotDist < 100)
				// {
				// getDevices().dribble(cmds, false);
				// }
			}
		} else
		{
			setDestination(getPos());
			setOrientation(getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle());
		}
	}
	
	
	@Override
	protected void doCalcExitActions(final List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
	}
	
}
