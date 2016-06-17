package com.contrastsecurity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamserverProfile {
	@JsonProperty("profilename") 
	public String profilename;
	@JsonProperty("username") 
	public String username;
	@JsonProperty("apikey")
	public String apikey;
	@JsonProperty("servicekey")
	public String servicekey;
	@JsonProperty("url") 
	public String url;
	@JsonProperty("servername") 
	public String servername;
	@JsonProperty("uuid") 
	public String uuid;

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
	public String getProfilename() {
		return profilename;
	}
	public void setProfilename(String profilename) {
		this.profilename = profilename;
	}
	public String getServername() {
		return servername;
	}
	public void setServername(String servername) {
		this.servername = servername;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
