package edu.tigers.sumatra.persistence.processors;

import java.nio.file.Path;


public interface IPersistenceDbAnalyzer<T>
{
	void process(T frame);

	void save(Path outputFolder);
}
