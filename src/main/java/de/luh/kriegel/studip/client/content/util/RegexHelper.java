package de.luh.kriegel.studip.client.content.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.luh.kriegel.studip.client.content.model.data.Id;

public class RegexHelper {

	private final static Logger log = LogManager.getLogger(RegexHelper.class);

	private static String[][] UMLAUT_REPLACEMENTS = { { "Ä", "Ae" }, { "Ü", "Ue" }, { "Ö", "Oe" }, { "ä", "ae" },
			{ "ü", "ue" }, { "ö", "oe" }, { "ß", "ss" } };

	public static Id extractIdFromString(String value) {
		Pattern idPattern = Pattern.compile(".*?\\/([0-9a-f]{32})\\.*");
		Matcher matcher = idPattern.matcher(value);

		matcher.find();

		if (matcher.groupCount() >= 1) {
			return new Id(matcher.group(1));
		}

		return null;
	}

	public static String replaceUmlaute(String orig) {
		String result = orig;

		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
			result = result.replaceAll(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
		}

		return result;
	}

	public static String getValidFilename(String value) {

		String filename = value.replaceAll("/", "-");

		if (SystemUtils.IS_OS_WINDOWS) {
			filename = filename.replaceAll("[^a-zA-Z0-9\\-\\säöüÄÖÜ]", " ");
		} else {
			filename = RegexHelper.replaceUmlaute(filename);
			filename = filename.replaceAll("[^a-zA-Z0-9\\-\\s]", " ");
		}

		return filename;
	}

}
