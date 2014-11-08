package org.hivesoft.confluence.model.enums;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UserVisualizationTest {

  @Test
  public void test_getFor_givenNull_returnNull() {
    UserVisualization result = UserVisualization.getFor(null);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void test_getFor_givenUnknown_returnNull() {
    UserVisualization result = UserVisualization.getFor("unknown");

    assertThat(result, is(nullValue()));
  }

  @Test
  public void test_getFor_should_return_LINKED_FULL_UserVisualization_for_its_propertyValue() {
    UserVisualization userVisualization = UserVisualization.LINKED_FULL;

    UserVisualization result = UserVisualization.getFor(userVisualization.propertyValue);

    assertEquals(userVisualization, result);
  }

  @Test
  public void test_valueOf() {
    UserVisualization userVisualization = UserVisualization.valueOf(UserVisualization.LINKED_FULL.name());

    assertThat(userVisualization, is(UserVisualization.LINKED_FULL));

    try {
      UserVisualization.valueOf("unknown");
      fail("should have thrown exception");
    } catch (IllegalArgumentException e) {
      //try catch to spare another test method for a method we don't use anyway
    }
  }
}