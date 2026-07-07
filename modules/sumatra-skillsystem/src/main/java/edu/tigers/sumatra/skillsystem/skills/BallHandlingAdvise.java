package edu.tigers.sumatra.skillsystem.skills;

import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
public class BallHandlingAdvise
{
	EBallHandlingSkillMoveAdvise moveAdvise;
	EBallHandlingSkillTurnAdvise turnAdvise;

	public BallHandlingAdvise()
	{
		moveAdvise = EBallHandlingSkillMoveAdvise.NONE;
		turnAdvise = EBallHandlingSkillTurnAdvise.NONE;
	}
}
