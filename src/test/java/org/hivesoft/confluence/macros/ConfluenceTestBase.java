package org.hivesoft.confluence.macros;

import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.model.Survey;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Choice;
import org.hivesoft.confluence.model.vote.Comment;
import org.hivesoft.confluence.model.wrapper.SurveyUser;
import org.hivesoft.confluence.utils.PermissionEvaluator;
import org.hivesoft.confluence.utils.SurveyUtils;
import org.hivesoft.confluence.utils.TestPermissionEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ConfluenceTestBase {
  protected static final long SOME_PAGE_ID = 123l;

  protected static final User SOME_USER1 = new SurveyUser(new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de"));
  protected static final User SOME_USER2 = new SurveyUser(new DefaultUser("someUser2", "someUser2 FullName", "some2@testmail.de"));

  protected static final String SOME_SURVEY_TITLE = "someSurveyTitle";
  protected static final String SOME_BALLOT_TITLE = "someBallotTitle";
  protected static final String SOME_CHOICE_DESCRIPTION = "someChoiceDescription";

  protected static class BallotBuilder {

    private final Map<String, String> parameters = new HashMap<String, String>();
    private String title = "";
    private String description = "";
    private PermissionEvaluator permissionEvaluator = new TestPermissionEvaluator.Builder(SOME_USER1).build();
    private final List<Choice> choices = SurveyUtils.getDefaultChoices();
    private final List<Comment> comments = new ArrayList<Comment>();

    public BallotBuilder() {
    }

    /**
     * convenience method if title was not in the parameters
     */
    public BallotBuilder title(String title) {
      this.title = title;
      return this;
    }

    public BallotBuilder parameters(Map<String, String> parameters) {
      this.parameters.clear();
      this.parameters.putAll(parameters);
      if (StringUtils.isBlank(this.title)) {
        this.title = StringUtils.defaultString(parameters.get(VoteConfig.KEY_TITLE));
      }
      return this;
    }

    public BallotBuilder description(String description) {
      this.description = description;
      return this;
    }

    public BallotBuilder permissionEvaluator(PermissionEvaluator permissionEvaluator) {
      this.permissionEvaluator = permissionEvaluator;
      return this;
    }

    public BallotBuilder choices(List<Choice> choices) {
      this.choices.clear();
      this.choices.addAll(choices);
      return this;
    }

    public BallotBuilder comments(List<Comment> comments) {
      this.comments.addAll(comments);
      return this;
    }

    public Ballot build() {
      return new Ballot(title, description, new VoteConfig(permissionEvaluator, parameters), choices, comments);
    }
  }

  public static class SurveyBuilder {
    PermissionEvaluator permissionEvaluator = new TestPermissionEvaluator.Builder(SOME_USER1).build();
    final Map<String, String> parameters = new HashMap<String, String>();

    public SurveyBuilder permissionEvaluator(PermissionEvaluator permissionEvaluator) {
      this.permissionEvaluator = permissionEvaluator;
      return this;
    }

    public SurveyBuilder parameters(Map<String, String> parameters) {
      this.parameters.clear();
      this.parameters.putAll(parameters);
      return this;
    }

    public Survey build() {
      return new Survey(new SurveyConfig(permissionEvaluator, parameters));
    }
  }

  protected static List<Choice> createChoicesWithoutVotes(int count) {
    List<Choice> choices = new ArrayList<Choice>();
    for (int i = 0; i < count; i++) {
      choices.add(new Choice("someChoice" + i));
    }
    return choices;
  }
}
