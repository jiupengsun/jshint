package com.eclipsesource.jshint.Problem;

public class SysoutProblemHandler extends ProblemHandlerEx {

	@Override
	public void handleProblem(Problem problem) {
		// TODO Auto-generated method stub
		int line = problem.getLine();
		String message = "Problem in file " + this.fileName + " at line " + line
				+ ": " + problem.getMessage();
		System.out.println(message);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
