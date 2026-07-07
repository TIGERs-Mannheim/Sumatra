package edu.tigers.sumatra.util;

import javax.swing.KeyStroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * This class provides a focus listener for suppressing shortcuts.
 * These shortcuts, by default, are Space, Left ArrowKey and Right ArrowKey
 * They can be changed with the addShortcut, clearShortcuts and addShortcuts methods
 * You can also trigger the suppression and unsupression manually by calling their respective methods
 */
public class ShortcutSuppressor implements FocusListener
{
	private List<KeyStroke> suppressedKeys;


	public ShortcutSuppressor()
	{
		this.suppressedKeys = new ArrayList<>();
		suppressedKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		suppressedKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		suppressedKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
	}


	/**
	 * This adds a shortcut by keystroke that should be suppressed
	 */
	public void addShortcut(KeyStroke keyStroke)
	{
		suppressedKeys.add(keyStroke);
	}


	/**
	 * This adds a shortcut by keystroke-List that should be suppressed
	 */
	public void addShortcuts(List<KeyStroke> keyStrokes)
	{
		suppressedKeys.addAll(keyStrokes);
	}


	public void suppressShortcuts()
	{
		suppressedKeys.forEach(GlobalShortcuts::disableByKeyStroke);
	}


	public void unsupressShortcuts()
	{
		GlobalShortcuts.enableAllShortcuts();
	}


	/**
	 * This clears all shortcuts that should be suppressed
	 */
	public void clearShortcuts()
	{
		suppressedKeys.clear();
	}


	@Override
	public void focusGained(FocusEvent e)
	{
		suppressShortcuts();
	}


	@Override
	public void focusLost(FocusEvent e)
	{
		unsupressShortcuts();
	}
}
