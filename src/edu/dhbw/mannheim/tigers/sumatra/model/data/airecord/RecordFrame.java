/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotNull;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AICom;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.IPlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ITacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;


/**
 * Frame with data for recording and visualization
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Entity(version = 6)
public class RecordFrame implements IRecordFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@PrimaryKey
	private int							id			= 0;
	private static int				nextId	= 1;
	
	/** */
	@NotNull
	@AssertValid
	private final IRecordWfFrame	recordFrame;
	
	/** only contains new referee messages (for one frame) */
	private final RefereeMsg		refereeMsg;
	/** the updated referee message. It will contain current times, scores, etc. */
	private final RefereeMsg		latestRefereeMsg;
	
	/** stores all tactical information added by metis' calculators. */
	@NotNull
	@AssertValid
	private final ITacticalField	tacticalInfo;
	
	/** */
	@NotNull
	@AssertValid
	private final IPlayStrategy	playStrategy;
	
	@NotNull
	@AssertValid
	private final AresData			aresData;
	
	@NotNull
	private final ETeamColor		teamColor;
	
	private final float				fps;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private RecordFrame()
	{
		recordFrame = null;
		tacticalInfo = null;
		playStrategy = null;
		aresData = new AresData();
		refereeMsg = null;
		latestRefereeMsg = null;
		teamColor = null;
		fps = 0;
	}
	
	
	/**
	 * @param aiFrame
	 */
	public RecordFrame(final IRecordFrame aiFrame)
	{
		setId();
		recordFrame = new RecordWfFrame(aiFrame.getWorldFrame());
		tacticalInfo = aiFrame.getTacticalField();
		playStrategy = aiFrame.getPlayStrategy();
		
		aresData = new AresData(aiFrame.getAresData());
		refereeMsg = aiFrame.getNewRefereeMsg();
		latestRefereeMsg = aiFrame.getLatestRefereeMsg();
		teamColor = aiFrame.getTeamColor();
		fps = aiFrame.getFps();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This will be called internally, so normally there is no use to call this.
	 * This is only available for database tasks
	 */
	public final synchronized void setId()
	{
		id = nextId;
		nextId++;
	}
	
	
	/**
	 * @param id
	 */
	@Override
	public void setId(final int id)
	{
		this.id = id;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the worldFrame
	 */
	@Override
	public final IRecordWfFrame getWorldFrame()
	{
		return recordFrame;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	@Override
	public final RefereeMsg getLatestRefereeMsg()
	{
		return latestRefereeMsg;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	@Override
	public final RefereeMsg getNewRefereeMsg()
	{
		return refereeMsg;
	}
	
	
	/**
	 * @return the tacticalInfo
	 */
	@Override
	public final ITacticalField getTacticalField()
	{
		return tacticalInfo;
	}
	
	
	/**
	 * @return the playStrategy
	 */
	@Override
	public final IPlayStrategy getPlayStrategy()
	{
		return playStrategy;
	}
	
	
	@Override
	public AresData getAresData()
	{
		return aresData;
	}
	
	
	@Override
	public boolean isPersistable()
	{
		return true;
	}
	
	
	@Override
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	@Override
	public void cleanUp()
	{
		tacticalInfo.cleanup();
	}
	
	
	@Override
	public float getFps()
	{
		return fps;
	}
	
	
	@Override
	public AICom getAICom()
	{
		return new AICom();
	}
	
	
}
