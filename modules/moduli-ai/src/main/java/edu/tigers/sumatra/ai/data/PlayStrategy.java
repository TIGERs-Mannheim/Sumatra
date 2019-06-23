/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotIDMap;


/**
 * This stores all tactical information.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
@Persistent(version = 4)
public class PlayStrategy implements IPlayStrategy
{
	private transient List<APlay>	activePlays;
											
	/** Contains all finished plays of the last cycle */
	private transient List<APlay>	finishedPlays;
											
	private EAIControlState			aiControlState;
											
											
	@SuppressWarnings("unused")
	private PlayStrategy()
	{
		activePlays = new ArrayList<>();
		finishedPlays = new ArrayList<>();
	}
	
	
	/**
	 * @param builder
	 */
	public PlayStrategy(final Builder builder)
	{
		activePlays = Collections.unmodifiableList(builder.activePlays);
		finishedPlays = Collections.unmodifiableList(builder.finishedPlays);
		aiControlState = builder.controlState;
	}
	
	
	/**
	 * Get the number of roles (calculates the sum of roles of all plays)
	 * 
	 * @return
	 */
	@Override
	public int getNumRoles()
	{
		int sum = 0;
		for (APlay play : activePlays)
		{
			sum += play.getRoles().size();
		}
		return sum;
	}
	
	
	/**
	 * Warn: This will construct a new map each time it is called!
	 * 
	 * @return
	 */
	@Override
	public BotIDMap<ARole> getActiveRoles()
	{
		BotIDMap<ARole> roles = new BotIDMap<ARole>(6);
		for (APlay play : activePlays)
		{
			for (ARole role : play.getRoles())
			{
				roles.put(role.getBotID(), role);
			}
		}
		return roles;
	}
	
	
	/**
	 * Get roles by type
	 * 
	 * @param roleType
	 * @return
	 */
	@Override
	public List<ARole> getActiveRoles(final ERole roleType)
	{
		List<ARole> roles = new ArrayList<ARole>();
		for (APlay play : activePlays)
		{
			for (ARole role : play.getRoles())
			{
				if (role.getType() == roleType)
				{
					roles.add(role);
				}
			}
		}
		return roles;
	}
	
	
	/**
	 * Get roles by play
	 * 
	 * @param playType
	 * @return
	 */
	@Override
	public List<ARole> getActiveRoles(final EPlay playType)
	{
		List<ARole> roles = new ArrayList<ARole>();
		for (APlay play : activePlays)
		{
			if (play.getType() != playType)
			{
				continue;
			}
			for (ARole role : play.getRoles())
			{
				roles.add(role);
			}
		}
		return roles;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	@Override
	public List<APlay> getActivePlays()
	{
		return activePlays;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public List<APlay> getFinishedPlays()
	{
		return finishedPlays;
	}
	
	
	/**
	 * @return the controlState
	 */
	@Override
	public EAIControlState getAIControlState()
	{
		return aiControlState;
	}
	
	
	/**
	 * Use this builder to create a {@link PlayStrategy}
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class Builder
	{
		private List<APlay>		activePlays;
		private List<APlay>		finishedPlays;
		private EAIControlState	controlState;
										
										
		/**
		  * 
		  */
		public Builder()
		{
			activePlays = new ArrayList<APlay>();
			finishedPlays = new ArrayList<APlay>();
			controlState = EAIControlState.TEST_MODE;
		}
		
		
		/**
		 * Create a new {@link PlayStrategy} with the data in this Builder
		 * 
		 * @return
		 */
		public IPlayStrategy build()
		{
			return new PlayStrategy(this);
		}
		
		
		/**
		 * @return the activePlays
		 */
		public final List<APlay> getActivePlays()
		{
			return activePlays;
		}
		
		
		/**
		 * @param activePlays the activePlays to set
		 */
		public final void setActivePlays(final List<APlay> activePlays)
		{
			this.activePlays = activePlays;
		}
		
		
		/**
		 * @return the finishedPlays
		 */
		public final List<APlay> getFinishedPlays()
		{
			return finishedPlays;
		}
		
		
		/**
		 * @param finishedPlays the finishedPlays to set
		 */
		public final void setFinishedPlays(final List<APlay> finishedPlays)
		{
			this.finishedPlays = finishedPlays;
		}
		
		
		/**
		 * @return Athena's controlState
		 */
		public final EAIControlState getAIControlState()
		{
			return controlState;
		}
		
		
		/**
		 * @param controlState the controlState to set
		 */
		public final void setAIControlState(final EAIControlState controlState)
		{
			this.controlState = controlState;
		}
		
	}
}
