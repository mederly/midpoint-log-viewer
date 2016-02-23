package com.evolveum.logviewer.utils.truncater;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Truncater {

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Usage: Truncater logfile");
			return;
		}
		RandomAccessFile file = new RandomAccessFile(args[0], "rw");
		file.setLength(0);
		file.close();
		System.out.println("File " + args[0] + " successfully truncated to zero size.");
	}

}
