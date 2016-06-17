window.onload  = function() {
	var baseUrl = "/bamboo";
	function populateForm() {
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			dataType: "json",
			success: function(config) {
				AJS.$("#username").val(config.username);
				AJS.$("#apiKey").val(config.apikey);
				AJS.$("#serviceKey").val(config.servicekey);
				AJS.$("#url").val(config.url);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log(jqXHR.responseText);
				console.log(textStatus);
				console.log(errorThrown);
			}
		});
	}
	function updateConfig() {
		var user = AJS.$("#username").attr("value");
		var api = AJS.$("#apiKey").attr("value");
		var service = AJS.$("#serviceKey").attr("value");
		var TSurl = AJS.$("#url").attr("value");
		var JSONPayload = {
				"username": user,
				"apikey": api,
				"servicekey": service,
				"url": TSurl
			};
		var stringPayload = JSON.stringify(JSONPayload);
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			type: "POST",
			contentType: "application/json",
			dataType:"json",
			data:stringPayload,
			processData: false
		});
	}
	AJS.$("#admin-submit").removeAttr('onsubmit').submit(function(event){
        event.preventDefault();
    });
	populateForm();
    AJS.$('#admin-submit').click(function(){
		updateConfig();
		return false;
	});
};