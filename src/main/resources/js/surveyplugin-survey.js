AJS.toInit(function () {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");
  var pageId = AJS.$("meta[name='ajs-page-id']").attr("content");

  if (baseUrl == null || baseUrl == undefined) {
    baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
  }

  function castVote(castVoteLink, voteActionValue) {
    var contentId = castVoteLink.getAttribute("contentid");
    var voteTitle = encodeURIComponent(castVoteLink.alt);
    var encodedURI = encodeURIComponent(castVoteLink.title);
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + contentId + "/votes",
      type: "POST",
      dataType: "json",
      contentType: "application/json; charset=utf-8",
      data: JSON.stringify({
        ballotTitle: voteTitle,
        voteChoice: encodedURI,
        voteAction: voteActionValue
      }),
      success: function (voteRepresentation) {
        var voteText = AJS.I18n.getText("surveyplugin.vote.add.success");
        if (voteRepresentation.voteAction == "UNVOTE") {
          voteText = AJS.I18n.getText("surveyplugin.vote.remove.success");
        } else if (voteRepresentation.voteAction == "CHANGEVOTE") {
          voteText = AJS.I18n.getText("surveyplugin.vote.change.success");
        }
        var inlineDialog = AJS.InlineDialog(AJS.$(castVoteLink), "voteDialog",
          function (content, trigger, showPopup) {
            content.css({"padding": "20px"}).html('<p>' + voteText + '.</p>');
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

  function createCSVExport(surveyOrVote, exportLink) {
    var encodedTitle = encodeURIComponent(exportLink.alt);
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + pageId + "/" + surveyOrVote + "/export",
      type: "POST",
      dataType: "json",
      contentType: "application/json; charset=utf-8",
      data: JSON.stringify({
        title: encodedTitle
      }),
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
    var encodedTitle = encodeURIComponent(lockLink.alt);
    AJS.$.ajax({
      url: baseUrl + "/rest/surveyplugin/1.0/pages/" + pageId + "/" + surveyOrVote + "/lock",
      type: "POST",
      dataType: "json",
      contentType: "application/json; charset=utf-8",
      data: JSON.stringify({
        title: encodedTitle
      }),
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
  });
  AJS.$(".castunvote").click(function (e) {
    e.preventDefault();
    castVote(this, "unvote");
  });
  AJS.$(".exportsurvey").click(function (e) {
    e.preventDefault();
    createCSVExport("surveys", this);
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