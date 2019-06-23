/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.PositionDriver;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class TowardsBallDriver extends PositionDriver
{
	
	private float						maxSpeedPos	= 0;
	private List<IDrawableShape>	shapes		= new ArrayList<>();
	
	
	/**
	 * @param maxSpeedPos
	 */
	public TowardsBallDriver(final float maxSpeedPos)
	{
		this.maxSpeedPos = maxSpeedPos;
	}
	
	
	@Override
	public boolean isDone()
	{
		return false;
	}
	
	
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		shapes = new ArrayList<IDrawableShape>();
		IVector2 dest = wFrame.getBall().getPos();
		IVector2 direction = wFrame.getBall().getPos().subtractNew(bot.getPos());
		IVector2 botToKicker = AiMath.getBotKickerPos(bot).subtractNew(bot.getPos());
		dest = dest.subtractNew(botToKicker);
		
		if (GeoMath.distancePP(bot.getPos(), dest) > maxSpeedPos)
		{
			IVector2 botToDest = dest.subtractNew(bot.getPos()).normalizeNew();
			dest = bot.getPos().addNew(botToDest.multiplyNew(maxSpeedPos));
		}
		setDestination(dest);
		setOrientation(direction.getAngle());
		shapes.add(new DrawableLine(Line.newLine(AiMath.getBotKickerPos(bot), wFrame.getBall().getPos()), Color.blue));
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.TOWARDS_BALL;
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePathDebug(bot, shapes);
		shapes.addAll(this.shapes);
	}
	
}
