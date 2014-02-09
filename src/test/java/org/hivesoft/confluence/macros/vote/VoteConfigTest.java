package org.hivesoft.confluence.macros.vote;

import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteConfigTest {

  VoteConfig classUnderTest;

  @Test
  public void test_createWithDefaultParameters_success() {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    Map<String, String> parameters = new HashMap<String, String>();
    classUnderTest = new VoteConfig(mockPermissionEvaluator, parameters);

    assertThat(classUnderTest.getRenderTitleLevel(), is(equalTo(3)));
    assertThat(classUnderTest.isChangeableVotes(), is(equalTo(false)));
    assertThat(classUnderTest.isShowComments(), is(equalTo(false)));
    assertThat(classUnderTest.getStartBound(), is(equalTo(1)));
    assertThat(classUnderTest.getIterateStep(), is(equalTo(1)));
    assertThat(classUnderTest.getVoters().size(), is(equalTo(0)));
    assertThat(classUnderTest.getViewers().size(), is(equalTo(0)));
    assertThat(classUnderTest.getManagers().size(), is(equalTo(0)));
    assertThat(classUnderTest.isVisibleVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isVisibleVotersWiki(), is(equalTo(false)));
    assertThat(classUnderTest.isLocked(), is(equalTo(false)));
  }

  @Test
  public void test_createFromSurveyConfigWithDefaultParameters_success() {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    Map<String, String> parameters = new HashMap<String, String>();
    SurveyConfig surveyConfig = new SurveyConfig(mockPermissionEvaluator, parameters);
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
    assertThat(classUnderTest.isVisibleVotersWiki(), is(equalTo(false)));
    assertThat(classUnderTest.isLocked(), is(equalTo(false)));
  }

  @Test
  public void test_createWithAllCustomParameters_success() {
    final String currentUserName = "spock";
    List<String> viewers = new ArrayList<String>();
    viewers.add(currentUserName);
    viewers.add("kirk");
    List<String> voters = new ArrayList<String>();
    voters.add("me");
    voters.add("myself");
    voters.add("irene");

    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    when(mockPermissionEvaluator.getRemoteUsername()).thenReturn(currentUserName);
    when(mockPermissionEvaluator.isPermissionListEmptyOrContainsGivenUser(viewers, currentUserName)).thenReturn(true);
    when(mockPermissionEvaluator.isPermissionListEmptyOrContainsGivenUser(voters, currentUserName)).thenReturn(false);
    when(mockPermissionEvaluator.getCanSeeVoters("true", true)).thenReturn(true);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someRandomTitle");
    parameters.put(VoteConfig.KEY_RENDER_TITLE_LEVEL, "5");
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");
    parameters.put(VoteConfig.KEY_SHOW_COMMENTS, "true");
    parameters.put(VoteConfig.KEY_START_BOUND, "6");
    parameters.put(VoteConfig.KEY_ITERATE_STEP, "4");
    parameters.put(VoteConfig.KEY_VOTERS, StringUtils.join(voters, ','));
    parameters.put(VoteConfig.KEY_VIEWERS, StringUtils.join(viewers, ','));
    parameters.put(VoteConfig.KEY_MANAGERS, "vader, yoda");
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS, "true");
    parameters.put(VoteConfig.KEY_VISIBLE_VOTERS_WIKI, "true");
    parameters.put(VoteConfig.KEY_LOCKED, "true");
    classUnderTest = new VoteConfig(mockPermissionEvaluator, parameters);

    assertThat(classUnderTest.getRenderTitleLevel(), is(equalTo(5)));
    assertThat(classUnderTest.isChangeableVotes(), is(equalTo(true)));
    assertThat(classUnderTest.isShowComments(), is(equalTo(true)));
    assertThat(classUnderTest.getStartBound(), is(equalTo(6)));
    assertThat(classUnderTest.getIterateStep(), is(equalTo(4)));
    assertThat(classUnderTest.getVoters().size(), is(equalTo(3)));
    assertThat(classUnderTest.getViewers().size(), is(equalTo(2)));
    assertThat(classUnderTest.getManagers().size(), is(equalTo(2)));
    assertThat(classUnderTest.isVisibleVoters(), is(equalTo(true)));
    assertThat(classUnderTest.isVisibleVotersWiki(), is(equalTo(true)));
    assertThat(classUnderTest.isLocked(), is(equalTo(true)));

    assertThat(classUnderTest.isCanSeeResults(), is(true));
    assertThat(classUnderTest.isCanTakeSurvey(), is(false));
  }
}
