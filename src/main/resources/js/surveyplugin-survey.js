AJS.toInit(function () {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
  var pageId = AJS.$("meta[name='ajs-page-id']").attr("content");

  if (baseUrl == null || baseUrl == undefined) {
    baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
  }

  function getCSVExport(surveyOrVote, exportLink) {
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + pageId + "/" + surveyOrVote + "/" + exportLink.alt + "/export",
      dataType: "json",
      success: function (csvExportRepresentation) {
        var inlineDialog = AJS.InlineDialog(AJS.$(exportLink), "exportDialog",
          function (content, trigger, showPopup) {
            content.css({"padding": "20px"}).html('<p>The survey has been exported as a page attachment: <a href="' + csvExportRepresentation.uri + '">click to download</a></p>');
            showPopup();
            return false;
          }
        );
        inlineDialog.show();
      },
      error: function (xhr, status, error) {
        var err = eval("(" + xhr.responseText + ")");
        alert(err.Message);
      }
    });
  }

  AJS.$(".exportsurvey").click(function (e) {
    e.preventDefault();
    getCSVExport("surveys", this);
  });
  /* voteResource has been deleted, so uncomment the call to it / no code for future, but at least the idea ;)
   AJS.$(".exportvote").click(function (e) {
   e.preventDefault();
   getCSVExport("votes", this);
   });*/
  AJS.$(".resetsurvey").click(function (e){
    e.preventDefault();
    //AJS.
  });
});