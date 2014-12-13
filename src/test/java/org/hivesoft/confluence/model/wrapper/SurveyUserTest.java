package org.hivesoft.confluence.model.wrapper;

import com.atlassian.user.impl.DefaultUser;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class SurveyUserTest {

  SurveyUser classUnderTest;

  @Test
  public void test_equals_hashCode_toString() throws Exception {
    try {
      classUnderTest = new SurveyUser("");
      fail("Parameter must not be empty");
    } catch (IllegalArgumentException e) {
      assertThat(classUnderTest, is(nullValue()));
    }

    classUnderTest = new SurveyUser("someUser");

    SurveyUser someOtherUser = new SurveyUser("someUser");

    assertThat(classUnderTest, is(equalTo(someOtherUser)));
    assertThat(classUnderTest.hashCode(), is(someOtherUser.hashCode()));
    assertThat(classUnderTest.toString(), is(someOtherUser.toString()));

    assertFalse(classUnderTest.equals("someString"));

    someOtherUser = new SurveyUser(new DefaultUser("someUser", "someFull Name", "email@test.de"));
    assertThat(classUnderTest, is(equalTo(someOtherUser)));
    assertThat(classUnderTest.toString(), not(is(equalTo(someOtherUser.toString()))));

    someOtherUser = new SurveyUser("notThisUser");
    assertThat(classUnderTest, not(is(equalTo(someOtherUser))));
  }
}
