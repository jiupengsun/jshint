package com.eclipsesource.jshint.Problem;

import java.io.File;

import com.eclipsesource.jshint.HTMLReport;

public class HTMLProblemHandler extends ProblemHandlerEx {
	private File outputFile;

	public HTMLProblemHandler(File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void handleProblem(Problem problem) {
		// TODO Auto-generated method stub
		int line = problem.getLine();
		String message = "Problem in file " + this.fileName + " at line " + line
				+ ": " + problem.getMessage();
		HTMLReport.getInstance().insert(this.fileName, message);
	}

	@Override
	public void destroy() {
		HTMLReport.getInstance().flush(outputFile);
	}
}
