window.onload  = function() {
	var baseUrl = "/bamboo";
	function populateForm() {
		console.log("sending ajax call");
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			dataType: "json",
			success: function(config) {
				console.log("ajax success");
				AJS.$("#username").val(config.username);
				AJS.$("#apikey").val(config.apikey);
				AJS.$("#servicekey").val(config.servicekey);
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
		var api = AJS.$("#apikey").attr("value");
		var service = AJS.$("#servicekey").attr("value");
		var url = AJS.$("#url").attr("value");
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-config/1.0/",
			type: "PUT",
			contentType: "application/json",
			data:{ 
			"username": user,
			"apikey": api,
			"servicekey": url,
			"url": url
		},
		processData: false
		});
		alert(0);
	}  
	populateForm();

	AJS.$("#admin").submit(function(e) {
		e.preventDefault();
		updateConfig();
	});
};