/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.visualizer.view;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Status of a single robot.
 */
@Getter
@Setter
public class BotStatus
{
	private boolean visible;
	private boolean connected;
	private double batRel;
	private double kickerRel;

	private Map<EFeature, EFeatureState> botFeatures = Collections.emptyMap();
	private List<String> brokenFeatures = new ArrayList<>();
	private ERobotMode robotMode;


	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj.getClass() != this.getClass())
		{
			return false;
		}
		BotStatus bs = (BotStatus) obj;
		boolean equalBools = bs.isVisible() == visible && bs.isConnected() == connected;
		boolean equalDoubles = Math.abs(bs.getBatRel() - batRel) < 0.001 &&
				Math.abs(bs.getKickerRel() - kickerRel) < 0.001;

		return equalBools && equalDoubles && bs.getBotFeatures().equals(botFeatures);
	}


	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
