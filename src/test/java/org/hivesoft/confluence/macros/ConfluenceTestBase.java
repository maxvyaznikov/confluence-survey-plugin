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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public abstract class ConfluenceTestBase {
  protected final static long SOME_PAGE_ID = 123l;

  protected final static User SOME_USER1 = new SurveyUser(new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de"));
  protected final static User SOME_USER2 = new SurveyUser(new DefaultUser("someUser2", "someUser2 FullName", "some2@testmail.de"));

  protected final static String SOME_SURVEY_TITLE = "someSurveyTitle";
  protected static final String SOME_BALLOT_TITLE = "someBallotTitle";
  protected static final String SOME_CHOICE_DESCRIPTION = "someChoiceDescription";

  protected static Ballot createDefaultBallot(String title) {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return createBallotWithParameters(parameters);
  }

  protected static Ballot createDefaultBallotWithChoices(String title, List<Choice> choices) {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return createBallotWithParametersAndChoices(parameters, choices);
  }

  protected static Ballot createDefaultBallotWithComments(String title, List<Comment> comments) {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return new Ballot(title, "", createDefaultVoteConfig(parameters), SurveyUtils.getDefaultChoices(), comments);
  }

  protected static Ballot createBallotWithParameters(Map<String, String> parameters) {
    return createBallotWithParametersAndChoices(parameters, SurveyUtils.getDefaultChoices());
  }

  protected static Ballot createBallotWithParametersAndChoices(Map<String, String> parameters, List<Choice> choices) {
    String titleInMacroParameters = StringUtils.defaultString(parameters.get(VoteConfig.KEY_TITLE));
    return new Ballot(titleInMacroParameters, "", createDefaultVoteConfig(parameters), choices, new ArrayList<Comment>());
  }

  protected static Choice createdDefaultChoice() {
    return new Choice(SOME_CHOICE_DESCRIPTION);
  }

  protected static VoteConfig createDefaultVoteConfig(Map<String, String> parameters) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    return new VoteConfig(mockPermissionEvaluator, parameters);
  }

  protected static SurveyConfig createDefaultSurveyConfig(Map<String, String> parameters) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    return new SurveyConfig(mockPermissionEvaluator, parameters);
  }

  protected static Map<String, String> createDefaultParametersWithTitle(String title) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return parameters;
  }
}
