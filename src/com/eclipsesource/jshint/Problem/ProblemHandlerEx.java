package com.eclipsesource.jshint.Problem;

public class ProblemHandlerEx implements ProblemHandler {

	protected String fileName;

	public void setFileName(String file) {
		this.fileName = file;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public void handleProblem(Problem problem) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * 
	 * 2016Äê3ÔÂ14ÈÕ
	 * @author Jiupeng
	 * @description to do some destroy work
	 * @reference 
	 * @interpretation
	 */
	public void destroy() {
	}

}
