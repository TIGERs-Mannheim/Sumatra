/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TurnWithBallDriver extends PositionDriver implements IKickPathDriver
{
	private final DynamicPosition	receiver;
	private boolean					shoot						= false;
	
	@Configurable
	private static float				orthDirTurnPercent	= 1f;
	@Configurable
	private static float				orientAheadPercent	= 0.5f;
	@Configurable
	private static float				destDist					= 100;
	
	
	private float						lastRotation			= 0;
	
	
	/**
	 * @param receiver
	 */
	public TurnWithBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
		
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.TURN_WITH_BALL;
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return true;
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePathDebug(bot, shapes);
		DrawablePoint dpState = new DrawablePoint(bot.getPos().addNew(new Vector2(-150, 100)));
		dpState.setText("TurnWithBall");
		shapes.add(dpState);
	}
	
	
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		float dist2Ball = GeoMath.distancePP(wFrame.getBall().getPos(), bot.getPos());
		if (dist2Ball > 200)
		{
			setDone(true);
		} else
		{
			IVector2 isDir = wFrame.getBall().getPos().subtractNew(bot.getPos())
					.scaleToNew(bot.getBot().getCenter2DribblerDist());
			IVector2 targetDir = receiver.subtractNew(wFrame.getBall().getPos());
			
			float rotation = AngleMath.getShortestRotation(isDir.getAngle(), targetDir.getAngle());
			if ((Math.abs(rotation) < 0.05f)
					|| ((lastRotation != 0) && (Math.signum(lastRotation) != Math.signum(rotation))))
			{
				shoot = true;
			} else
			{
				float rel = Math.signum(rotation)
						* Math.min(1, (Math.abs(rotation) * orthDirTurnPercent) / AngleMath.PI_HALF);
				IVector2 orthDir = isDir.turnNew(AngleMath.PI_HALF * -rel).normalize();
				IVector2 dest = bot.getPos().addNew(orthDir.multiplyNew((destDist * Math.abs(rotation))))
						.add(targetDir.scaleToNew(50));
				float orient = isDir.getAngle() + (rotation * orientAheadPercent);
				setDestination(dest);
				setOrientation(orient);
			}
			lastRotation = rotation;
		}
	}
	
	
	@Override
	public boolean armKicker()
	{
		return shoot;
	}
}
