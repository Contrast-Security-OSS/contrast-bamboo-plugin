var profiles;
window.onload  = function() {
	var baseUrl = "/bamboo";
	function getProfiles() {
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			dataType: "json",
			success: function(configs) {
				profiles = configs;
				if(profiles != null){
					initDropDown(configs);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log(jqXHR.responseText);
				console.log(textStatus);
				console.log(errorThrown);
				AJS.messages.warning({
				    title: "Unable to retrieve Teamserver Profiles!",
				    body: "Check your internet connection and try again."
				});
				return null;
			}
		});
	}
	function updateConfig() {
		var user = AJS.$("#username").attr("value");
		var api = AJS.$("#apiKey").attr("value");
		var service = AJS.$("#serviceKey").attr("value");
		var TSurl = AJS.$("#url").attr("value");
		var servername = AJS.$("#servername").attr("value");
		var uuid = AJS.$("#uuid").attr("value");
		var profilename = AJS.$("#profilename").attr("value");

		var JSONPayload = {
				"profilename":profilename,
				"username":user,
				"apikey":api,
				"servicekey":service,
				"url":TSurl,
				"servername":servername,
				"uuid":uuid
			};
		var stringPayload = JSON.stringify(JSONPayload);
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			type: "POST",
			contentType: "application/json",
			dataType:"json",
			data:stringPayload,
			processData: false,
			success: function() {
				profiles[JSONPayload.profilename] = JSONPayload;
				AJS.messages.success({
				    title: "Success!",
				    body: "You have updated your Teamserver Configuration!"
				});
			}
		});
	}
	function populateForm(profilename) {
		clearForm();
        var config = profiles[profilename];
        if (config != undefined){
            AJS.$("#username").val(config.username);
            AJS.$("#apiKey").val(config.apikey);
            AJS.$("#serviceKey").val(config.servicekey);
            AJS.$("#url").val(config.url);
            AJS.$("#servername").val(config.servername);
			AJS.$("#uuid").val(config.uuid);
			AJS.$("#uuid").val(config.uuid);
			AJS.$("#profilename").val(config.profilename);
			AJS.$("#profilename-display").html(config.profilename);
		}
		AJS.$("#admin-form").show();
	}
	function clearForm(){
		AJS.$("#username").val("");
		AJS.$("#apiKey").val("");
		AJS.$("#serviceKey").val("");
		AJS.$("#url").val("");
		AJS.$("#servername").val("");
		AJS.$("#uuid").val("");
		AJS.$("#profilename").val("");
	}
	function testConnection(){
		var user = AJS.$("#username").attr("value");
		var api = AJS.$("#apiKey").attr("value");
		var service = AJS.$("#serviceKey").attr("value");
		var TSurl = AJS.$("#url").attr("value");
		var servername = AJS.$("#servername").attr("value");
		var uuid = AJS.$("#uuid").attr("value");
		var profilename = AJS.$("#profilename").attr("value");

		var JSONPayload = {
			"profilename":profilename,
			"username":user,
			"apikey":api,
			"servicekey":service,
			"url":TSurl,
			"servername":servername,
			"uuid":uuid
		};
		var stringPayload = JSON.stringify(JSONPayload);
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/verifyconnection",
			type: "POST",
			contentType: "application/json",
			dataType:"json",
			data:stringPayload,
			processData: false,
			success: function() {
				AJS.messages.success({
					title: "Success!",
					body: "A connection has been established!"
				});
			},
			error: function() {
				AJS.messages.warning({
					title:"Error.",
					body:"Unable to establish a connection. Please check your settings and try again"
				});
			}
		});
	}
	function initDropDown(profs){
		var i = 0;
		AJS.$.each(profs, function(name, profile) {
			AJS.$("#profile-list").append("<li><a id='profile-item-"+(i)+"'>"+name+"</a></li>");
			AJS.$("#profile-item-"+i).click(function(){
				populateForm(name);
				AJS.$("#profile-delete").show();
				AJS.$("#profilename-display").css('display', 'inline');
				AJS.$("#profilename-label").hide();
				AJS.$("#profilename").hide();
			});
			i++;
		});
	}
	function isNew(name){
		return (profiles[name] === undefined);
	}

	AJS.$("#admin-submit").removeAttr("onsubmit").submit(function(event){
        event.preventDefault();
    });
	AJS.$("#test-connection").removeAttr("onsubmit").submit(function(event){
        event.preventDefault();
    });
	AJS.$("#new-profile-button").removeAttr("onsubmit").submit(function(event){
		event.preventDefault();
	});
    AJS.$("#admin-submit").click(function(){
		if(isNew(AJS.$("#profilename").attr("value"))){
			AJS.$("#profile-list").html("");
			updateConfig();
			initDropDown(profiles);
			return false;
		}
		updateConfig();
		return false;
	});
	AJS.$("#test-connection").click(function(){
		testConnection();
		return false;
	});
	AJS.$("#new-profile-button").click(function(){
		clearForm();
		AJS.$("#admin-form").show();
		AJS.$("#profilename-display").hide();
		AJS.$("#profilename-label").show();
		AJS.$("#profilename").show();
		AJS.$("#profile-delete").hide();
		return false;
	});
	AJS.$("#delete-profile-button").click(function(){
		AJS.$("#admin-form").hide();
		deleteProfile();
		clearForm();
		return false;
	});
	getProfiles();
};
