package org.hivesoft.confluence.utils;

import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.model.enums.UserVisualization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UserRendererTest extends ConfluenceTestBase {

  UserRenderer classUnderTest;

  @Test
  public void test_render_plainLoginName() throws Exception {
    classUnderTest = new UserRenderer(UserVisualization.PLAIN_LOGIN);

    String result = classUnderTest.render("http://example.com", SOME_USER1);

    assertThat(result, is("someUser1"));
  }

  @Test
  public void test_render_plainFullUserName() throws Exception {
    classUnderTest = new UserRenderer(UserVisualization.PLAIN_FULL);

    String result = classUnderTest.render("http://example.com", SOME_USER1);

    assertThat(result, is("someUser1 FullName"));
  }

  @Test
  public void test_render_linkedPlainLoginName() throws Exception {
    classUnderTest = new UserRenderer(UserVisualization.LINKED_LOGIN);

    String result = classUnderTest.render("http://example.com", SOME_USER1);

    assertThat(result, is("<a href=\"http://example.com/display/~someUser1\" class=\"url fn confluence-userlink\" data-username=\"someUser1\">someUser1</a>"));
  }

  @Test
  public void test_render_linkedFullUserName() throws Exception {
    classUnderTest = new UserRenderer(UserVisualization.LINKED_FULL);

    String result = classUnderTest.render("http://example.com", SOME_USER1);

    assertThat(result, is("<a href=\"http://example.com/display/~someUser1\" class=\"url fn confluence-userlink\" data-username=\"someUser1\">someUser1 FullName</a>"));
  }


  @Test
  public void test_renderForCsv_plainLoginName() throws Exception {
    classUnderTest = new UserRenderer(UserVisualization.PLAIN_LOGIN);

    String result = classUnderTest.renderForCsv(SOME_USER1);

    assertThat(result, is("someUser1"));
  }

  @Test
  public void test_renderForCsv_linkedFullUserName_noLinkGenerated() throws Exception {
    classUnderTest = new UserRenderer(UserVisualization.LINKED_FULL);

    String result = classUnderTest.renderForCsv(SOME_USER1);

    assertThat(result, is("someUser1 FullName"));
  }
}