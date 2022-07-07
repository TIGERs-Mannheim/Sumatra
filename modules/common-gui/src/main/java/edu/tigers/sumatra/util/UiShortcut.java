/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.awt.Component;
import java.awt.KeyEventDispatcher;


@Value
@Builder
public class UiShortcut
{
	@NonNull
	String name;
	@NonNull
	String keys;
	@NonNull
	Component component;
	@NonNull
	KeyEventDispatcher dispatcher;
}
