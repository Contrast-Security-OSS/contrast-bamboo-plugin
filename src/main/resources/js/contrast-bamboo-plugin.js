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
		var url = AJS.$("#url").attr("value");
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-config/1.0/",
			type: "PUT",
			contentType: "json",
			data:{ 
			"username": user,
			"apikey": api,
			"servicekey": url,
			"url": url
		},
		processData: false
		});
	}  
	AJS.$("#admin-submit").removeAttr('onsubmit').submit(function(event){
        event.preventDefault();
    });
	populateForm();
    AJS.$('#admin-submit').click(function(){
		alert(0);
		updateConfig();
		return false;
	});
};