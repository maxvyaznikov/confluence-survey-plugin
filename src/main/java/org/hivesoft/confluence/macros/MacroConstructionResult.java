/**
 * Copyright (c) 2006-2015, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
