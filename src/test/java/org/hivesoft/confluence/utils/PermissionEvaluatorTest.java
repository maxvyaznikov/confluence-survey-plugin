package org.hivesoft.confluence.utils;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultGroup;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionEvaluatorTest extends ConfluenceTestBase {

  public static final String SOME_BALLOT_TITLE = "someBallotTitle";

  UserAccessor mockUserAccessor = mock(UserAccessor.class);
  UserManager mockUserManager = mock(UserManager.class);
  PermissionManager mockPermissionManager = mock(PermissionManager.class);

  PermissionEvaluator classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new PermissionEvaluator(mockUserAccessor, mockUserManager, mockPermissionManager);
  }

  @Test
  public void test_canCreatePage_success() {
    when(mockUserManager.getRemoteUsername()).thenReturn(SOME_USER1.getName());
    final Page contentEntityObject = new Page();
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockPermissionManager.hasPermission(SOME_USER1, Permission.EDIT, contentEntityObject)).thenReturn(true);
    final boolean canCreatePage = classUnderTest.canCreatePage(contentEntityObject);
    assertThat(canCreatePage, is(true));
  }

  @Test
  public void test_getRemoteUser_success() {
    when(mockUserManager.getRemoteUsername()).thenReturn("someUser");
    when(mockUserAccessor.getUser("someUser")).thenReturn(new DefaultUser("someUser"));
    final User remoteUser = classUnderTest.getRemoteUser();
    assertThat(remoteUser.getName(), is("someUser"));
  }

  @Test
  public void test_isPermissionListEmptyOrContainsGivenUser_Anonymous_failure() {
    final Boolean canPerformAction = classUnderTest.isPermissionListEmptyOrContainsGivenUser(new ArrayList<String>(), null);
    assertEquals(Boolean.FALSE, canPerformAction);
  }

  @Test
  public void test_isPermissionListEmptyOrContainsGivenUser_KnownNotRestricted_success() {
    final Boolean canPerformAction = classUnderTest.isPermissionListEmptyOrContainsGivenUser(new ArrayList<String>(), new DefaultUser("KnownUser"));
    assertEquals(Boolean.TRUE, canPerformAction);
  }

  @Test
  public void test_isPermissionListEmptyOrContainsGivenUser_isInList_success() {
    List<String> permissionsList = newArrayList("KnownUser", "AnotherUser");

    final Boolean canPerformAction = classUnderTest.isPermissionListEmptyOrContainsGivenUser(permissionsList, new DefaultUser("KnownUser"));

    assertThat(canPerformAction, is(true));
  }

  @Test
  public void test_isPermissionListEmptyOrContainsGivenUser_notIsInList_success() {
    List<String> permissionsList = new ArrayList<String>();
    permissionsList.add("NotThisUser");
    permissionsList.add("AnotherUser");
    final Boolean canPerformAction = classUnderTest.isPermissionListEmptyOrContainsGivenUser(permissionsList, new DefaultUser("KnownUser"));
    assertEquals(Boolean.FALSE, canPerformAction);
  }

  @Test
  public void test_isPermissionListEmptyOrContainsGivenUser_isMemberInGroup_success() {
    List<String> permissionsList = new ArrayList<String>();
    permissionsList.add("IsAGroup");
    permissionsList.add("AnotherUser");
    when(mockUserAccessor.hasMembership("IsAGroup", "KnownUser")).thenReturn(true);
    final Boolean canPerformAction = classUnderTest.isPermissionListEmptyOrContainsGivenUser(permissionsList, new DefaultUser("KnownUser"));
    assertEquals(Boolean.TRUE, canPerformAction);
  }

  @Test
  public void test_getCanSeeVoters_success() {
    //cant see results
    assertFalse(classUnderTest.canSeeVoters("true", false));
    //visibleVoters Parameter null
    assertFalse(classUnderTest.canSeeVoters(null, true));
    //visibleVoters Parameter not "true"
    assertFalse(classUnderTest.canSeeVoters("something", true));
    //visibleVoters Parameter "true"
    assertTrue(classUnderTest.canSeeVoters("true", true));
  }

  @Test
  public void test_getCanVote_emptyUser_success() {
    final Boolean canVote = classUnderTest.canVote(null, SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE));
    assertFalse(canVote);
  }

  @Test
  public void test_getCanVote_success() {
    final Boolean canVote = classUnderTest.canVote(SOME_USER1, SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE));
    assertTrue(canVote);
  }

  @Test
  public void test_getCanVote_notInVotersList_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VOTERS, "notThisUser, notThisUserEither");
    final Boolean canVote = classUnderTest.canVote(SOME_USER1, SurveyUtilsTest.createBallotWithParameters(parameters));
    assertFalse(canVote);
  }

  @Test
  public void test_getCanVote_inListViaGroup_success() {
    when(mockUserAccessor.hasMembership("someGroup", SOME_USER1.getName())).thenReturn(true);
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(VoteConfig.KEY_VOTERS, "notThisUser, notThisUserEither, someGroup");
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");
    final Boolean canVote = classUnderTest.canVote(SOME_USER1, SurveyUtilsTest.createBallotWithParameters(parameters));
    assertTrue(canVote);
  }

  @Test
  public void test_getUsersForGroupOrUser_oneGroupReturnsTwoUsers_success() {
    final DefaultGroup group1 = new DefaultGroup("group1");

    when(mockUserAccessor.getGroup(group1.getName())).thenReturn(group1);
    when(mockUserAccessor.getMemberNamesAsList(group1)).thenReturn(newArrayList(SOME_USER1.getName(), SOME_USER2.getName()));
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockUserAccessor.getUser(SOME_USER2.getName())).thenReturn(SOME_USER2);

    final List<User> result = classUnderTest.getActiveUsersForGroupOrUser(group1.getName());

    assertThat(result, containsInAnyOrder(SOME_USER1, SOME_USER2));
  }

  @Test
  public void test_getUsersForGroupOrUser_oneUserExistsAndIsNotDeactivated_success() {
    when(mockUserAccessor.getGroup(SOME_USER1.getName())).thenReturn(null);
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockUserAccessor.isDeactivated(SOME_USER1)).thenReturn(false);

    final List<User> result = classUnderTest.getActiveUsersForGroupOrUser(SOME_USER1.getName());

    assertThat(result, containsInAnyOrder(SOME_USER1));
  }

  @Test
  public void test_getUsersForGroupOrUser_oneUserDoesNotExist_success() {
    when(mockUserAccessor.getGroup(SOME_USER1.getName())).thenReturn(null);
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(null);

    final List<User> result = classUnderTest.getActiveUsersForGroupOrUser(SOME_USER1.getName());

    assertThat(result, hasSize(0));
  }

  @Test
  public void test_getUsersForGroupOrUser_oneUserExistsButIsDeactivated_success() {
    when(mockUserAccessor.getGroup(SOME_USER1.getName())).thenReturn(null);
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockUserAccessor.isDeactivated(SOME_USER1)).thenReturn(true);

    final List<User> result = classUnderTest.getActiveUsersForGroupOrUser(SOME_USER1.getName());

    assertThat(result, hasSize(0));
  }
}
