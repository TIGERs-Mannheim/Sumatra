package edu.tigers.sumatra.aicenter.presenter;

import static edu.tigers.sumatra.aicenter.view.AthenaControlPanel.NUM_ROWS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.athena.AIControl;
import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.aicenter.view.AthenaControlPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.UiThrottler;


public class AthenaPresenter
{
	private static final Logger log = Logger.getLogger(AthenaPresenter.class.getName());
	private static final String REPLACE_PATTERN = "[,; ]";

	private final EAiTeam team;
	private final AthenaControlPanel athenaControlPanel;
	private final UiThrottler aiFrameThrottler = new UiThrottler(500);

	private boolean shown = false;
	
	private AIInfoFrame latestAIFrame = null;


	public AthenaPresenter(final EAiTeam team, final AthenaControlPanel athenaControlPanel)
	{
		this.team = team;
		this.athenaControlPanel = athenaControlPanel;

		athenaControlPanel.getTable().addPropertyChangeListener(new ChangeListener());
		athenaControlPanel.getBtnClearPlays().addActionListener(new ClearListener());
		athenaControlPanel.getBtnClearRoles().addActionListener(new ClearRolesListener());
		athenaControlPanel.getBtnAddAllToSelected().addActionListener(new AddAllToSelectedListener());

		aiFrameThrottler.start();
	}


	public void setShown(final boolean shown)
	{
		this.shown = shown;
	}


	public void updateAIInfoFrame(final AIInfoFrame lastFrame)
	{
		latestAIFrame = lastFrame;
		if (athenaControlPanel.getTable().isEnabled() || !shown)
		{
			// The table can be edited by the user -> do not override
			// The table is not shown -> don't update it
			return;
		}

		aiFrameThrottler.execute(() -> update(lastFrame));
	}


	private void update(final AIInfoFrame lastFrame)
	{
		for (int row = 0; row < NUM_ROWS; row++)
		{
			if (row < lastFrame.getPlayStrategy().getActivePlays().size())
			{
				APlay play = lastFrame.getPlayStrategy().getActivePlays().get(row);
				String desired = StringUtils.join(play.getRoles().stream()
						.map(ARole::getBotID).map(BotID::getNumber).collect(Collectors.toList()), ",");

				athenaControlPanel.getTable().getModel().setValueAt(play, row, AthenaControlPanel.EColumn.PLAY.getIdx());
				athenaControlPanel.getTable().getModel().setValueAt(desired, row,
						AthenaControlPanel.EColumn.DESIRED_BOTS.getIdx());
			} else
			{
				athenaControlPanel.clearRow(row);
			}
		}
	}


	private void sendRoleFinderInfos()
	{
		Map<EPlay, RoleMapping> infos = new EnumMap<>(EPlay.class);
		Map<EPlay, Boolean> useAiPlays = new EnumMap<>(EPlay.class);
		for (int row = 0; row < NUM_ROWS; row++)
		{
			String strPlay = athenaControlPanel.getTable().getModel()
					.getValueAt(row, AthenaControlPanel.EColumn.PLAY.getIdx()).toString();
			if (strPlay.isEmpty())
			{
				continue;
			}
			EPlay ePlay = EPlay.valueOf(strPlay);
			RoleMapping info = new RoleMapping();
			String desBots = athenaControlPanel.getTable().getModel()
					.getValueAt(row, AthenaControlPanel.EColumn.DESIRED_BOTS.getIdx()).toString();
			if (!desBots.isEmpty())
			{
				Set<String> desiredBots = Arrays.stream(desBots.split(REPLACE_PATTERN)).filter(e -> e.length() > 0)
						.collect(Collectors.toSet());
				
				for (String desiredBot : desiredBots)
				{
					info.getDesiredBots().add(BotID.createBotId(valueOrZero(desiredBot), team.getTeamColor()));
				}
			}
			Boolean override = Boolean
					.valueOf(athenaControlPanel.getTable().getModel().getValueAt(row, AthenaControlPanel.EColumn.AI.getIdx())
							.toString());
			infos.put(ePlay, info);
			useAiPlays.put(ePlay, override);
		}

		getAIControl().ifPresent(ai -> ai.getRoleMapping().clear());
		getAIControl().ifPresent(ai -> ai.getRoleMapping().putAll(infos));
		getAIControl().ifPresent(ai -> ai.getUseAiFlags().clear());
		getAIControl().ifPresent(ai -> ai.getUseAiFlags().putAll(useAiPlays));
	}


	private Optional<AIControl> getAIControl()
	{
		return SumatraModel.getInstance().getModuleOpt(Agent.class)
				.map(agent -> agent.getAi(team).orElse(null))
				.map(ai -> ai.getAthena().getAthenaAdapter().getAiControl());
	}


	private int valueOrZero(final String strInt)
	{
		if (StringUtils.isBlank(strInt))
		{
			return 0;
		}
		try
		{
			return Integer.valueOf(strInt);
		} catch (NumberFormatException err)
		{
			log.debug("Could not parse string: " + strInt, err);
		}
		return 0;
	}

	private class ChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(final PropertyChangeEvent evt)
		{
			if (athenaControlPanel.getTable().isEnabled())
			{
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (int i = 0; i < NUM_ROWS; i++)
				{
					String play = athenaControlPanel.getTable().getModel()
							.getValueAt(i, AthenaControlPanel.EColumn.PLAY.getIdx()).toString();
					if (play.isEmpty())
					{
						continue;
					}
					if (!first)
					{
						sb.append(",");
					}
					first = false;
					sb.append(play);
				}
				SumatraModel.getInstance().setUserProperty(AthenaControlPanel.class + ".plays", sb.toString());
				sendRoleFinderInfos();
			}
		}
	}

	private class ClearListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			athenaControlPanel.clear();
			sendRoleFinderInfos();
		}
	}
	
	private class AddAllToSelectedListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			int row = athenaControlPanel.getTable().getSelectedRow();
			for (int i = 0; i < athenaControlPanel.getTable().getRowCount(); i++)
			{
				athenaControlPanel.getTable().getModel().setValueAt("", i,
						AthenaControlPanel.EColumn.DESIRED_BOTS.getIdx());
			}
			Set<Integer> bots = latestAIFrame.getWorldFrame().getTigerBotsAvailable().keySet().stream()
					.map(BotID::getNumber).collect(Collectors.toSet());
			String botString = StringUtils.join(bots, " ");
			athenaControlPanel.getTable().getModel().setValueAt(botString, row,
					AthenaControlPanel.EColumn.DESIRED_BOTS.getIdx());
			sendRoleFinderInfos();
		}
	}
	

	private class ClearRolesListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			clearRoles();
			sendRoleFinderInfos();
		}


		private void clearRoles()
		{
			for (int j = 0; j < NUM_ROWS; j++)
			{
				athenaControlPanel.getTable().getModel().setValueAt(
						AthenaControlPanel.EColumn.values()[AthenaControlPanel.EColumn.DESIRED_BOTS.getIdx()].getDefValue(),
						j,
						AthenaControlPanel.EColumn.DESIRED_BOTS.getIdx());
			}
		}
	}
}
