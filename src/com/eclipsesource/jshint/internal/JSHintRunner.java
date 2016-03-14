/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.eclipsesource.jshint.HTMLReport;
import com.eclipsesource.jshint.JSHint;
import com.eclipsesource.jshint.Problem;
import com.eclipsesource.jshint.ProblemHandler;
import com.eclipsesource.json.JsonObject;

public class JSHintRunner {

	private static final String PARAM_CHARSET = "--charset";
	private static final String PARAM_CUSTOM_JSHINT = "--custom";
	private static final String PARAM_CONFIGURATION = "--config";
	private static final String PARAM_OUTPUTFILE = "--output";
	private static final String CONFIG_BLACKLIST = "blackList";
	private List<File> files;
	private Map<String, String> blackFiles = new HashMap<String, String>();
	private Charset charset;
	private File library;
	private File outputFile;
	private File configFile;
	private JSHint jshint;

	public void run(String... args) {
		try {
			readArgs(args);
			ensureCharset();
			ensureInputFiles();
			loadJSHint();
			configureJSHint();
			processFiles();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println();
			System.out.println(
					"Usage: JSHint [ <options> ] <input-file> [ <input-file> ... ]");
			System.out.println("Options: --custom <custom-jshint-file>");
			System.out.println("         --config <config-jshint.properties>");
			System.out.println("         --output <output-report-file>");
		}
	}

	private void readArgs(String[] args) {
		files = new ArrayList<File>();
		String lastArg = null;
		for (String arg : args) {
			if (PARAM_CHARSET.equals(lastArg)) {
				setCharset(arg);
			} else if (PARAM_CUSTOM_JSHINT.equals(lastArg)) {
				setLibrary(arg);
			} else if (PARAM_CONFIGURATION.equals(lastArg)) {
				setConfiguration(arg);
			} else if (PARAM_OUTPUTFILE.equals(lastArg)) {
				setOutputFile(arg);
			} else if (PARAM_CHARSET.equals(arg) || PARAM_CUSTOM_JSHINT.equals(arg)
					|| PARAM_CONFIGURATION.equals(arg) || PARAM_OUTPUTFILE.equals(arg)) {
				// continue
			} else {
				//added by jiupeng
				//check whether this string is a file or directory, if the latter one, recursively loaded js files
				File file = new File(arg);
				if (file.isDirectory()) {
					//directory
					loadJsFiles(file, files);
				} else {
					checkFile(file);
					files.add(file);
				}
			}
			lastArg = arg;
		}
	}

	/**
	 * 
	 * @param directory
	 * 2016年3月4日
	 * @author Jiupeng
	 * @description load js files in the directory recusively
	 * @reference 
	 * @interpretation
	 */
	private void loadJsFiles(File directory, List<File> files) {
		File[] subFiles = directory.listFiles();
		for (File f : subFiles) {
			if (f.isFile() && f.getName().endsWith(".js")) {
				files.add(f);
			} else if (f.isDirectory())
				loadJsFiles(f, files);
		}
	}

	private void checkFile(File file) throws IllegalArgumentException {
		if (!file.isFile()) {
			throw new IllegalArgumentException(
					"No such file: " + file.getAbsolutePath());
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException(
					"Cannot read file: " + file.getAbsolutePath());
		}
	}

	private void ensureCharset() {
		if (charset == null) {
			setCharset("UTF-8");
		}
	}

	private void setCharset(String name) {
		try {
			charset = Charset.forName(name);
		} catch (Exception exception) {
			throw new IllegalArgumentException(
					"Unknown or unsupported charset: " + name);
		}
	}

	private void setLibrary(String name) {
		library = new File(name);
	}

	private void setConfiguration(String config) {
		configFile = new File(config);
	}

	private void setOutputFile(String output) {
		outputFile = new File(output);
	}

	private void ensureInputFiles() {
		if (files.isEmpty()) {
			throw new IllegalArgumentException("No input files");
		}
	}

	private void loadJSHint() {
		jshint = new JSHint();
		try {
			if (library != null) {
				FileInputStream inputStream = new FileInputStream(library);
				try {
					jshint.load(inputStream);
				} finally {
					inputStream.close();
				}
			} else {
				jshint.load();
			}
		} catch (Exception exception) {
			String message = "Failed to load JSHint library: "
					+ exception.getMessage();
			throw new IllegalArgumentException(message);
		}
	}

	private void processFiles() throws IOException {
		for (File file : files) {
			if (blackFiles.containsKey(file.getName()))
				continue;
			String code = readFileContents(file);
			ProblemHandler handler = new SysoutProblemHandler(file.getAbsolutePath(),
					outputFile);
			jshint.check(code, handler);
			handler.destroy();
		}
	}

	/**
	 * 
	 * 
	 * 2016年3月7日
	 * @author Jiupeng
	 * @description
	 * @reference load configuration file and load inside parameters
	 * @interpretation
	 */
	private void configureJSHint() {
		JsonObject configuration = new JsonObject();
		if (configFile != null) {
			Properties property = new Properties();
			try {
				property.load(new FileReader(configFile));
				String blacklist;
				if ((blacklist = property.getProperty(CONFIG_BLACKLIST)) != null) {
					String[] blackfiles = blacklist.trim().split(" ");
					for (String s : blackfiles)
						blackFiles.put(s, s);
					property.remove(CONFIG_BLACKLIST);
					Iterator<String> propNames = property.stringPropertyNames()
							.iterator();
					while (propNames.hasNext()) {
						String key = propNames.next();
						configuration.add(key, property.getProperty(key));
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//configuration.add("undef", true);
		//configuration.add("devel", true);
		jshint.configure(configuration);
	}

	private String readFileContents(File file)
			throws FileNotFoundException, IOException {
		FileInputStream inputStream = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream, charset));
		try {
			StringBuilder builder = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				builder.append(line);
				builder.append('\n');
				line = reader.readLine();
			}
			return builder.toString();
		} finally {
			reader.close();
		}
	}

	/**
	 * 
	 * @author samparly
	 * Handler: output the error message
	 * user could define their own handler. e.g. output to files or database
	 * output String to a well-formatted HTML
	 */
	private static final class SysoutProblemHandler implements ProblemHandler {

		private final String fileName;
		private File outputFileName;
		private PrintWriter pw;

		public SysoutProblemHandler(String fileName, File outputFile) {
			this.fileName = fileName;
			this.outputFileName = outputFile;
			try {
				if (outputFile != null)
					pw = new PrintWriter(new FileWriter(outputFileName, true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void destroy() {
			if (pw != null) {
				pw.flush();
				pw.close();
				pw = null;
			}
		}

		public void handleProblem(Problem problem) {
			int line = problem.getLine();
			String message = "Problem in file " + fileName + " at line " + line + ": "
					+ problem.getMessage();
			System.out.println(message);
			if (pw != null) {
				pw.println(message);
			}
		}

	}

	private static final class HTMLProblemHandler implements ProblemHandler {

		private final String fileName;
		private final File outputFile;

		public HTMLProblemHandler(String fileName, File outputFile) {
			this.fileName = fileName;
			this.outputFile = outputFile;
		}

		@Override
		public void handleProblem(Problem problem) {
			// TODO Auto-generated method stub
			int line = problem.getLine();
			String message = "Problem in file " + fileName + " at line " + line + ": "
					+ problem.getMessage();
			HTMLReport.getInstance().insert(fileName, message);
		}

		@Override
		public void destroy() {
			// TODO Auto-generated method stub

		}

	}
}
