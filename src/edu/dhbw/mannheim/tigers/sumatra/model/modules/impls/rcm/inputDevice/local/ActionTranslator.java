/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * 
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local;

import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.AInputDevice;


/**
 * This class creates correctly formatted commands.
 * 
 * @author Lukas
 * 
 */

public class ActionTranslator
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(ActionTranslator.class.getName());
	
	// private ActionSender sender;
	private final AInputDevice		inputDevice;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param inputDevice
	 */
	public ActionTranslator(AInputDevice inputDevice)
	{
		this.inputDevice = inputDevice;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * turn hashmap containing all single commands into an instance of ActionCommand
	 * @param values
	 */
	public void translate(HashMap<String, Double> values)
	{
		double forward = 0;
		double backward = 0;
		double left = 0;
		double right = 0;
		double rotateLeft = 0;
		double rotateRight = 0;
		double pass = 0;
		double kick = 0;
		double chipKick = 0;
		double dribble = 0;
		double arm = 0;
		double chipArm = 0;
		double disarm = 0;
		
		try
		{
			forward = values.get("forward");
			backward = values.get("backward");
			left = values.get("left");
			right = values.get("right");
			rotateLeft = values.get("rotateLeft");
			rotateRight = values.get("rotateRight");
			pass = values.get("pass");
			kick = values.get("force");
			chipKick = values.get("chipKick");
			dribble = values.get("dribble");
			arm = values.get("arm");
			disarm = values.get("disarm");
			chipArm = values.get("chipArm");
		} catch (final NullPointerException e)
		{
			log.fatal("HashMap does not have the right keys!", e);
		}
		
		// forward - positive; backward - negative;
		final double translateY = forward - backward;
		// right - positive; left - negative;
		final double translateX = right - left;
		// rotateRight - positive; rotateLeft - negative;
		final double rotate = rotateLeft - rotateRight;
		// bam-hierarchy: shoot-pass-cross
		final ActionCommand cmd = new ActionCommand(translateY, translateX, rotate, kick, chipKick, arm, chipArm,
				dribble, pass, disarm);
		inputDevice.execute(cmd);
	}
}