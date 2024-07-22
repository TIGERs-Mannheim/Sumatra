/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.Getter;


/**
 * Configuration object for rule parameters.
 *
 * @author Stefan Schneyer
 */
public class RuleConstraints
{
	@Getter
	@Configurable(comment = "Max allowed ball speed", defValue = "6.5")
	private static double maxBallSpeed = 6.5;
	@Getter
	@Configurable(comment = "Max allowed kick speed (internal, to avoid kicking too fast)", defValue = "6.2")
	private static double maxKickSpeed = 6.2;
	@Getter
	@Configurable(comment = "Stop radius around ball", defValue = "500.0")
	private static double stopRadius = 500.0;
	@Getter
	@Configurable(comment = "Bots must be behind this line on penalty shot", defValue = "1000.0")
	private static double distancePenaltyMarkToPenaltyLine = 1000;
	@Getter
	@Configurable(comment = "Bot speed in stop phases", defValue = "1.5")
	private static double stopSpeed = 1.5;
	@Getter
	@Configurable(comment = "Distance between bots and penalty area in standard situations", defValue = "200.0")
	private static double botToPenaltyAreaMarginStandard = 200;
	@Getter
	@Configurable(comment = "Offset to distance between bots and penalty area in standard situations", defValue = "10.0")
	private static double botToPenaltyAreaMarginStandardOffset = 10;
	@Getter
	@Configurable(comment = "Ball placement accuracy tolerance of referee", defValue = "150.0")
	private static double ballPlacementTolerance = 150;
	@Getter
	@Configurable(comment = "The max allowed robot height", defValue = "150.0")
	private static double maxRobotHeight = 150;
	@Getter
	@Configurable(comment = "[s] The amount of time before a violation is reported again", defValue = "2.0")
	private static double gracePeriod = 2.0;
	@Getter
	@Configurable(comment = "[s] The maximum time allowed for the keeper to hold the ball inside the PenArea", defValue = "5.0")
	private static double keeperHeldBallPeriod = 5.0;

	static
	{
		ConfigRegistration.registerClass("ruleConst", RuleConstraints.class);
	}


	private RuleConstraints()
	{
	}


	public static double getPenAreaMarginStandard()
	{
		return botToPenaltyAreaMarginStandard + botToPenaltyAreaMarginStandardOffset;
	}
}
