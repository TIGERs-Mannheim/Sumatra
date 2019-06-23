/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableText;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DoNothingDriver extends ABaseDriver implements IKickPathDriver
{
	
	/**
	 * 
	 */
	public DoNothingDriver()
	{
		addSupportedCommand(ECommandType.VEL);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.DO_NOTHING;
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		return null;
	}
	
	
	@Override
	public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePath(bot, shapes);
		shapes.add(new DrawableText(bot.getPos().addNew(new Vector2(0, 200)), "IDLE", Color.red));
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return false;
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
	}
	
	
	@Override
	public boolean armKicker()
	{
		return false;
	}
}
