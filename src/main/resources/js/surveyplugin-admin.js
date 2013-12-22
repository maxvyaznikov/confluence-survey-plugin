AJS.toInit(function () {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

  //Atlassian's framework seems not completely bug free. the confluence-base-url is set as a fallback, which we set ourself in the admin.vm
  if (baseUrl == null || baseUrl == undefined) {
    baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
  }

  function populateForm() {
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/admin",
      dataType: "json",
      success: function (config) {
        AJS.$("#is-" + config.iconSet).attr("checked", "checked");
      }
    });
  }

  function updateConfig() {
    var upConfig = AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/admin",
      type: "PUT",
      contentType: "application/json",
      data: '{ "iconSet": "' + AJS.$("input[name='is-rads']:checked").attr("id") + '" }',
      processData: false
    });
    upConfig.done(function () {
      alert("iconSet has been updated to: " + AJS.$("input[name='is-rads']:checked").attr("id"));
    });
  }

  populateForm();

  AJS.$("#admin").submit(function (e) {
    e.preventDefault();
    updateConfig();
  });
});