var profiles;
function isEmpty(obj) {
    for(var prop in obj) {
        if(obj.hasOwnProperty(prop))
            return false;
    }
    return true;
}
window.onload  = function() {
	var baseUrl = AJS.contextPath();
	var profiles;
	function getProfiles() {
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			dataType: "json",
			success: function(configs) {
				profiles = configs;
				if(profiles != null){
					initDropDown(profiles);
				    if(!isEmpty(profiles)){
                        AJS.$("#dropdown-menu").show();
                    }
				} else {
				    profiles = {};
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log(jqXHR.responseText);
				console.log(textStatus);
				console.log(errorThrown);
				AJS.messages.warning({
				    title: "Unable to retrieve Contrast Profiles!",
				    body: "Check your internet connection and try again."
				});
				return null;
			}
		});
	}

	function isEmpty(str) {
        return (!str || 0 === str.length);
    }

	function validate() {
	    var user = AJS.$("#username").attr("value");
        var api = AJS.$("#apiKey").attr("value");
        var service = AJS.$("#serviceKey").attr("value");
        var TSurl = AJS.$("#url").attr("value");
        var uuid = AJS.$("#uuid").attr("value");
        var profilename = AJS.$("#profilename").attr("value");

        if (isEmpty(user) || isEmpty(api) || isEmpty(service) || isEmpty(TSurl) || isEmpty(uuid) || isEmpty(profilename)) {
            return false;
        }
        return true;

	}

	function addProfile() {
	    if (validate()) {
	        var user = AJS.$("#username").attr("value");
        	var api = AJS.$("#apiKey").attr("value");
        	var service = AJS.$("#serviceKey").attr("value");
        	var TSurl = AJS.$("#url").attr("value");
        	var uuid = AJS.$("#uuid").attr("value");
        	var profilename = AJS.$("#profilename").attr("value");

        	var JSONPayload = {
        	    "profilename":profilename,
        		"username":user,
        		"apikey":api,
        		"servicekey":service,
        		"url":TSurl,
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
                    AJS.$("#profile-list").empty();
                    initDropDown(profiles);
                    AJS.$("#dropdown-menu").show();
                    AJS.messages.success({
                        title: "Success!",
                        body: "You have updated your Contrast Configuration"
                    });
        		},
        		error: function(){
        		    AJS.messages.warning({
                        title: "Unable to retrieve Contrast Profiles.",
                        body: "Check your internet connection and try again."
                    });
        		}
        	});
	    } else {
	        AJS.messages.warning({
                title: "Unable to save TeamServer Profile.",
                body: "Fill in all the fields and try again."
            });
	    }
	}
	function deleteProfile() {
		var profilename = AJS.$("#profilename").attr("value");
		var JSONPayload = {
				"profilename":profilename,
				"username":"",
				"apikey":"",
				"servicekey":"",
				"url":"",
				"uuid":""
			};
		var stringPayload = JSON.stringify(JSONPayload);
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/deleteprofile",
			type: "POST",
			contentType: "application/json",
			dataType:"json",
			data:stringPayload,
			processData: false,
			success: function() {
				delete profiles[JSONPayload.profilename];
				AJS.$("#profile-list").empty();
				initDropDown(profiles);
				if(AJS.$.isEmptyObject(profiles)){
                    AJS.$("#dropdown-menu").hide();
				}
				AJS.messages.success({
					title: "Success!",
					body: "You have deleted the profile: " + JSONPayload.profilename
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
		AJS.$("#uuid").val("");
		AJS.$("#profilename").val("");
	}
	function testConnection(){
		var user = AJS.$("#username").attr("value");
		var api = AJS.$("#apiKey").attr("value");
		var service = AJS.$("#serviceKey").attr("value");
		var TSurl = AJS.$("#url").attr("value");
		var uuid = AJS.$("#uuid").attr("value");
		var profilename = AJS.$("#profilename").attr("value");

        if(TSurl == ""){
            TSurl = "http://app.contrastsecurity.com/Contrast/api";
        }

		var JSONPayload = {
			"profilename":profilename,
			"username":user,
			"apikey":api,
			"servicekey":service,
			"url":TSurl,
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
					body: "A connection has been established."
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

	AJS.$("#admin-submit").removeAttr("onsubmit").submit(function(event){
        event.preventDefault();
    });
	AJS.$("#test-connection").removeAttr("onsubmit").submit(function(event){
        event.preventDefault();
    });
	AJS.$("#new-profile-button").removeAttr("onsubmit").submit(function(event){
		event.preventDefault();
	});
	AJS.$("#profile-delete").removeAttr("onsubmit").submit(function(event){
		event.preventDefault();
	});
    AJS.$("#admin-submit").click(function(){
		addProfile();
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
	AJS.$("#profile-delete").click(function(){
		AJS.$("#admin-form").hide();
		deleteProfile();
		clearForm();
		return false;
	});
	AJS.$("#dropdown-menu").hide();
	getProfiles();
};
