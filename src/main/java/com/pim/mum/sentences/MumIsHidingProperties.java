package com.pim.mum.sentences;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mum")
public class MumIsHidingProperties {

	private String dataPath = "/data/";

	public void setDataPath(final String dataPath) {
		this.dataPath = dataPath;
	}

	public String getDataPath() {
		return dataPath;
	}
}