/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * Frame with data for recording and visualization
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Entity
public class RecordFrame implements IRecordFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** */
	private IRecordWfFrame			recordFrame;
	/** only contains new referee messages (for one frame) */
	private Command					refereeCmd;
	/** stores all tactical information added by metis' calculators. */
	private TacticalField			tacticalInfo;
	/** */
	private PlayStrategy				playStrategy;
	/** Represents the mapping between a BotID (int), and the {@link ARole} the bot had been assigned to */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private BotIDMapConst<ERole>	assigendRoles;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<Path>				paths	= new ArrayList<Path>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 */
	public RecordFrame(AIInfoFrame aiFrame)
	{
		recordFrame = new RecordWfFrame(aiFrame.worldFrame);
		tacticalInfo = aiFrame.tacticalInfo;
		playStrategy = aiFrame.playStrategy;
		BotIDMap<ERole> roles = new BotIDMap<ERole>();
		for (ARole role : aiFrame.getAssigendRoles().values())
		{
			roles.put(role.getBotID(), role.getType());
		}
		assigendRoles = BotIDMapConst.unmodifiableBotIDMap(roles);
		
		if (aiFrame.refereeMsg != null)
		{
			refereeCmd = aiFrame.refereeMsg.getCommand();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the worldFrame
	 */
	@Override
	public final IRecordWfFrame getRecordWfFrame()
	{
		return recordFrame;
	}
	
	
	/**
	 * @param worldFrame the worldFrame to set
	 */
	public final void setWorldFrame(WorldFrame worldFrame)
	{
		recordFrame = worldFrame;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	@Override
	public final Command getRefereeCmd()
	{
		return refereeCmd;
	}
	
	
	/**
	 * @param refereeCmd the refereeMsg to set
	 */
	public final void setRefereeCmd(Command refereeCmd)
	{
		this.refereeCmd = refereeCmd;
	}
	
	
	/**
	 * @return the tacticalInfo
	 */
	@Override
	public final TacticalField getTacticalInfo()
	{
		return tacticalInfo;
	}
	
	
	/**
	 * @param tacticalInfo the tacticalInfo to set
	 */
	public final void setTacticalInfo(TacticalField tacticalInfo)
	{
		this.tacticalInfo = tacticalInfo;
	}
	
	
	/**
	 * @return the playStrategy
	 */
	@Override
	public final PlayStrategy getPlayStrategy()
	{
		return playStrategy;
	}
	
	
	/**
	 * @param playStrategy the playStrategy to set
	 */
	public final void setPlayStrategy(PlayStrategy playStrategy)
	{
		this.playStrategy = playStrategy;
	}
	
	
	/**
	 * @return the assigendRolesConst
	 */
	@Override
	public final BotIDMapConst<ERole> getAssigendERoles()
	{
		return assigendRoles;
	}
	
	
	/**
	 * @param assigendRoles the assigendRoles to set
	 */
	public final void setAssigendRolesConst(BotIDMapConst<ERole> assigendRoles)
	{
		this.assigendRoles = assigendRoles;
	}
	
	
	/**
	 * @return the paths
	 */
	@Override
	public final List<Path> getPaths()
	{
		return paths;
	}
	
	
	@Override
	public void setPaths(List<Path> paths)
	{
		this.paths = paths;
	}
}
