package edu.tigers.sumatra.view.toolbar;

import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.view.BaseStationPanel;
import edu.tigers.sumatra.view.FpsPanel;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Log4j2
public class ToolBar
{
	private final List<IToolbarObserver> observers = new CopyOnWriteArrayList<>();

	private final JToggleButton btnRecSave = new JToggleButton();
	private final JButton btnEmergency = new JButton();
	private final JToggleButton btnTournament = new JToggleButton();

	@Getter
	private final JToolBar jToolBar = new JToolBar();
	@Getter
	private final FpsPanel fpsPanel = new FpsPanel();
	@Getter
	private final JProgressBar heapBar = new JProgressBar();
	@Getter
	private final JLabel heapLabel = new JLabel();
	@Getter
	private final BaseStationPanel baseStationPanel = new BaseStationPanel();

	private EAIControlState yellowMode = EAIControlState.ACTIVE;
	private EAIControlState blueMode = EAIControlState.ACTIVE;

	private boolean emergencyAlarmActive = false;


	public ToolBar()
	{
		log.trace("Create toolbar");

		btnEmergency.addActionListener(actionEvent -> notifyEmergencyStop());
		btnEmergency.setToolTipText("Emergency stop [Esc]");
		btnEmergency.setBorder(BorderFactory.createEmptyBorder());
		btnEmergency.setBackground(new Color(0, 0, 0, 1));

		btnRecSave.addActionListener(actionEvent -> toggleRecord());
		btnRecSave.setToolTipText("Start/Stop recording");
		btnRecSave.setBorder(BorderFactory.createEmptyBorder());
		btnRecSave.setBackground(new Color(0, 0, 0, 1));

		btnTournament.addActionListener(this::toggleTournamentMode);
		btnTournament.setToolTipText("Tournament mode");
		btnTournament.setBorder(BorderFactory.createEmptyBorder());
		btnTournament.setBackground(new Color(0, 0, 0, 1));
		btnTournament.setContentAreaFilled(false);

		JPanel heapPanel = new JPanel(new BorderLayout());
		heapLabel.setToolTipText("Memory Usage (current/total/maximum)");
		heapPanel.add(heapLabel, BorderLayout.NORTH);
		heapPanel.add(heapBar, BorderLayout.SOUTH);

		heapBar.setStringPainted(true);
		heapBar.setMinimum(0);

		jToolBar.setFloatable(false);
		jToolBar.setRollover(true);

		JPanel toolBarPanel = new JPanel(new MigLayout("inset 1"));

		toolBarPanel.add(btnEmergency, "left");
		toolBarPanel.add(btnRecSave, "left");
		toolBarPanel.add(btnTournament, "left");
		toolBarPanel.add(fpsPanel, "left");
		toolBarPanel.add(baseStationPanel, "left");
		toolBarPanel.add(heapPanel, "left");

		jToolBar.add(toolBarPanel);
		jToolBar.addPropertyChangeListener("foreground", _ -> updateIcons());

		updateIcons();
	}


	private void updateIcons()
	{
		Color c = UIManager.getColor("Label.foreground");

		btnTournament.setIcon(btnTournament.isSelected()
				? IconFontSwing.buildIcon(FontAwesome.TROPHY, 28, new Color(212, 175, 55))
				: IconFontSwing.buildIcon(FontAwesome.TROPHY, 28, c));

		btnRecSave.setIcon(btnRecSave.isSelected()
				? IconFontSwing.buildIcon(FontAwesome.STOP_CIRCLE_O, 28, new Color(0, 180, 0))
				: IconFontSwing.buildIcon(FontAwesome.DOT_CIRCLE_O, 28, c));

		if (!emergencyAlarmActive)
		{
			btnEmergency.setIcon(IconFontSwing.buildIcon(FontAwesome.STOP_CIRCLE, 28, Color.RED));
		}
	}


	public void setRecordingEnabled(final boolean recording)
	{
		btnRecSave.setSelected(recording);
		updateIcons();
	}


	private void notifyEmergencyStop()
	{
		observers.forEach(IToolbarObserver::onEmergencyStop);
	}


	public void onAiModeChanged(EAiTeam aiTeam, EAIControlState mode)
	{
		if (aiTeam == EAiTeam.BLUE)
		{
			blueMode = mode;
		} else if (aiTeam == EAiTeam.YELLOW)
		{
			yellowMode = mode;
		}

		emergencyAlarmActive =
				blueMode == EAIControlState.EMERGENCY_MODE
						|| yellowMode == EAIControlState.EMERGENCY_MODE;

		if (emergencyAlarmActive)
		{
			Color fg = UIManager.getColor("Label.foreground");

			boolean whiteMode =
					(fg != null)
							&& ((fg.getRed() + fg.getGreen() + fg.getBlue()) < 382);

			btnEmergency.setIcon(
					ImageScaler.scaleDefaultButtonImageIcon(
							whiteMode
									? "/alarm_white.gif"
									: "/alarm.gif"));
		} else
		{
			updateIcons();
		}
	}


	private void toggleTournamentMode(ActionEvent e)
	{
		JToggleButton btn = (JToggleButton) e.getSource();
		SumatraModel.getInstance().setTournamentMode(btn.isSelected());
		updateIcons();
	}


	private void toggleRecord()
	{
		btnRecSave.setEnabled(false);

		new Thread(
				() ->
				{
					observers.forEach(IToolbarObserver::onToggleRecord);
					btnRecSave.setEnabled(true);
					updateIcons();
				}, "RecordSaveButton"
		).start();
	}


	public void addObserver(final IToolbarObserver o)
	{
		observers.add(o);
	}


	public void removeObserver(final IToolbarObserver o)
	{
		observers.remove(o);
	}


	public enum EAIControlState
	{
		ACTIVE,
		EMERGENCY_MODE,
		OFF
	}
}