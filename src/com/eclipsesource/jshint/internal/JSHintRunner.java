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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.eclipsesource.jshint.JSHint;
import com.eclipsesource.jshint.Problem;
import com.eclipsesource.jshint.ProblemHandler;
import com.eclipsesource.json.JsonObject;

public class JSHintRunner {

	private static final String PARAM_CHARSET = "--charset";
	private static final String PARAM_CUSTOM_JSHINT = "--custom";
	private static final String PARAM_CONFIGURATION = "--config";
	private List<File> files;
	private Charset charset;
	private File library;
	private File Config;
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
			System.out.println("         --charset <charset>");
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
			} else if (PARAM_CHARSET.equals(arg) || PARAM_CUSTOM_JSHINT.equals(arg)
					|| PARAM_CONFIGURATION.equals(arg)) {
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
	 * 2016Äê3ÔÂ4ÈÕ
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
		Config = new File(config);
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
			String code = readFileContents(file);
			ProblemHandler handler = new SysoutProblemHandler(file.getAbsolutePath());
			jshint.check(code, handler);
		}
	}

	private void configureJSHint() {
		JsonObject configuration = new JsonObject();
		if (Config != null) {
			Properties property = new Properties();
			try {
				property.load(new FileReader(Config));
				Iterator<String> propNames = property.stringPropertyNames().iterator();
				while (propNames.hasNext()) {
					String key = propNames.next();
					configuration.add(key, property.getProperty(key));
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

	private static final class SysoutProblemHandler implements ProblemHandler {

		private final String fileName;

		public SysoutProblemHandler(String fileName) {
			this.fileName = fileName;
		}

		public void handleProblem(Problem problem) {
			int line = problem.getLine();
			String message = problem.getMessage();
			System.out.println(
					"Problem in file " + fileName + " at line " + line + ": " + message);
		}

	}

}
