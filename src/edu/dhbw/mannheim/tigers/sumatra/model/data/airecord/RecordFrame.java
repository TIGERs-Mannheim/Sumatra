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
@Entity(version = 4)
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
		teamColor = null;
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
		refereeMsg = aiFrame.getLatestRefereeMsg();
		teamColor = aiFrame.getTeamColor();
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
		// remove ValuedFields as they are too big atm
		tacticalInfo.getSupportValues().clear();
	}
}
