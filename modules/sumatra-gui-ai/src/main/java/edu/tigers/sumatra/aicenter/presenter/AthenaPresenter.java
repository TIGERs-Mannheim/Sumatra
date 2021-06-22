/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter.presenter;

import com.github.g3force.instanceables.IInstanceableObserver;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.athena.AthenaGuiInput;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.aicenter.view.AthenaControlPanel;
import edu.tigers.sumatra.aicenter.view.RoleControlPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.aicenter.view.AthenaControlPanel.EColumn.BOTS;
import static edu.tigers.sumatra.aicenter.view.AthenaControlPanel.EColumn.PLAY;
import static edu.tigers.sumatra.aicenter.view.AthenaControlPanel.NUM_ROWS;


@Log4j2
public class AthenaPresenter
{
	private final EAiTeam team;
	private final AthenaControlPanel athenaControlPanel;

	private static final String BOT_IDS_KEY = RoleControlPanel.class.getName() + ".botids";


	public AthenaPresenter(final EAiTeam team, final AthenaControlPanel athenaControlPanel)
	{
		this.team = team;
		this.athenaControlPanel = athenaControlPanel;

		athenaControlPanel.getBtnAssign().addActionListener(new AssignListener());
		athenaControlPanel.getBtnUnassign().addActionListener(new UnassignListener());
		athenaControlPanel.getBtnClearRoles().addActionListener(new ClearRolesListener());
		athenaControlPanel.getBtnRemovePlay().addActionListener(new RemovePlayListener());
		athenaControlPanel.getBtnClearPlays().addActionListener(new ClearPlaysListener());
		athenaControlPanel.getInstanceablePanel().addObserver(new CreatePlayListener());
		athenaControlPanel.getBotIdList().addListSelectionListener(new BotIdListSelectionListener());

		String botIdsStr = SumatraModel.getInstance().getUserProperty(BOT_IDS_KEY);
		if (botIdsStr != null)
		{
			var botIds = Arrays.stream(botIdsStr.split(",")).map(Integer::parseInt).mapToInt(i -> i).toArray();
			athenaControlPanel.getBotIdList().setSelectedIndices(botIds);
		}
	}


	public void update()
	{
		getAthenaGuiInput().map(AthenaGuiInput::getPlays).ifPresent(this::updateTable);
	}


	private void updateTable(final List<APlay> plays)
	{
		for (int row = 0; row < NUM_ROWS; row++)
		{
			if (row > plays.size() - 1)
			{
				athenaControlPanel.getTable().getModel().setValueAt("", row, PLAY.ordinal());
				athenaControlPanel.getTable().getModel().setValueAt("", row, BOTS.ordinal());
			} else
			{
				APlay play = plays.get(row);
				String botIds = StringUtils
						.join(play.getRoles().stream().map(ARole::getBotID).map(BotID::getNumber)
								.collect(Collectors.toList()), ",");
				athenaControlPanel.getTable().getModel().setValueAt(play.getType().name(), row, PLAY.ordinal());
				athenaControlPanel.getTable().getModel().setValueAt(botIds, row, BOTS.ordinal());
			}
		}
	}


	private Optional<AthenaGuiInput> getAthenaGuiInput()
	{
		return SumatraModel.getInstance().getModuleOpt(Agent.class)
				.flatMap(agent -> agent.getAi(team))
				.map(ai -> ai.getAthena().getAthenaGuiInput());
	}


	private List<EPlay> getSelectedPlays()
	{
		int[] selectedRows = athenaControlPanel.getTable().getSelectedRows();
		List<EPlay> plays = new ArrayList<>();
		for (int row : selectedRows)
		{
			String playString = (String) athenaControlPanel.getTable().getValueAt(row, PLAY.ordinal());
			if (StringUtils.isNotBlank(playString))
			{
				plays.add(EPlay.valueOf(playString));
			}
		}
		return plays;
	}


	private Set<BotID> getSelectedBots()
	{
		return athenaControlPanel.getBotIdList().getSelectedValuesList().stream()
				.map(i -> BotID.createBotId(i, team.getTeamColor()))
				.collect(Collectors.toSet());
	}


	private class AssignListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			var plays = getSelectedPlays();
			if (plays.isEmpty())
			{
				return;
			}
			var firstPlay = plays.get(0);
			var bots = getSelectedBots();

			getAthenaGuiInput()
					.ifPresent(in -> in.getRoleMapping().entrySet().stream()
							.filter(entry -> entry.getKey() != firstPlay)
							.map(Map.Entry::getValue)
							.forEach(bots::removeAll));
			getAthenaGuiInput()
					.ifPresent(in -> plays.stream().findFirst().ifPresent(p -> in.getRoleMapping().put(p, bots)));
		}
	}


	private class UnassignListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			var plays = getSelectedPlays();
			var bots = getSelectedBots();

			getAthenaGuiInput().ifPresent(in ->
					plays.forEach(p ->
							Optional.ofNullable(in.getRoleMapping().get(p))
									.ifPresent(ids -> ids.removeAll(bots))));
		}
	}

	private class ClearRolesListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			var plays = getSelectedPlays();
			getAthenaGuiInput().ifPresent(in -> plays.forEach(p -> in.getRoleMapping().remove(p)));
		}
	}

	private class RemovePlayListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			var plays = getSelectedPlays();
			getAthenaGuiInput().ifPresent(in -> plays.forEach(p -> in.getRoleMapping().remove(p)));
			getAthenaGuiInput().ifPresent(in -> plays.forEach(p -> in.getPlays().removeIf(pl -> pl.getType() == p)));
		}
	}

	private class ClearPlaysListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			getAthenaGuiInput().ifPresent(in -> in.getPlays().clear());
			getAthenaGuiInput().ifPresent(in -> in.getRoleMapping().clear());
		}
	}

	private class CreatePlayListener implements IInstanceableObserver
	{
		@Override
		public void onNewInstance(final Object object)
		{
			var play = (APlay) object;
			getAthenaGuiInput().ifPresent(in -> in.getPlays().add(play));
		}
	}

	private class BotIdListSelectionListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(final ListSelectionEvent e)
		{
			var selectedValues = athenaControlPanel.getBotIdList().getSelectedValuesList().stream()
					.map(Object::toString)
					.collect(Collectors.toList());
			var valueString = StringUtils.join(selectedValues, ",");
			SumatraModel.getInstance().setUserProperty(BOT_IDS_KEY, valueString);
		}
	}
}
