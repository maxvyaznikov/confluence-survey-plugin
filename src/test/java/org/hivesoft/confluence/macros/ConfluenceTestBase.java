package org.hivesoft.confluence.macros;

import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.model.wrapper.SurveyUser;

public abstract class ConfluenceTestBase {
  protected final static User SOME_USER1 = new SurveyUser(new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de"));
  protected final static User SOME_USER2 = new SurveyUser(new DefaultUser("someUser2", "someUser2 FullName", "some2@testmail.de"));

  protected final static String SOME_SURVEY_TITLE = "someSurveyTitle";

}
