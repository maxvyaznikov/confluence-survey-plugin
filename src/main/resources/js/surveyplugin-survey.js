AJS.toInit(function () {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
  var pageId = AJS.$("meta[name='ajs-page-id']").attr("content");

  if (baseUrl == null || baseUrl == undefined) {
    baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
  }

  function castVote(castVoteLink, voteAction) {
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + pageId + "/votes/" + castVoteLink.alt + "/choices/" + castVoteLink.title,
      type: "POST",
      dataType: "json",
      contentType: "text/plain",
      data: voteAction,
      success: function () {
        var inlineDialog = AJS.InlineDialog(AJS.$(castVoteLink), "voteDialog",
          function (content, trigger, showPopup) {
            content.css({"padding": "20px"}).html('<p>you successfully casted a vote.</p>');
            showPopup();
            return false;
          }
        );
        inlineDialog.show();
        location.reload(true); //reload the wiki page
      },
      error: function (xhr, status, error) {
        alert("There was a problem casting a vote. Returned status: " + status + ", error: " + error);
      }
    });
  }

  function getCSVExport(surveyOrVote, exportLink) {
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + pageId + "/" + surveyOrVote + "/" + exportLink.alt + "/export",
      dataType: "json",
      success: function (csvExportRepresentation) {
        var inlineDialog = AJS.InlineDialog(AJS.$(exportLink), "exportDialog",
          function (content, trigger, showPopup) {
            content.css({"padding": "20px"}).html('<p>' + AJS.I18n.getText("surveyplugin.export.result.confirmation") + ': <a href="' + baseUrl + csvExportRepresentation.uri + '">' + AJS.I18n.getText("surveyplugin.click.to.download") + '</a></p>');
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
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + pageId + "/" + surveyOrVote + "/" + lockLink.alt + "/lock",
      type: "POST",
      dataType: "json",
      success: function (lockRepresentation) {
        var inlineDialog = AJS.InlineDialog(AJS.$(lockLink), "lockDialog",
          function (content, trigger, showPopup) {
            content.css({"padding": "20px"}).html('<p>' + getLockedText(lockRepresentation.locked) + '.</p>');
            showPopup();
            return false;
          }
        );
        inlineDialog.show();
        location.reload(true); //reload the wiki page
      },
      error: function (xhr, status, error) {
        alert("There was a problem locking the survey. Returned status: " + status + ", error: " + error);
      }
    });
  }

  function getLockedText(locked) {
    if (locked) {
      return AJS.I18n.getText("surveyplugin.locked.confirmation");
    } else {
      return AJS.I18n.getText("surveyplugin.unlocked.confirmation");
    }
  }

  AJS.$(".castvote").click(function (e) {
    e.preventDefault();
    castVote(this, "vote");
  })
  AJS.$(".exportsurvey").click(function (e) {
    e.preventDefault();
    getCSVExport("surveys", this);
  });
  AJS.$(".resetsurvey").click(function (e) {
    e.preventDefault();
    //TBD
  });
  AJS.$(".locksurvey").click(function (e) {
    e.preventDefault();
    locklink = this;
    var dialog = new AJS.Dialog({
      width: 320,
      height: 140,
      id: "lock-dialog",
      closeOnOutsideClick: true
    });

    dialog.addHeader("Confirmation");
    dialog.addPanel("SinglePanel", "<p>" + AJS.I18n.getText("surveyplugin.lock.confirmation.question") + "</p>", "singlePanel"
    )
    ;
    dialog.addButton("Ok", function (dialog) {
      dialog.hide();
      lockSurveyOrVote("surveys", locklink);
    });
    dialog.addLink("Cancel", function (dialog) {
      dialog.hide();
    }, "#");

    dialog.show();
  });
});