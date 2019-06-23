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
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.ABaseDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class PullBallDriver extends ABaseDriver
{
	
	private float						driveSpeed	= 0;
	private IVector2					pullTarget	= null;
	private List<IDrawableShape>	shapes		= new ArrayList<>();
	private float						tolerance	= 0;
	private boolean					finished		= false;
	
	
	/**
	 * @param driveSpeed
	 * @param pullTarget
	 * @param tolerance
	 */
	public PullBallDriver(final float driveSpeed, final IVector2 pullTarget, final float tolerance)
	{
		this.driveSpeed = driveSpeed;
		this.pullTarget = pullTarget;
		this.tolerance = tolerance;
		addSupportedCommand(ECommandType.VEL);
	}
	
	
	@Override
	public boolean isDone()
	{
		return finished;
	}
	
	
	@Override
	public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		shapes = new ArrayList<IDrawableShape>();
		IVector2 ballToTarget = pullTarget.subtractNew(wFrame.getBall().getPos()).normalizeNew();
		IVector2 dest = wFrame.getBall().getPos().addNew(ballToTarget.multiplyNew(driveSpeed));
		// IVector2 direction = wFrame.getBall().getPos().subtractNew(bot.getPos());
		// IVector2 botToKicker = AiMath.getBotKickerPos(bot).subtractNew(bot.getPos());
		
		IVector2 botToDest = dest.subtractNew(bot.getPos());
		// float orient = ballToTarget.normalizeNew().multiplyNew(-1).getAngle();
		
		if (GeoMath.distancePP(pullTarget, wFrame.getBall().getPos()) < tolerance)
		{
			finished = true;
		} else
		{
			finished = false;
		}
		shapes.add(new DrawableLine(Line.newLine(wFrame.getBall().getPos(), pullTarget), Color.blue));
		
		return new Vector3(botToDest.normalizeNew().multiplyNew(-0.2f), 0);
	}
	
	
	// @Override
	// public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	// {
	// shapes = new ArrayList<IDrawableShape>();
	// // IVector2 ballToTarget = pullTarget.subtractNew(wFrame.getBall().getPos()).normalizeNew();
	// // IVector2 dest = wFrame.getBall().getPos().addNew(ballToTarget.multiplyNew(driveSpeed));
	// // IVector2 direction = wFrame.getBall().getPos().subtractNew(bot.getPos());
	// // IVector2 botToKicker = AiMath.getBotKickerPos(bot).subtractNew(bot.getPos());
	// //
	//
	// if (GeoMath.distancePP(pullTarget, wFrame.getBall().getPos()) < tolerance)
	// {
	// finished = true;
	// } else
	// {
	// finished = false;
	// }
	// shapes.add(new DrawableLine(Line.newLine(wFrame.getBall().getPos(), pullTarget), Color.blue));
	// }
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.PULL_BALL;
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePathDebug(bot, shapes);
		shapes.addAll(this.shapes);
	}
}
