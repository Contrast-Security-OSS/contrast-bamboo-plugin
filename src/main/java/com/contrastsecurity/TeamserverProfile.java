package com.contrastsecurity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamserverProfile {
	@JsonProperty("username") 
	public String username;
	@JsonProperty("apikey")
	public String apikey;
	@JsonProperty("servicekey")
	public String servicekey;
	@JsonProperty("url") 
	public String url;

	public TeamserverProfile(String username, String apikey, String servicekey, String url){
		this.username = username;
		this.apikey = apikey;
		this.servicekey = servicekey;
		this.url = url;
	}
	public TeamserverProfile(){};

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getServicekey() {
		return servicekey;
	}

	public void setServicekey(String servicekey) {
		this.servicekey = servicekey;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
