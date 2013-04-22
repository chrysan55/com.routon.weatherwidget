package com.routon.weatherwidget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import android.Manifest.permission;
import android.util.Log;

public class IniReader {
	private Map<String, Properties> sections;
	private String secion;
	private Properties properties;
	private String fileName;
	static Object object = new Object();

	public IniReader(String FileName) {
		this.fileName = FileName;
		File f = new File(fileName);
		synchronized (object) {
			if (!f.exists())
				createIniFromMntn();
			sections = new HashMap<String, Properties>();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(new File(
								fileName))));
				read(reader);
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void createIniFromMntn() {
		File fromFile = new File("/hdisk/etc/mntn/mntn.ini");
		File toFile = new File(fileName);
		String line;
		boolean startWrite = false;
		StringBuilder builder = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(fromFile)));
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.matches("\\[.*\\]") == true && startWrite == true)
					startWrite = false;

				if (line.contains("[Weather]"))
					startWrite = true;

				if (startWrite == true)
					builder.append(line).append('\n');

				Log.i("tag", "line :  " + line + " , start=" + startWrite);
			}
			reader.close();
			Log.i("tag", "write weather  :  " + builder.toString());
			File f = new File(this.fileName);
			if (!f.exists()) {
				// create file.
				f.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(f)));
			writer.write(builder.toString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void read(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			parseLine(line);
		}
	}

	private void parseLine(String line) {
		line = line.trim();
		if (line.matches("\\[.*\\]") == true) {
			secion = line.replaceFirst("\\[(.*)\\]", "$1");
			properties = new Properties();
			sections.put(secion, properties);
		} else if (line.matches(".*=.*") == true) {
			if (properties != null) {
				int i = line.indexOf('=');
				String name = line.substring(0, i);
				String value = line.substring(i + 1);
				properties.setProperty(name, value);
			}
		}
	}

	public String getValue(String section, String name) {
		Properties p = sections.get(section);

		if (p == null) {
			return null;
		}

		String value = p.getProperty(name);
		return value;
	}

	public void SetValue(String section, String name, String value) {
		Properties p = sections.get(section);
		if (p == null) {
			Properties newpro = new Properties();
			newpro.setProperty(name, value);
			sections.put(section, newpro);
		} else {
			p.setProperty(name, value);
		}

		// write();
	}

	public boolean write() {
		PrintWriter writer = null;
		synchronized (object) {
			try {
				File f = new File(this.fileName);
				if (!f.exists()) {
					// create file.
					f.createNewFile();
				}
				writer = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(f)));
				StringBuilder builder = new StringBuilder();
				Iterator<String> iterator = sections.keySet().iterator();
				int i = 0;
				while (iterator.hasNext()) {
					String sectionName = iterator.next();
					Properties p = (Properties) sections.get(sectionName);
					builder.append("\n").append("\n").append("[")
							.append(sectionName).append("]");
					builder.append(getPropertiesString(p));
					Log.i("iniReader", "========================= " + (i++)
							+ builder.toString());
				}
				writer.write(builder.toString().trim());
				writer.flush();
				writer.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("EncryptBox", "INIReader: write error(" + this.fileName
						+ ").");
			}
			if (writer != null) {
				writer.close();
			}
			return false;
		}
	}

	private String getPropertiesString(Properties p) {
		StringBuilder builder = new StringBuilder();
		Iterator iterator = p.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			builder.append("\n").append(key).append("=").append(p.get(key));
		}
		return builder.toString();
	}
}
