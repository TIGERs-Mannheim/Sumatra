/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills.data;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import lombok.NoArgsConstructor;


/**
 * A match command includes all information, a robot needs during a match
 */
@NoArgsConstructor
public class MatchCommand implements IMatchCommand
{
	private ABotSkill skill = new BotSkillMotorsOff();
	private boolean autoCharge = false;
	private boolean strictVelocityLimit = false;
	private MultimediaControl multimediaControl = new MultimediaControl();


	public MatchCommand(MatchCommand matchCommand)
	{
		this.skill = matchCommand.skill;
		this.autoCharge = matchCommand.autoCharge;
		this.strictVelocityLimit = matchCommand.strictVelocityLimit;
		this.multimediaControl = new MultimediaControl(matchCommand.multimediaControl);
	}


	@Override
	public void setSkill(final ABotSkill skill)
	{
		this.skill = skill;
	}


	@Override
	public void setKickerAutocharge(final boolean enable)
	{
		autoCharge = enable;
	}


	/**
	 * @return the skill
	 */
	@Override
	public final ABotSkill getSkill()
	{
		return skill;
	}


	/**
	 * @return the autoCharge
	 */
	public final boolean isAutoCharge()
	{
		return autoCharge;
	}


	@Override
	public void setMultimediaControl(final MultimediaControl control)
	{
		multimediaControl = control;
	}


	/**
	 * @return the multimediaControl
	 */
	public final MultimediaControl getMultimediaControl()
	{
		return multimediaControl;
	}


	@Override
	public void setStrictVelocityLimit(final boolean enable)
	{
		strictVelocityLimit = enable;
	}


	public final boolean isStrictVelocityLimit()
	{
		return strictVelocityLimit;
	}
}
