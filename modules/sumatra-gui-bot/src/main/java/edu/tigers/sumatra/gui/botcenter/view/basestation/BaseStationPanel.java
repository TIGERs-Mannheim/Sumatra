package edu.tigers.sumatra.gui.botcenter.view.basestation;

import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;


/**
 * A panel showing details about a {@link TigersBaseStation}
 */
public class BaseStationPanel extends JPanel
{
	private final BaseStationControlPanel controlPanel = new BaseStationControlPanel();
	private final BaseStationWifiStatsPanel wifiStatsPanel = new BaseStationWifiStatsPanel();
	private final BaseStationEthStatsPanel ethStatsPanel = new BaseStationEthStatsPanel();


	public BaseStationPanel()
	{
		setName("Base Station");
		setLayout(new MigLayout("wrap 1"));

		add(wifiStatsPanel);
		add(controlPanel);
		add(ethStatsPanel);
	}


	/**
	 * @return the baseStationStatsPanel
	 */
	public BaseStationWifiStatsPanel getWifiStatsPanel()
	{
		return wifiStatsPanel;
	}


	/**
	 * @return the ethStatsPanel
	 */
	public BaseStationEthStatsPanel getEthStatsPanel()
	{
		return ethStatsPanel;
	}


	/**
	 * @return
	 */
	public BaseStationControlPanel getControlPanel()
	{
		return controlPanel;
	}
}
