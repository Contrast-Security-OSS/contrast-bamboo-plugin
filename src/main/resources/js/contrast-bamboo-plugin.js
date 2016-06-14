function f() {
	var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
	function populateForm() {
		alert("populating form");
		AJS.$.ajax({
			url: baseUrl + "/rest/teamserver-admin/1.0/",
			dataType: "application/json",
			success: function(config) {
			$("#username").val(config.username);
			$("#apikey").val(config.apikey);
			$("#servicekey").val(config.servicekey);
			$("#url").val(config.url);    
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
	}  
	populateForm();

	AJS.$("#admin").submit(function(e) {
		e.preventDefault();
		updateConfig();
	});
}
AJS.toInit(f);