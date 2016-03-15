package com.eclipsesource.jshint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLReport {

	private static HTMLReport instance;

	private final String HTMLFileName = "Report.html";
	private final String charSet = "UTF-8";
	private Document document = null;

	private HTMLReport() {
		ClassLoader classLoader = JSHint.class.getClassLoader();
		// Include DEFAULT_JSHINT_VERSION in name to ensure the constant matches the actual version
		String name = "com/jshint/" + HTMLFileName;
		InputStream inputStream = classLoader.getResourceAsStream(name);
		try {
			document = Jsoup.parse(inputStream, charSet, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HTMLReport getInstance() {
		if (instance == null)
			instance = new HTMLReport();
		return instance;
	}

	/**
	 * 
	 * @param fileName
	 * @param message
	 * 2016年3月14日
	 * @author Jiupeng
	 * @description Insert message to Report
	 * @reference 
	 * @interpretation
	 */
	public void insert(String fileName, String message) {
		Element div = document.body().getElementById("list");
		Elements div_lists = div.children();
		for (Element child : div_lists) {
			if (child.className().trim().equals("menuDiv")) {
				Element a = child.getElementsByTag("a").get(0);
				if (!a.text().contains(fileName))
					continue;
				else {
					//find it!
					child.getElementsByTag("ul").get(0)
							.append("<li>" + message + "</li>");
					return;
				}
			}
		}
		//do not contain such child element
		div.append("<div class='menuDiv'><h3><a href='#'>" + "+" + fileName
				+ "</a></h3><ul>" + "<li>" + message + "</li>" + "</ul></div>");
	}

	/**
	 * 
	 * @param outputFile
	 * 2016年3月14日
	 * @author Jiupeng
	 * @description Output report to file system
	 * @reference 
	 * @interpretation
	 */
	public void flush(File outputFile) {
		if (document != null && outputFile != null) {
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new FileWriter(outputFile));
				pw.print(document.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (pw != null) {
					pw.flush();
					pw.close();
					pw = null;
				}
			}
		}
	}

}
