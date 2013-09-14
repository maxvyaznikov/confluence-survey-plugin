AJS.toInit(function() {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

  //atlassians framework seems not completely bug free. the confluence-base-url is set as a fallback.
  if (baseUrl==null || baseUrl==undefined) {
    baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
  }

  function populateForm() {
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin-admin/1.0/",
      dataType: "json",
      success: function(config) {
        AJS.$("#name").attr("value", config.name);
        AJS.$("#time").attr("value", config.time);
      }
    });
  }

    function updateConfig() {
      AJS.$.ajax({
        url: baseUrl + "/rest/surveyplugin-admin/1.0/",
        type: "PUT",
        contentType: "application/json",
        data: '{ "name": "' + AJS.$("#name").attr("value") + '", "time": ' +  AJS.$("#time").attr("value") + ' }',
        processData: false
      });
    }
  populateForm();

  AJS.$("#admin").submit(function(e) {
      e.preventDefault();
      updateConfig();
  });
});