package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.athena.Athena;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.common.TimeSeriesStatisticsSaver;
import edu.tigers.sumatra.ai.metis.Metis;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;


/**
 * The AI takes a {@link WorldFrameWrapper} as input, processes the whole AI chain and produces an {@link AIInfoFrame}
 */
@Log4j2
public class Ai
{
	private AIInfoFrame previousAIFrame = null;
	private long lastRefMsgCounter = -1;

	private final Metis metis = new Metis();
	private final Athena athena;
	private final EAiTeam aiTeam;
	private final ASkillSystem skillSystem;
	private final TimeSeriesStatisticsSaver timeSeriesStatisticsSaver = new TimeSeriesStatisticsSaver();


	/**
	 * Create a new AI
	 *
	 * @param aiTeam
	 * @param skillSystem
	 */
	public Ai(EAiTeam aiTeam, ASkillSystem skillSystem)
	{
		this.aiTeam = aiTeam;
		this.skillSystem = skillSystem;
		this.athena = new Athena(skillSystem);
	}


	/**
	 * @param mode
	 */
	public void changeMode(final EAIControlState mode)
	{
		athena.changeMode(mode);
	}


	public void start()
	{
		metis.start();
	}


	/**
	 * Stop what has to be stopped
	 */
	public void stop()
	{
		skillSystem.emergencyStop(aiTeam.getTeamColor());
		metis.stop();
	}


	/**
	 * Process a {@link WorldFrame} (one AI cycle)
	 * Do not call this on the two module agents! Create your own agent, please :)
	 *
	 * @param wfw
	 * @return
	 */
	public AIInfoFrame processWorldFrame(final WorldFrameWrapper wfw)
	{
		RefereeMsg refereeMsg = wfw.getRefereeMsg();
		boolean newRefereeMsg = false;
		if ((refereeMsg != null) && isNewMessage(refereeMsg))
		{
			log.trace("Referee cmd: {}", refereeMsg.getCommand());
			newRefereeMsg = true;
		}

		BaseAiFrame baseAiFrame = new BaseAiFrame(wfw, newRefereeMsg, previousAIFrame, aiTeam);

		if (previousAIFrame == null)
		{
			// Skip first frame
			previousAIFrame = AIInfoFrame.fromBaseAiFrame(baseAiFrame);
			return null;
		}

		previousAIFrame.cleanUp();


		// ### Process!
		MetisAiFrame metisAiFrame;
		AthenaAiFrame athenaAiFrame = null;
		long startTime = System.nanoTime();
		try
		{
			// Analyze
			metisAiFrame = metis.process(baseAiFrame);

			// Choose and calculate behavior, execute skills, collect bot AI info
			athenaAiFrame = athena.process(metisAiFrame);

			// ### Populate used AIInfoFrame (for visualization etc)
			previousAIFrame = AIInfoFrame.builder()
					.baseAiFrame(baseAiFrame)
					.athenaAiFrame(athenaAiFrame)
					.build();
			captureTimeSeriesStats(startTime, previousAIFrame);
			return previousAIFrame;
		} catch (@SuppressWarnings("squid:S1181") final Throwable ex) // we want to catch runtime exceptions, too!
		{
			log.error("Exception in AI {}", aiTeam, ex);

			// # Undo everything we've done this cycle to restore previous state
			// - RefereeMsg
			if (refereeMsg != null)
			{
				lastRefMsgCounter--;
			}

			if (athenaAiFrame != null)
			{
				athena.onException(athenaAiFrame);
			}

			skillSystem.reset(aiTeam.getTeamColor());

			previousAIFrame.cleanUp();
			previousAIFrame = AIInfoFrame.fromBaseAiFrame(baseAiFrame);
		}
		return null;
	}


	private void captureTimeSeriesStats(long startTime, AIInfoFrame aiFrame)
	{
		long timestamp = aiFrame.getWorldFrame().getTimestamp();
		TimeSeriesStatsEntry metisDurations = new TimeSeriesStatsEntry("metis.duration", timestamp);
		TimeSeriesStatsEntry metisExecution = new TimeSeriesStatsEntry("metis.execution", timestamp);
		aiFrame.getAthenaAiFrame().getMetisAiFrame().getCalculatorExecutions().forEach((calc, exec) -> {
			metisDurations.addField(calc.getCanonicalName(), exec.getProcessingTime());
			metisExecution.addField(calc.getCanonicalName(), exec.isExecuted());
		});

		TimeSeriesStatsEntry ai = new TimeSeriesStatsEntry("ai", timestamp);
		ai.addField("systemTime", aiFrame.getWorldFrameWrapper().getUnixTimestamp());
		ai.addField("startTime", startTime);
		ai.addField("endTime", System.nanoTime());

		timeSeriesStatisticsSaver.add(aiFrame.getBaseAiFrame(), metisExecution);
		timeSeriesStatisticsSaver.add(aiFrame.getBaseAiFrame(), metisDurations);
		timeSeriesStatisticsSaver.add(aiFrame.getBaseAiFrame(), ai);
	}


	/**
	 * @param msg The recently received message
	 * @return Whether this message does really new game-state information
	 * @author FriederB
	 */
	private boolean isNewMessage(final RefereeMsg msg)
	{
		if (msg.getCmdCounter() != lastRefMsgCounter)
		{
			lastRefMsgCounter = msg.getCmdCounter();
			return true;
		}

		return false;
	}


	public EAiTeam getAiTeam()
	{
		return aiTeam;
	}


	public Athena getAthena()
	{
		return athena;
	}


	public Metis getMetis()
	{
		return metis;
	}


	public AIInfoFrame getLatestAiFrame()
	{
		return previousAIFrame;
	}
}
