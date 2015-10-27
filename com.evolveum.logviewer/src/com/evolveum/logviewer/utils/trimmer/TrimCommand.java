package com.evolveum.logviewer.utils.trimmer;

public class TrimCommand extends Command {

	private int keepLines;

	public TrimCommand(String key, int lines) {
		super(key);
		keepLines = lines;
	}

	public int getKeepLines() {
		return keepLines;
	}
	
}
