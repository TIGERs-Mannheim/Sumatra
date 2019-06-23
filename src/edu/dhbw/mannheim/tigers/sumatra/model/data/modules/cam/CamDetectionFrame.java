/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;


/**
 * This class contains every information a
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionFrame} has to offer about
 * the current situation on the
 * field
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 * 
 */
public class CamDetectionFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** time-stamp in System.nanotime() */
	public final long					tCapture;
	
	/** time-stamp in System.nanotime() */
	public final long					tSent;
	
	/** time-stamp in System.nanotime() */
	public final long					tReceived;
	
	/** ID 0 or 1 */
	public final int					cameraId;
	
	/** independent frame number, continuous */
	public final long					frameNumber;
	
	/** */
	public final FrameID				id;
	
	/** frames per second (refresh every second) */
	public final double				fps;
	
	/** */
	public final List<CamBall>		balls;
	/** */
	public final List<CamRobot>	robotsTigers;
	/** */
	public final List<CamRobot>	robotsEnemies;
	
	/** */
	public final TeamProps			teamProps;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param tCapture
	 * @param tSent
	 * @param tReceived
	 * @param cameraId
	 * @param frameNumber
	 * @param fps
	 * @param balls
	 * @param tigers
	 * @param enemies
	 * @param teamProps
	 */
	public CamDetectionFrame(long tCapture, long tSent, long tReceived, int cameraId, long frameNumber, double fps,
			List<CamBall> balls, List<CamRobot> tigers, List<CamRobot> enemies, TeamProps teamProps)
	{
		// Fields
		this.tCapture = tCapture;
		this.tSent = tSent;
		this.tReceived = tReceived;
		this.cameraId = cameraId;
		this.frameNumber = frameNumber;
		this.fps = fps;
		
		id = new FrameID(cameraId, frameNumber);
		
		// Collections
		this.balls = balls; // Collections.unmodifiableList(balls);
		robotsTigers = Collections.unmodifiableList(tigers);
		robotsEnemies = Collections.unmodifiableList(enemies);
		
		this.teamProps = teamProps;
	}
	
	
	/**
	 * Create an empty cam detection frame
	 * 
	 * @param frameNumber
	 * @param teamProps
	 * @return
	 */
	public static CamDetectionFrame createEmpty(long frameNumber, TeamProps teamProps)
	{
		final long time = (long) ((System.nanoTime() - WPConfig.getFilterTimeOffset()) * WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME);
		return new CamDetectionFrame(time, time, time, 0, frameNumber, 0, new LinkedList<CamBall>(),
				new LinkedList<CamRobot>(), new LinkedList<CamRobot>(), teamProps);
	}
}
