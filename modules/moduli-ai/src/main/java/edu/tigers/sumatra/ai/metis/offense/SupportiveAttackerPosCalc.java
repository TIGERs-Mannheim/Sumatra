/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class SupportiveAttackerPosCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setSupportiveAttackerMovePos(calcMovePosition(newTacticalField, baseAiFrame.getWorldFrame()));
	}
	
	
	private IVector2 calcMovePosition(TacticalField newTacticalField, WorldFrame worldFrame)
	{
		IVector2 ballPos = worldFrame.getBall().getPos();
		IVector2 goal = DefenseMath.getBisectionGoal(ballPos);
		IVector2 dir = goal.subtractNew(ballPos).normalizeNew();
		if (!worldFrame.getFoeBots().isEmpty())
		{
			ITrackedBot bot = newTacticalField.getEnemyClosestToBall().getBot();
			
			if ((bot != null) && (bot.getPos().x() > ballPos.x()))
			{
				dir = ballPos.subtractNew(bot.getPos()).normalizeNew();
			}
			return ballPos.addNew(dir.multiplyNew(Geometry.getBotToBallDistanceStop()
					+ (Geometry.getBotRadius() * 2)));
		}
		return ballPos.addNew(Vector2.fromXY(-1, 0).multiplyNew(Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 2)));
	}
}
