package org.hivesoft.confluence.macros.vote;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import org.hivesoft.confluence.model.enums.UserVisualization;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.utils.PermissionEvaluator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteConfigTest {
  final static String CURRENT_USER_NAME = "spock";

  PermissionManager mockPermissionManager = mock(PermissionManager.class);
  UserManager mockUserManager = mock(UserManager.class);
  UserAccessor mockUserAccessor = mock(UserAccessor.class);

  PermissionEvaluator permissionEvaluator = new PermissionEvaluator(mockUserAccessor, mockUserManager, mockPermissionManager);

  VoteConfig classUnderTest;

  @Test
  public void test_createWithDefaultParameters_success() {
    Map<String, String> parameters = new HashMap<String, String>();

    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getRenderTitleLevel(), is(equalTo(3)));
    assertThat(classUnderTest.isChangeableVotes(), is(equalTo(false)));
    assertThat(classUnderTest.isShowComments(), is(equalTo(false)));
    assertThat(classUnderTest.getStartBound(), is(equalTo(1)));
    assertThat(classUnderTest.getIterateStep(), is(equalTo(1)));
    assertThat(classUnderTest.getVoters().size(), is(equalTo(0)));
    assertThat(classUnderTest.getViewers().size(), is(equalTo(0)));
    assertThat(classUnderTest.getManagers().size(), is(equalTo(0)));
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
  public void test_createFromSurveyConfigWithDefaultParameters_success() {
    Map<String, String> parameters = new HashMap<String, String>();

    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);

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

    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);

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

    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VOTERS, " ");
    parameters.put(VoteConfig.KEY_VISIBLE_PENDING_VOTERS, "true");

    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    assertThat(classUnderTest.getVoters().size(), is(equalTo(0)));
    assertThat(classUnderTest.isVisiblePendingVoters(), is(equalTo(false)));
  }


  @Test
  public void test_userVisualization_backwards_compatibility_with_visibleVotersWiki_for_false() {
    // Given:
    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS_WIKI, "false");

    // When:
    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    // Then:
    assertEquals(UserVisualization.PLAIN_LOGIN, classUnderTest.getUserRenderer().getUserVisualization());
  }

  @Test
  public void test_userVisualization_backwards_compatibility_with_visibleVotersWiki_for_true() {
    // Given:
    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS_WIKI, "true");

    // When:
    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    // Then:
    assertEquals(UserVisualization.LINKED_LOGIN, classUnderTest.getUserRenderer().getUserVisualization());
  }

  @Test
  public void test_userVisualization_should_handle_new_parameter_before_old() {
    // Given:
    when(mockUserManager.getRemoteUsername()).thenReturn(CURRENT_USER_NAME);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS_WIKI, "false");
    parameters.put(VoteConfig.KEY_USER_VISUALIZATION, "linked user name");

    // When:
    classUnderTest = new VoteConfig(permissionEvaluator, parameters);

    // Then:
    assertEquals(UserVisualization.LINKED_FULL, classUnderTest.getUserRenderer().getUserVisualization());
  }

  @Test
  public void test_canAttachFile_success() {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    classUnderTest = new VoteConfig(mockPermissionEvaluator, new HashMap<String, String>());

    when(mockPermissionEvaluator.canAttachFile(any(Page.class))).thenReturn(true);

    final Boolean canAttachFile = classUnderTest.canAttachFile(new Page());
    assertThat(canAttachFile, is(true));
  }

  @Test
  public void test_canCreatePage_success() {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    classUnderTest = new VoteConfig(mockPermissionEvaluator, new HashMap<String, String>());

    when(mockPermissionEvaluator.canCreatePage(any(Page.class))).thenReturn(true);

    final Boolean canCreatePage = classUnderTest.canCreatePage(new Page());
    assertThat(canCreatePage, is(true));
  }
}
