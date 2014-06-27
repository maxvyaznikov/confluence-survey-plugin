package org.hivesoft.confluence.macros.utils;

import com.atlassian.user.User;
import junit.framework.TestCase;
import org.hivesoft.confluence.macros.vote.UserVisualization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserRendererTest {

  @Mock
  private User user;

  @Test
  public void test_render_should_correctly_render_plain_login_name() throws Exception {
    // Given:
    UserRenderer underTest = new UserRenderer(UserVisualization.PLAIN_LOGIN);
    doReturn("doej").when(user).getName();

    // When:
    String result = underTest.render("http://example.com", user);

    // Then:
    assertEquals("doej", result);
  }

  @Test
  public void test_render_should_correctly_render_linked_full_user_name() throws Exception {
    // Given:
    UserRenderer underTest = new UserRenderer(UserVisualization.LINKED_FULL);

    doReturn("jdoe").when(user).getName();
    doReturn("John Doe").when(user).getFullName();

    // When:
    String result = underTest.render("http://example.com", user);

    // Then:
    assertEquals("<a href=\"http://example.com/display/~jdoe\" class=\"url fn confluence-userlink\" data-username=\"jdoe\">John Doe</a>", result);
  }

  @Test
  public void test_renderForCsv_should_correctly_render_plain_login_name() throws Exception {
    // Given:
    UserRenderer underTest = new UserRenderer(UserVisualization.PLAIN_LOGIN);
    doReturn("doej").when(user).getName();

    // When:
    String result = underTest.renderForCsv(user);

    // Then:
    assertEquals("doej", result);
  }

  @Test
  public void test_renderForCsv_should_correctly_render_linked_full_user_name() throws Exception {
    // Given:
    UserRenderer underTest = new UserRenderer(UserVisualization.LINKED_FULL);

    doReturn("John Doe").when(user).getFullName();

    // When:
    String result = underTest.renderForCsv(user);

    // Then:
    assertEquals("John Doe", result);
  }
}