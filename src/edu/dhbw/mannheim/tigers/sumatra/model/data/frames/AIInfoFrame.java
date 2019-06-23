/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.exceptions.RoleHasNotBeenAssignedException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


/**
 * This class is a simple container for all information the AI gathers during its processes for one {@link WorldFrame}
 * 
 * @author Oliver Steinbrecher, Daniel Waigand
 * 
 */
public class AIInfoFrame implements Serializable, IRecordFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long				serialVersionUID	= 1035987436338077764L;
	
	// general data
	/** */
	public final WorldFrame					worldFrame;
	/** only contains new referee messages (for one frame) */
	public final RefereeMsg					refereeMsg;
	/** always contains last referee message */
	public final RefereeMsg					refereeMsgCached;
	
	// from Metis
	/** stores all tactical information added by metis' calculators. */
	public final TacticalField				tacticalInfo;
	
	// from Athena
	/** */
	public final PlayStrategy				playStrategy;
	
	
	// from Lachesis
	/** Represents the mapping between a BotID (int), and the {@link ARole} the bot had been assigned to */
	private final BotIDMap<ARole>			assignedRoles;
	private final BotIDMapConst<ARole>	assigendRolesConst;
	
	/** frames per second */
	private float								fps					= 0;
	
	private EAIControlState					controlState		= null;
	
	private List<Path>						paths					= new ArrayList<Path>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param worldFrame
	 * @param refereeMsg
	 * @param preRefereeMsg
	 */
	public AIInfoFrame(WorldFrame worldFrame, RefereeMsg refereeMsg, RefereeMsg preRefereeMsg)
	{
		this.worldFrame = worldFrame;
		this.refereeMsg = refereeMsg;
		if (refereeMsg != null)
		{
			refereeMsgCached = refereeMsg;
		} else
		{
			refereeMsgCached = preRefereeMsg;
		}
		
		assignedRoles = new BotIDMap<ARole>();
		assigendRolesConst = BotIDMapConst.unmodifiableBotIDMap(assignedRoles);
		
		tacticalInfo = new TacticalField(worldFrame);
		playStrategy = new PlayStrategy();
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values (instead of copying these values, too))
	 * @param original
	 */
	public AIInfoFrame(AIInfoFrame original)
	{
		worldFrame = new WorldFrame(original.worldFrame);
		refereeMsg = original.refereeMsg;
		refereeMsgCached = original.refereeMsgCached;
		
		assignedRoles = new BotIDMap<ARole>();
		assignedRoles.putAll(original.assignedRoles);
		assigendRolesConst = BotIDMapConst.unmodifiableBotIDMap(assignedRoles);
		
		tacticalInfo = new TacticalField(original.tacticalInfo);
		playStrategy = new PlayStrategy(original.playStrategy);
		fps = original.fps;
	}
	
	
	/**
	 * @return the assigendRolesConst
	 */
	@Override
	public BotIDMapConst<ERole> getAssigendERoles()
	{
		BotIDMap<ERole> roles = new BotIDMap<ERole>();
		for (ARole role : getAssigendRoles().values())
		{
			roles.put(role.getBotID(), role.getType());
		}
		return BotIDMapConst.unmodifiableBotIDMap(roles);
	}
	
	
	/**
	 * @return the assigendRolesConst
	 */
	public BotIDMapConst<ARole> getAssigendRoles()
	{
		return assigendRolesConst;
	}
	
	
	/**
	 * 
	 * @param role
	 */
	public void putAssignedRole(ARole role)
	{
		if (!role.hasBeenAssigned())
		{
			throw new RoleHasNotBeenAssignedException("The following role has not been assgined yet: '" + role + "' !!!");
		}
		assignedRoles.put(role.getBotID(), role);
	}
	
	
	/**
	 * Remove all assigned roles
	 */
	public void clearAssignedRoles()
	{
		assignedRoles.clear();
	}
	
	
	/**
	 * Remove a bot that was already assigned.
	 * Carefully! You should know, what you are doing!
	 * 
	 * @param botID
	 */
	public void removeAssignedRole(final BotID botID)
	{
		assignedRoles.remove(botID);
	}
	
	
	/**
	 * Convenience method
	 * 
	 * @param roles
	 */
	public void putAllAssignedRoles(Collection<ARole> roles)
	{
		for (final ARole role : roles)
		{
			putAssignedRole(role);
		}
	}
	
	
	/**
	 * Convenience method
	 * 
	 * @param assignments
	 */
	public void putAllAssignedRoles(IBotIDMap<ARole> assignments)
	{
		for (final Entry<BotID, ARole> entry : assignments)
		{
			putAssignedRole(entry.getValue());
		}
	}
	
	
	/**
	 * Add a drawableShape to the field. Use this, to draw your vectors,
	 * points and other shapes to the field to
	 * visualize your plays actions
	 * 
	 * @see IDrawableShape
	 * 
	 * @param drawableShape
	 */
	public void addDebugShape(IDrawableShape drawableShape)
	{
		tacticalInfo.getDebugShapes().add(drawableShape);
	}
	
	
	/**
	 * @return the fps
	 */
	public float getFps()
	{
		return fps;
	}
	
	
	/**
	 * @param fps the fps to set
	 */
	public void setFps(float fps)
	{
		this.fps = fps;
	}
	
	
	/**
	 * @return the controlState
	 */
	public final EAIControlState getControlState()
	{
		return controlState;
	}
	
	
	/**
	 * @param controlState the controlState to set
	 */
	public final void setControlState(EAIControlState controlState)
	{
		this.controlState = controlState;
	}
	
	
	@Override
	public WorldFrame getRecordWfFrame()
	{
		return worldFrame;
	}
	
	
	@Override
	public Command getRefereeCmd()
	{
		return refereeMsgCached.getCommand();
	}
	
	
	@Override
	public TacticalField getTacticalInfo()
	{
		return tacticalInfo;
	}
	
	
	@Override
	public PlayStrategy getPlayStrategy()
	{
		return playStrategy;
	}
	
	
	@Override
	public void setPaths(List<Path> paths)
	{
		this.paths = paths;
	}
	
	
	@Override
	public List<Path> getPaths()
	{
		return paths;
	}
	
}
