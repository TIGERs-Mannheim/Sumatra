/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots.communication;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemAck;
import edu.tigers.sumatra.thread.GeneralPurposeTimer;


/**
 * Manages retransmission and acknowledgments of reliable commands
 * 
 * @author AndreR
 */
public class ReliableCmdManager
{
	private static final Logger log = Logger.getLogger(ReliableCmdManager.class.getName());
	private static final int RETRY_TIMEOUT = 100;
	private final Map<Integer, TimerTask> activeCmds = new ConcurrentHashMap<>();
	private final ABot bot;
	private int nextSeq = 0;
	
	
	/**
	 * Reliable command manager
	 * 
	 * @param bot
	 */
	public ReliableCmdManager(final ABot bot)
	{
		this.bot = bot;
	}
	
	/**
	 * Process an outgoing command and check if it is a reliable one.
	 * 
	 * @param cmd
	 */
	public void outgoingCommand(final ACommand cmd)
	{
		if (!cmd.isReliable())
		{
			return; // not a reliable command
		}
		
		if (cmd.getSeq() == -1) // first time we see this command?
		{
			TimerTask tTask = activeCmds.remove(nextSeq);
			if (tTask != null)
			{
				tTask.cancel(); // just in case this sequence number is still used, we delete it. this command is lost!
			}
			
			cmd.setSeq(nextSeq); // assign new sequence number
			++nextSeq;
			nextSeq %= 0xFFFF;
		}
		
		cmd.incRetransmits();
		if (cmd.getRetransmits() > 20)
		{
			log.warn("Too many retransmits for cmd " + cmd.getType());
		} else
		{
			CommandTimeout timeout = new CommandTimeout(cmd);
			GeneralPurposeTimer.getInstance().schedule(timeout, RETRY_TIMEOUT);
			activeCmds.put(cmd.getSeq(), timeout);
		}
	}
	
	
	/**
	 * Process an incomming command.
	 * 
	 * @param cmd
	 */
	public void incommingCommand(final ACommand cmd)
	{
		if (cmd.isReliable())
		{
			// this is a reliable command from the bot, send ACK
			bot.execute(new TigerSystemAck(cmd.getSeq()));
			
			return;
		}
		
		if (cmd.getType() == ECommand.CMD_SYSTEM_ACK)
		{
			TigerSystemAck ack = (TigerSystemAck) cmd;
			
			TimerTask tTask = activeCmds.remove(ack.getSeq());
			if (tTask != null)
			{
				tTask.cancel();
			}
		}
	}
	
	private class CommandTimeout extends TimerTask
	{
		private final ACommand cmd;
		
		
		/**
		 * @param cmd
		 */
		public CommandTimeout(final ACommand cmd)
		{
			this.cmd = cmd;
		}
		
		
		@Override
		public void run()
		{
			log.debug("Resend command " + cmd.getType());
			bot.execute(cmd);
		}
	}
}
