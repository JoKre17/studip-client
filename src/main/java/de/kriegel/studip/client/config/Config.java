package de.kriegel.studip.client.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.kriegel.studip.client.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

	private final Logger log = LoggerFactory.getLogger(Config.class);

	private final String[] OPTIONS = { "baseUri", "username", "password" };
	public List<String> OPT_PARAMS = new ArrayList<>(Arrays.asList(OPTIONS));

	public Map<String, String> params = new HashMap<>();

	public URI baseUri;
	public Credentials credentials;

	public Config(String[] args) {

		parse(args);
		evaluate();

	}

	private void parse(String[] args) {

		for (String arg : args) {

			if (arg.contains("=")) {
				String[] argSplit = arg.split("=");

				String key = "";
				String value = "";

				if (argSplit.length == 0) {
					// does not contain valid value
					continue;

				} else if (argSplit.length == 1) {
					// contains key and empty value
					key = argSplit[0];

				} else if (argSplit.length == 2) {
					// found normal key value pair
					key = argSplit[0];
					value = argSplit[1];
				}

				if (OPT_PARAMS.contains(key)) {
					params.put(key, value);
				}
			}
		}
	}

	private void evaluate() {
		if (params.containsKey("baseUri")) {
			try {
				this.baseUri = new URI(params.get("baseUri"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		if (params.containsKey("username") && params.containsKey("password")) {
			credentials = new Credentials(params.get("username"), params.get("password"));
		}
	}

	@Override
	public String toString() {
		Map<String, String> res = new HashMap<>();

		for (Entry<String, String> e : params.entrySet()) {
			if (e.getKey().contains("password")) {
				res.put(e.getKey(), e.getValue().replaceAll(".", "*"));
			} else {
				res.put(e.getKey(), e.getValue());
			}
		}

		return res.toString();
	}

}
