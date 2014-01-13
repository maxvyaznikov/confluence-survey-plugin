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
            content.css({"padding": "20px"}).html('<p>' + AJS.I18n.getText("surveyplugin.export.result.confirmation") + ': <a href="' + csvExportRepresentation.uri + '">' + AJS.I18n.getText("surveyplugin.click.to.download") + '</a></p>');
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

  function lockSurveyOrVote(surveyOrVote, lockLink) {
    //TBD
    alert("Locking or unlocking in progress for survey with title '" + locklink.title + "'.");
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
  AJS.$(".resetsurvey").click(function (e) {
    e.preventDefault();
    //TBD
  });
  AJS.$(".locksurvey").click(function (e) {
    e.preventDefault();
    locklink = this;
    var dialog = new AJS.Dialog({
      id: "reset-dialog",
      closeOnOutsideClick: true
    });

    dialog.addHeader("Confirmation");
    dialog.addPanel("SinglePanel", "<p>Do you really want to lock this Survey?</p>", "singlePanel");
    dialog.addButton("Ok", function (dialog) {
      dialog.hide();
      lockSurveyOrVote("survey", locklink);
    });
    dialog.addLink("Cancel", function (dialog) {
      dialog.hide();
    }, "#");

    dialog.show();
  });
});