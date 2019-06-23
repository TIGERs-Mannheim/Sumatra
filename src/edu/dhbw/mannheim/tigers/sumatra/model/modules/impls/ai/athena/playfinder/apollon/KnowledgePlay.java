/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.PlayAndRoleCount;


/**
 * A KnowledgePlay contains a play with the number of roles and their associated
 * successful and failed fields
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Entity
public class KnowledgePlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private KnowledgeFields		successFields	= new KnowledgeFields();
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private KnowledgeFields		failedFields	= new KnowledgeFields();
	
	private PlayAndRoleCount	play;
	
	@Id
	private String					id;
	
	@Version
	private long					version;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param play
	 */
	public KnowledgePlay(PlayAndRoleCount play)
	{
		this.play = play;
		id = play.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public void resetCounters()
	{
		successFields.resetCounter();
		failedFields.resetCounter();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public KnowledgeFields getSuccessFields()
	{
		return successFields;
	}
	
	
	/**
	 * @param successField
	 */
	public void addSuccessField(AKnowledgeField successField)
	{
		successFields.add(successField);
	}
	
	
	/**
	 * @return
	 */
	public KnowledgeFields getFailedFields()
	{
		return failedFields;
	}
	
	
	/**
	 * @param failedField
	 */
	public void addFailedField(AKnowledgeField failedField)
	{
		failedFields.add(failedField);
	}
	
	
	/**
	 * @return the play
	 */
	public PlayAndRoleCount getPlay()
	{
		return play;
	}
	
	
	/**
	 * @param play the play to set
	 */
	public void setPlay(PlayAndRoleCount play)
	{
		this.play = play;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		return (prime * result) + ((play == null) ? 0 : play.hashCode());
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final KnowledgePlay other = (KnowledgePlay) obj;
		if (play == null)
		{
			if (other.play != null)
			{
				return false;
			}
		} else if (!play.equals(other.play))
		{
			return false;
		}
		return true;
	}
	
	
	@Override
	public String toString()
	{
		return play.toString();
	}
	
	
	/**
	 * @param successFields the successFields to set
	 */
	public final void setSuccessFields(KnowledgeFields successFields)
	{
		this.successFields = successFields;
	}
	
	
	/**
	 * @param failedFields the failedFields to set
	 */
	public final void setFailedFields(KnowledgeFields failedFields)
	{
		this.failedFields = failedFields;
	}
	
	
	/**
	 * @return the id
	 */
	public final String getId()
	{
		return id;
	}
	
	
	/**
	 * @return the version
	 */
	public final long getVersion()
	{
		return version;
	}
}
