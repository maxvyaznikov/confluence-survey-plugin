package org.hivesoft.confluence.macros;

import com.atlassian.confluence.core.ContentEntityObject;
import org.hivesoft.confluence.utils.SurveyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacroConstructionResult {
  private final List<String> problems = new ArrayList<String>();

  private final boolean canAttachFile;
  private final boolean canCreatePage;
  private final long contentId;

  public MacroConstructionResult(SurveyManager surveyManager, ContentEntityObject contentObject) {
    this.canAttachFile = surveyManager.canAttachFile(contentObject);
    this.canCreatePage = surveyManager.canCreatePage(contentObject);
    this.contentId = contentObject.getId();
  }

  public void addProblems(String... problem) {
    problems.addAll(Arrays.asList(problem));
  }

  public List<String> getProblems() {
    return problems;
  }

  public boolean hasProblems() {
    return !problems.isEmpty();
  }

  public boolean isCanAttachFile() {
    return canAttachFile;
  }

  public boolean isCanCreatePage() {
    return canCreatePage;
  }

  public long getContentId() {
    return contentId;
  }
}
