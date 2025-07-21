/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands;

/**
 * Base for all commands.
 */
public abstract class ACommand
{
	private final ECommand cmd;
	private boolean reliable = false;
	private int seq = -1;
	private int retransmits = 0;
	
	
	protected ACommand(final ECommand cmd)
	{
		this.cmd = cmd;
	}
	
	
	protected ACommand(final ECommand cmd, final boolean reliable)
	{
		this.cmd = cmd;
		this.reliable = reliable;
	}
	
	
	/**
	 * @return
	 */
	public ECommand getType()
	{
		return cmd;
	}
	
	
	/**
	 * @return the reliable
	 */
	public boolean isReliable()
	{
		return reliable;
	}
	
	
	/**
	 * @param reliable the reliable to set
	 */
	public ACommand setReliable(final boolean reliable)
	{
		this.reliable = reliable;
		return this;
	}
	
	
	/**
	 * @return the seq
	 */
	public int getSeq()
	{
		return seq;
	}
	
	
	/**
	 * @param seq the seq to set
	 */
	public void setSeq(final int seq)
	{
		this.seq = seq;
	}
	
	
	/**
	 * @return the retransmits
	 */
	public final int getRetransmits()
	{
		return retransmits;
	}
	
	
	/**
	 * Increase number of retransmits.
	 */
	public final void incRetransmits()
	{
		retransmits++;
	}
}
