package com.eclipsesource.jshint.Problem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileOutProblemHandler extends ProblemHandlerEx {

	private File outputFile;
	private PrintWriter pw;

	public FileOutProblemHandler(File outputFile) {
		this.outputFile = outputFile;
		if (this.outputFile != null)
			try {
				pw = new PrintWriter(new FileWriter(this.outputFile, true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			throw new NullPointerException();
	}

	@Override
	public void handleProblem(Problem problem) {
		// TODO Auto-generated method stub
		int line = problem.getLine();
		String message = "Problem in file " + fileName + " at line " + line + ": "
				+ problem.getMessage();
		pw.println(message);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (pw != null) {
			pw.flush();
			pw.close();
			pw = null;
		}
	}
}
