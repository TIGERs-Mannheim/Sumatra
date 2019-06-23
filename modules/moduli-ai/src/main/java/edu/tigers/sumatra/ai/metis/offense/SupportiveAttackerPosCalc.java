/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class SupportiveAttackerPosCalc extends ACalculator
{
	private final PositionValidator positionValidator = new PositionValidator();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		positionValidator.update(getWFrame(), null, null);
		newTacticalField.setSupportiveAttackerMovePos(calcMovePosition(newTacticalField, baseAiFrame.getWorldFrame()));
	}
	
	
	private IVector2 calcMovePosition(TacticalField newTacticalField, WorldFrame worldFrame)
	{
		IVector2 ballPos = worldFrame.getBall().getPos();
		ITrackedBot bot = newTacticalField.getEnemyClosestToBall().getBot();
		
		final IVector2 dir;
		if (bot == null)
		{
			dir = Vector2f.fromX(-1);
		} else if (bot.getPos().x() > ballPos.x())
		{
			dir = ballPos.subtractNew(bot.getPos()).normalizeNew();
		} else
		{
			IVector2 goal = DefenseMath.getBisectionGoal(ballPos);
			dir = goal.subtractNew(ballPos).normalizeNew();
		}
		return ballPos.addNew(dir.multiplyNew(RuleConstraints.getStopRadius()
				+ (Geometry.getBotRadius() * 2)));
	}
}
