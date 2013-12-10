AJS.toInit(function () {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
  var pageId = AJS.$("meta[name='ajs-page-id']").attr("content");

  //Atlassian's framework seems not completely bug free. the confluence-base-url is set as a fallback.
  if (baseUrl == null || baseUrl == undefined) {
    baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
  }

  function getCSVExport(title) {
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/"+pageId+"/votes/"+title+"/export",
      dataType: "text",
      success: function (exportData) {
        alert(exportData);
        //AJS.$("#is-" + config.iconSet).attr("checked", "checked");
      }
    });
  }

  AJS.$(".exportvote").click(function (e) {
    e.preventDefault();
    getCSVExport(this.alt);
  });
});