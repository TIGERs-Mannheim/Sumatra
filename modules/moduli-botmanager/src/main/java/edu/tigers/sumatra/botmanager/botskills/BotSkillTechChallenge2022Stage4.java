/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.botskills;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotSkillTechChallenge2022Stage4 extends ABotSkillTechChallenge2022
{
	public BotSkillTechChallenge2022Stage4()
	{
		super(EBotSkill.TC2022_STAGE4);
	}


	@SuppressWarnings("squid:S00107") // required for UI
	public BotSkillTechChallenge2022Stage4(IVector2 startPos,
			IVector2 targetPos, double velMax, double velMaxW,
			double accMax, double accMaxW, double dribblerSpeed, double dribblerCurrent,
			double rotationSpeed)
	{
		super(EBotSkill.TC2022_STAGE4, startPos, targetPos, velMax, velMaxW, accMax, accMaxW, dribblerSpeed,
				dribblerCurrent,
				rotationSpeed);
	}


	public BotSkillTechChallenge2022Stage4(final IVector2 startPos, final IVector2 targetPos,
			final IMoveConstraints mc,
			final double dribblerSpeed, final double dribblerCurrent, final double rotationSpeed)
	{
		super(EBotSkill.TC2022_STAGE4, startPos, targetPos, mc, dribblerSpeed, dribblerCurrent, rotationSpeed);
	}


	public BotSkillTechChallenge2022Stage4(final IMoveConstraints mc, final double dribblerSpeed,
			final double dribblerCurrent, final double rotationSpeed)
	{
		super(EBotSkill.TC2022_STAGE4, mc, dribblerSpeed, dribblerCurrent, rotationSpeed);
	}


}
