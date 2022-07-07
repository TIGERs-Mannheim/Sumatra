/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.processors;

import java.nio.file.Path;


public interface ISizeAnalyzer<T>
{
	void process(T frame);

	void save(Path outputFolder);
}
