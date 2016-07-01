var profiles;
window.onload  = function() {
	var baseUrl = "/bamboo";
	function getProfiles() {
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			dataType: "json",
			success: function(configs) {
				profiles = configs;
				populateForm("");
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log(jqXHR.responseText);
				console.log(textStatus);
				console.log(errorThrown);
				AJS.messages.warning({
				    title: "Unable to retrieve Teamserver Profiles!",
				    body: "Check your internet connection and try again."
				});
			}
		});
	}
	function populateForm(profilename) {
        var config = profiles[profilename];
        if (config != undefined){
            AJS.$("#username").val(config.username);
            AJS.$("#apiKey").val(config.apikey);
            AJS.$("#serviceKey").val(config.servicekey);
            AJS.$("#url").val(config.url);
            AJS.$("#servername").val(config.servername);
            AJS.$("#uuid").val(config.uuid);
            AJS.$("#profilename").val(config.profilename);
		}
	}
	function createNewProfile(){
        AJS.$("#admin").show();
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
				"username": user,
				"apikey": api,
				"servicekey": service,
				"url": TSurl,
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
				AJS.messages.success({
				    title: "Success!",
				    body: "You have updated you Teamserver Configuration!"
				});
			}
		});
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
	function createNewProfile(){
		console.log("creating new profile");
		AJS.$("#admin").show();
		clearForm();
	}
	AJS.$("#admin-submit").removeAttr('onsubmit').submit(function(event){
        event.preventDefault();
    });
    AJS.$('#admin-submit').click(function(){
		updateConfig();
		return false;
	});
	AJS.$("#admin-form").onclick=function(){createNewProfile();};
	getProfiles();
};
