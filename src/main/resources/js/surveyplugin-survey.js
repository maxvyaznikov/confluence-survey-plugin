AJS.toInit(function () {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
  var pageId = AJS.$("meta[name='ajs-page-id']").attr("content");

  if (baseUrl == null || baseUrl == undefined) {
    baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
  }

  function getCSVExport(surveyOrVote, title) {
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + pageId + "/" + surveyOrVote + "/" + title + "/export",
      dataType: "json",
      success: function (csvExportRepresentation) {
        alert(csvExportRepresentation.uri);
      },
      error: function(xhr, status, error) {
        var err = eval("(" + xhr.responseText + ")");
        alert(err.Message);
      }
    });
  }

  AJS.$(".exportsurvey").click(function (e) {
    e.preventDefault();
    getCSVExport("surveys", this.alt);
  });

  AJS.$(".exportvote").click(function (e) {
    e.preventDefault();
    getCSVExport("votes", this.alt);
  });
});