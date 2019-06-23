/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 18, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.config;

/**
 * This class is a data holder for configs that are not saved with GIT.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class UserConfig
{
	@Configurable(comment = "Change the standard grSim port (vision)")
	private static int		grSimPort						= 40102;
	
	@Configurable(comment = "Change the standard grSim command port")
	private static int		grSimCommandPort				= 20011;
	
	@Configurable(comment = "Change the standard grSim command back port. This is for blue team. Yellow will get this+1")
	private static int		grSimCommandBackPort			= 30011;
	
	@Configurable(comment = "Overrides vision port if >0.")
	private static int		visionPort						= -1;
	
	@Configurable(comment = "Index of openCL device that should be used. -1 to disable openCL, 0 for auto-selection. Sumatra must be restarted after change.")
	private static int		openClDevice					= -1;
	
	@Configurable(comment = "update rate for repaint of Visualizer")
	private static float		visualizerWpUpdateRate		= 25;
	
	@Configurable(comment = "Frame rate for visualization of AIInfoFrames")
	private static float		visualizerAiUpdateRate		= 25;
	
	@Configurable(comment = "Enable augmented data sender to send data to network")
	private static boolean	augmentedDataSenderEnabled	= false;
	
	@Configurable(comment = "Max relative amount of memory in use relative to max heap before frames will be removed in buffered recording")
	private static float		maxRelUsedMemRecord			= 0.5f;
	
	@Configurable(comment = "Multi-team message multicast address")
	private static String	multiTeamMessageAddress		= "224.5.23.1";
	
	@Configurable(comment = "Multi-team message port")
	private static int		multiTeamMessagePort			= 10012;
	
	@Configurable(comment = "Local port for multi-team message transmission")
	private static int		multiTeamMessageLocalPort	= 10013;
	
	@Configurable(comment = "Multi-team message interface")
	private static String	multiTeamMessageInterface	= "";
	
	
	/**
	 * @return the grSimPort
	 */
	public static int getGrSimPort()
	{
		return grSimPort;
	}
	
	
	/**
	 * @return the openClDevice
	 */
	public static int getOpenClDevice()
	{
		return openClDevice;
	}
	
	
	/**
	 * @return
	 */
	public static float getVisualizerWpUpdateRate()
	{
		return visualizerWpUpdateRate;
	}
	
	
	/**
	 * @return the visionPort
	 */
	public static int getVisionPort()
	{
		return visionPort;
	}
	
	
	/**
	 * @return the visualizeFps
	 */
	public static float getVisualizerAiUpdateRate()
	{
		return visualizerAiUpdateRate;
	}
	
	
	/**
	 * @return the augmentedDataSenderEnabled
	 */
	public static boolean isAugmentedDataSenderEnabled()
	{
		return augmentedDataSenderEnabled;
	}
	
	
	/**
	 * @return
	 */
	public static double getMaxRelUsedMemRecord()
	{
		return maxRelUsedMemRecord;
	}
	
	
	/**
	 * @return the grSimCommandPort
	 */
	public static final int getGrSimCommandPort()
	{
		return grSimCommandPort;
	}
	
	
	/**
	 * @return
	 */
	public static int getGrSimCommandBackPort()
	{
		return grSimCommandBackPort;
	}
	
	
	/**
	 * @return
	 */
	public static String getMultiTeamMessageAddress()
	{
		return multiTeamMessageAddress;
	}
	
	
	/**
	 * @return
	 */
	public static int getMultiTeamMessagePort()
	{
		return multiTeamMessagePort;
	}
	
	
	/**
	 * @return
	 */
	public static int getMultiTeamMessageLocalPort()
	{
		return multiTeamMessageLocalPort;
	}
	
	
	/**
	 * @return
	 */
	public static String getMultiTeamMessageInterface()
	{
		return multiTeamMessageInterface;
	}
}
