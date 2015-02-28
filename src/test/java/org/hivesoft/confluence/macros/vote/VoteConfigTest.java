package org.hivesoft.confluence.macros.vote;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.model.enums.UserVisualization;
import org.hivesoft.confluence.utils.PermissionEvaluatorImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteConfigTest {
  private final static String CURRENT_USER_NAME = "spock";

  private final PermissionManager mockPermissionManager = mock(PermissionManager.class);
  private final UserManager mockUserManager = mock(UserManager.class);
  private final UserAccessor mockUserAccessor = mock(UserAccessor.class);

  private final PermissionEvaluatorImpl permissionEvaluator = new PermissionEvaluatorImpl(mockUserAccessor, mockUserManager, mockPermissionManager);

  private VoteConfig classUnderTest;

  @Before
  public void setUp() {
    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);
  }

  @Test
  public void test_createWithDefaultParameters_success() {
    Map<String, String> parameters = new HashMap<String, String>();

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getRenderTitleLevel(), is(equalTo(3)));
    assertThat(classUnderTest.isChangeableVotes(), is(equalTo(false)));
    assertThat(classUnderTest.isShowComments(), is(equalTo(false)));
    assertThat(classUnderTest.getStartBound(), is(equalTo(1)));
    assertThat(classUnderTest.getIterateStep(), is(equalTo(1)));
    assertThat(classUnderTest.getVoters().size(), is(equalTo(0)));
    assertThat(classUnderTest.getViewers().size(), is(equalTo(0)));
    assertThat(classUnderTest.getManagers().size(), is(equalTo(0)));
    assertThat(classUnderTest.isAlwaysShowResults(), is(equalTo(false)));
    assertThat(classUnderTest.isVisibleVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isVisiblePendingVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isLocked(), is(equalTo(false)));
    assertThat(classUnderTest.isShowCondensed(), is(equalTo(false)));
    assertThat(classUnderTest.isAnonymous(), is(equalTo(false)));
    assertThat(classUnderTest.getUniqueId(), is(equalTo(-1)));

    assertThat(classUnderTest.isCanSeeResults(), is(true));
    assertThat(classUnderTest.isCanTakeSurvey(), is(true));
    assertThat(classUnderTest.isCanManageSurvey(), is(true));

    assertThat(classUnderTest.getUserRenderer(), is(notNullValue()));
    assertThat(classUnderTest.getUserRenderer().getUserVisualization(), is(equalTo(UserVisualization.PLAIN_LOGIN)));
  }

  @Test
  public void test_createWithDefaultParameters_userIsAnonymous_success() {
    Map<String, String> parameters = new HashMap<String, String>();

    when(mockUserManager.getRemoteUsername()).thenReturn(null);

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.isCanSeeResults(), is(true));
    assertThat(classUnderTest.isCanTakeSurvey(), is(false));
    assertThat(classUnderTest.isCanManageSurvey(), is(false));
  }

  @Test
  public void test_createFromSurveyConfigWithDefaultParameters_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    SurveyConfig surveyConfig = new SurveyConfig(permissionEvaluator, parameters);

    classUnderTest = new VoteConfig(surveyConfig);

    assertThat(classUnderTest.getRenderTitleLevel(), is(equalTo(3)));
    assertThat(classUnderTest.isChangeableVotes(), is(equalTo(false)));
    assertThat(classUnderTest.isShowComments(), is(equalTo(true)));
    assertThat(classUnderTest.getStartBound(), is(equalTo(1)));
    assertThat(classUnderTest.getIterateStep(), is(equalTo(1)));
    assertThat(classUnderTest.getVoters().size(), is(equalTo(0)));
    assertThat(classUnderTest.getViewers().size(), is(equalTo(0)));
    assertThat(classUnderTest.getManagers().size(), is(equalTo(0)));
    assertThat(classUnderTest.isAlwaysShowResults(), is(equalTo(false)));
    assertThat(classUnderTest.isVisibleVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isVisiblePendingVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isLocked(), is(equalTo(false)));
    assertThat(classUnderTest.isShowCondensed(), is(equalTo(false)));
    assertThat(classUnderTest.isAnonymous(), is(false));
    assertThat(classUnderTest.getUniqueId(), is(equalTo(-1)));

    assertThat(classUnderTest.isCanSeeResults(), is(true));
    assertThat(classUnderTest.isCanTakeSurvey(), is(true));
    assertThat(classUnderTest.isCanManageSurvey(), is(true));

    assertThat(classUnderTest.getUserRenderer(), is(notNullValue()));
    assertThat(classUnderTest.getUserRenderer().getUserVisualization(), is(equalTo(UserVisualization.PLAIN_LOGIN)));
  }

  @Test
  public void test_createWithAllCustomParameters_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someRandomTitle");
    parameters.put(VoteConfig.KEY_RENDER_TITLE_LEVEL, "5");
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");
    parameters.put(VoteConfig.KEY_SHOW_COMMENTS, "true");
    parameters.put(VoteConfig.KEY_START_BOUND, "6");
    parameters.put(VoteConfig.KEY_ITERATE_STEP, "4");
    parameters.put(VoteConfig.KEY_VOTERS, "me, myself, irene");
    parameters.put(VoteConfig.KEY_VIEWERS, CURRENT_USER_NAME + ", kirk");
    parameters.put(VoteConfig.KEY_MANAGERS, "vader, yoda");
    parameters.put(VoteConfig.KEY_ALWAYS_SHOW_RESULTS, "true");
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS, "true");
    parameters.put(VoteConfig.KEY_VISIBLE_PENDING_VOTERS, "true");
    parameters.put(VoteConfig.KEY_USER_VISUALIZATION, "plain user name");
    parameters.put(VoteConfig.KEY_LOCKED, "true");
    parameters.put(VoteConfig.KEY_SHOW_CONDENSED, "true");
    parameters.put(VoteConfig.KEY_ANONYMOUS_MODE, "true");
    parameters.put(VoteConfig.KEY_UNIQUE_ID, "1");

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getRenderTitleLevel(), is(equalTo(5)));
    assertThat(classUnderTest.isChangeableVotes(), is(equalTo(true)));
    assertThat(classUnderTest.isShowComments(), is(equalTo(true)));
    assertThat(classUnderTest.getStartBound(), is(equalTo(6)));
    assertThat(classUnderTest.getIterateStep(), is(equalTo(4)));
    assertThat(classUnderTest.getVoters().size(), is(equalTo(3)));
    assertThat(classUnderTest.getViewers().size(), is(equalTo(2)));
    assertThat(classUnderTest.getManagers().size(), is(equalTo(2)));
    assertThat(classUnderTest.isAlwaysShowResults(), is(equalTo(true)));
    assertThat(classUnderTest.isVisibleVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isVisiblePendingVoters(), is(equalTo(true)));
    assertThat(classUnderTest.isLocked(), is(equalTo(true)));
    assertThat(classUnderTest.isShowCondensed(), is(equalTo(true)));
    assertThat(classUnderTest.isAnonymous(), is(true));
    assertThat(classUnderTest.getUniqueId(), is(1));

    assertThat(classUnderTest.isCanSeeResults(), is(true));
    assertThat(classUnderTest.isCanTakeSurvey(), is(false));
    assertThat(classUnderTest.isCanManageSurvey(), is(false));

    assertThat(classUnderTest.getUserRenderer(), is(notNullValue()));
    assertThat(classUnderTest.getUserRenderer().getUserVisualization(), is(equalTo(UserVisualization.PLAIN_FULL)));
  }

  @Test
  public void test_visiblePendingVotersAlsoDependsOnVotersNotEmpty_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VOTERS, " ");
    parameters.put(VoteConfig.KEY_VISIBLE_PENDING_VOTERS, "true");

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getVoters().size(), is(equalTo(0)));
    assertThat(classUnderTest.isVisiblePendingVoters(), is(equalTo(false)));
  }


  @Test
  public void test_userVisualization_backwards_compatibility_with_visibleVotersWiki_for_false() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS_WIKI, "false");

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getUserRenderer().getUserVisualization(), is(UserVisualization.PLAIN_LOGIN));
  }

  @Test
  public void test_userVisualization_backwards_compatibility_with_visibleVotersWiki_for_true() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS_WIKI, "true");

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getUserRenderer().getUserVisualization(), is(UserVisualization.LINKED_LOGIN));
  }

  @Test
  public void test_userVisualization_should_handle_new_parameter_before_old() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS_WIKI, "false");
    parameters.put(VoteConfig.KEY_USER_VISUALIZATION, "linked user name");

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getUserRenderer().getUserVisualization(), is(UserVisualization.LINKED_FULL));
  }
}
