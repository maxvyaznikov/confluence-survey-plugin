package org.hivesoft.confluence.macros.vote;

import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

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
    assertThat(classUnderTest.isVisibleVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isVisibleVotersWiki(), is(equalTo(false)));
    assertThat(classUnderTest.isLocked(), is(equalTo(false)));
  }
}
