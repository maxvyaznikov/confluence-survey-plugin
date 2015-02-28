package org.hivesoft.confluence.macros;

import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.vote.VoteConfig;
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

    private Map<String, String> parameters;
    private String title;
    private String description = "";
    private PermissionEvaluator permissionEvaluator = new TestPermissionEvaluator.Builder(SOME_USER1).build();
    private List<Choice> choices = SurveyUtils.getDefaultChoices();
    private List<Comment> comments = new ArrayList<Comment>();

    public BallotBuilder(Map<String, String> parameters) {
      this.title = StringUtils.defaultString(parameters.get(VoteConfig.KEY_TITLE));
      this.parameters = parameters;
    }

    /**
     * convenience method if title was not in the parameters
     */
    public BallotBuilder title(String title) {
      this.title = title;
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

  protected static Ballot createDefaultBallot(String title) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return createBallotWithParameters(parameters);
  }

  protected static Ballot createDefaultBallotWithChoices(String title, List<Choice> choices) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return createBallotWithParametersAndChoices(parameters, choices);
  }

  protected static Ballot createDefaultBallotWithComments(String title, List<Comment> comments) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return new Ballot(title, "", createDefaultVoteConfig(parameters), SurveyUtils.getDefaultChoices(), comments);
  }

  protected static Ballot createBallotWithParameters(Map<String, String> parameters) {
    return createBallotWithParametersAndChoices(parameters, SurveyUtils.getDefaultChoices());
  }

  protected static Ballot createBallotWithParametersAndChoices(Map<String, String> parameters, List<Choice> choices) {
    return new BallotBuilder(parameters).choices(choices).build();
  }

  protected static Choice createdDefaultChoice() {
    return new Choice(SOME_CHOICE_DESCRIPTION);
  }

  protected static VoteConfig createDefaultVoteConfig(Map<String, String> parameters) {
    PermissionEvaluator dummyPermissionEvaluator = new TestPermissionEvaluator.Builder(SOME_USER1).build();
    return new VoteConfig(dummyPermissionEvaluator, parameters);
  }

  protected static SurveyConfig createDefaultSurveyConfig(Map<String, String> parameters) {
    PermissionEvaluator dummyPermissionEvaluator = new TestPermissionEvaluator.Builder(SOME_USER1).build();
    return new SurveyConfig(dummyPermissionEvaluator, parameters);
  }

  protected static Map<String, String> createDefaultParametersWithTitle(String title) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return parameters;
  }

  protected List<Choice> createChoicesWithoutVotes(int count) {
    List<Choice> choices = new ArrayList<Choice>();
    for (int i = 0; i < count; i++) {
      choices.add(new Choice("someChoice" + i));
    }
    return choices;
  }
}
