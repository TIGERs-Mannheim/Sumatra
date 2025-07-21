/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.communication;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemAck;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.thread.GeneralPurposeTimer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

import java.util.Deque;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * Manages retransmission and acknowledgments of reliable commands
 */
@Log4j2
@RequiredArgsConstructor
public class ReliableCmdManager
{
	private static final int RETRY_TIMEOUT = 100;
	private final BotID botId;
	private final IReliableCmdSender cmdSender;
	private int nextSeq = 0;
	private int lastRxSeq = -1;
	private final Deque<ACommand> commandQueue = new ConcurrentLinkedDeque<>();
	private TimerTask retransmitTask;


	public ECommandVerdict processOutgoingCommand(final ACommand cmd)
	{
		if (!cmd.isReliable())
		{
			return ECommandVerdict.PASS;
		}

		ECommandVerdict verdict;

		synchronized (commandQueue)
		{
			if (commandQueue.isEmpty())
			{
				verdict = ECommandVerdict.PASS;
			} else
			{
				verdict = ECommandVerdict.DROP;
			}

			cmd.setSeq(nextSeq); // assign new sequence number
			++nextSeq;
			nextSeq %= 0xFFFF;

			commandQueue.addLast(cmd);

			log.trace(
					"{} Enqueued command {} with SEQ {}, queue size: {}", botId, cmd.getType(), cmd.getSeq(),
					commandQueue.size()
			);

			if (retransmitTask == null)
			{
				scheduleRetransmit();
			}
		}

		return verdict;
	}


	public ECommandVerdict processIncomingCommand(final ACommand cmd)
	{
		if (cmd.isReliable())
		{
			// this is a reliable command from the bot, send ACK
			log.trace("{} ACK'd {} from bot with SEQ {}", botId, cmd.getType(), cmd.getSeq());
			cmdSender.sendReliableCmdOutput(new Output(new TigerSystemAck(cmd.getSeq())));

			if (cmd.getSeq() == lastRxSeq)
			{
				log.debug("{} duplicate command {} from bot received, SEQ: {}", botId, cmd.getType(), cmd.getSeq());
				return ECommandVerdict.DROP;
			}

			lastRxSeq = cmd.getSeq();

			return ECommandVerdict.PASS;
		}

		if (cmd.getType() == ECommand.CMD_SYSTEM_ACK)
		{
			TigerSystemAck ack = (TigerSystemAck) cmd;
			processAck(ack.getSeq());
		}

		return ECommandVerdict.PASS;
	}


	public void clear()
	{
		synchronized (commandQueue)
		{
			cancelRetransmit();
			commandQueue.clear();
		}
	}


	private void processAck(int seq)
	{
		synchronized (commandQueue)
		{
			if (commandQueue.isEmpty())
			{
				// This was a stray/late ACK, just ignore it
				return;
			}

			if (seq == commandQueue.getFirst().getSeq())
			{
				log.trace("{} {} ACK with SEQ {}", botId, commandQueue.getFirst().getType(), seq);

				commandQueue.removeFirst();
				cancelRetransmit();

				if (!commandQueue.isEmpty())
				{
					cmdSender.sendReliableCmdOutput(new Output(commandQueue.getFirst()));
					scheduleRetransmit();
				}
			} else
			{
				log.warn(
						"{} Out-of-order ACK received. Expected SEQ {} but got {}", botId, commandQueue.getFirst().getSeq(),
						seq
				);
			}
		}
	}


	private void cancelRetransmit()
	{
		if (retransmitTask != null)
		{
			retransmitTask.cancel();
			retransmitTask = null;
		}
	}


	private void scheduleRetransmit()
	{
		retransmitTask = new RetransmitTask();
		GeneralPurposeTimer.getInstance().schedule(retransmitTask, RETRY_TIMEOUT);
	}


	private class RetransmitTask extends TimerTask
	{
		@Override
		public void run()
		{
			synchronized (commandQueue)
			{
				if (!commandQueue.isEmpty())
				{
					var cmd = commandQueue.getFirst();
					cmd.incRetransmits();

					if(cmd.getRetransmits() < 20)
					{
						log.debug(
								"{} Retransmitting {} with sequence {} the {} time", botId, cmd.getType(), cmd.getSeq(),
								cmd.getRetransmits()
						);

						cmdSender.sendReliableCmdOutput(new Output(cmd));
						scheduleRetransmit();
						return;
					}

					log.warn("{} Too many retransmits for cmd {}", botId, cmd.getType());
					commandQueue.removeFirst();
					if(!commandQueue.isEmpty())
					{
						cmdSender.sendReliableCmdOutput(new Output(commandQueue.getFirst()));
						scheduleRetransmit();
					}
				}
			}
		}
	}

	public interface IReliableCmdSender
	{
		void sendReliableCmdOutput(Output out);
	}

	@Value
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Output
	{
		ACommand cmd;
	}
}
