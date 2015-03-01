package org.hivesoft.confluence.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultGroup;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.model.wrapper.AnonymousUser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionEvaluatorImplTest extends ConfluenceTestBase {

  private final UserAccessor mockUserAccessor = mock(UserAccessor.class);
  private final UserManager mockUserManager = mock(UserManager.class);
  private final PermissionManager mockPermissionManager = mock(PermissionManager.class);

  private PermissionEvaluatorImpl classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new PermissionEvaluatorImpl(mockUserAccessor, mockUserManager, mockPermissionManager);
  }

  @Test
  public void test_getUserByName_nullNotFound_anonymous() {
    when(mockUserAccessor.getUser(null)).thenReturn(null);

    User result = classUnderTest.getUserByName(null);

    assertThat(result.getFullName(), is(new AnonymousUser().getFullName()));
  }

  @Test
  public void test_canAttachFile_success() {
    ContentEntityObject contentEntityObject = new Page();

    when(mockUserManager.getRemoteUsername()).thenReturn(SOME_USER1.getName());
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockPermissionManager.hasCreatePermission(SOME_USER1, contentEntityObject, Attachment.class)).thenReturn(true);

    boolean result = classUnderTest.canAttachFile(contentEntityObject);

    assertThat(result, is(true));
  }

  @Test
  public void test_canCreatePage_success() {
    final Page contentEntityObject = new Page();

    when(mockUserManager.getRemoteUsername()).thenReturn(SOME_USER1.getName());
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockPermissionManager.hasPermission(SOME_USER1, Permission.EDIT, contentEntityObject)).thenReturn(true);

    final boolean result = classUnderTest.canCreatePage(contentEntityObject);
    assertThat(result, is(true));
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
    assertThat(canPerformAction, is(equalTo(false)));
  }

  @Test
  public void test_isPermissionListEmptyOrContainsGivenUser_KnownNotRestricted_success() {
    final Boolean canPerformAction = classUnderTest.isPermissionListEmptyOrContainsGivenUser(new ArrayList<String>(), new DefaultUser("KnownUser"));
    assertThat(canPerformAction, is(equalTo(true)));
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
  public void test_getUsersForGroupOrUser_oneGroupReturnsTwoUsersButOneIsDeactivated_success() {
    final DefaultGroup group1 = new DefaultGroup("group1");

    when(mockUserAccessor.getGroup(group1.getName())).thenReturn(group1);
    when(mockUserAccessor.getMemberNamesAsList(group1)).thenReturn(newArrayList(SOME_USER1.getName(), SOME_USER2.getName()));
    when(mockUserAccessor.getUser(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockUserAccessor.getUser(SOME_USER2.getName())).thenReturn(SOME_USER2);
    when(mockUserAccessor.isDeactivated(SOME_USER1)).thenReturn(true);

    final List<User> result = classUnderTest.getActiveUsersForGroupOrUser(group1.getName());

    assertThat(result, containsInAnyOrder(SOME_USER2));
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
