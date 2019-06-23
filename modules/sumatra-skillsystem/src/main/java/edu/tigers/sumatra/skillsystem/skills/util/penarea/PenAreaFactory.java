/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.penarea;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PenAreaFactory
{
	private enum EMode
	{
		LEGACY,
		RECTIFIED,
		ROUNDED
	}
	
	@Configurable(comment = "Extended Penalty Area (EPA) mode", defValue = "RECTIFIED")
	private static EMode mode = EMode.RECTIFIED;
	
	static
	{
		ConfigRegistration.registerClass("skills", PenAreaFactory.class);
	}
	
	
	private PenAreaFactory()
	{
		
	}
	
	
	/**
	 * Builds a new penalty area with the given margin
	 * 
	 * @param margin
	 * @return
	 */
	public static IDefensePenArea buildWithMargin(double margin)
	{
		switch (mode)
		{
			case RECTIFIED:
				return buildRectified(margin + Geometry.getBotRadius());
			case ROUNDED:
				return buildRounded(margin + Geometry.getBotRadius());
			case LEGACY:
				return buildLegacy(margin);
			default:
				throw new IllegalStateException("unknown mode: " + mode);
		}
	}
	
	
	private static IDefensePenArea buildRounded(final double margin)
	{
		double x = Geometry.getPenaltyAreaOur().getRectangle().xExtent() + margin / 2;
		double y = Geometry.getPenaltyAreaOur().getRectangle().yExtent() + margin;
		return new RoundedPenaltyArea(x, y, Geometry.getBotRadius() * 2);
	}
	
	
	private static IDefensePenArea buildRectified(double margin)
	{
		double x = Geometry.getPenaltyAreaOur().getRectangle().xExtent() + margin;
		double y = Geometry.getPenaltyAreaOur().getRectangle().yExtent() + (margin) * 2;
		return new RectifiedPenaltyArea(x, y);
	}
	
	
	private static IDefensePenArea buildLegacy(double margin)
	{
		return new LegacyDefensePenArea(1800, 10).withMargin(margin);
	}
	
}
