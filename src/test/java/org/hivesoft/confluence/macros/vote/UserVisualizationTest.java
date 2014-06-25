package org.hivesoft.confluence.macros.vote;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserVisualizationTest {

  @Test
  public void test_getFor_should_return_null_for_propertyValue_null() {
    // When:
    UserVisualization result = UserVisualization.getFor(null);

    // Then:
    assertEquals(null, result);
  }

  @Test
  public void test_getFor_should_return_null_for_unknown_propertyValue() {
    // When:
    UserVisualization result = UserVisualization.getFor("unknown");

    // Then:
    assertEquals(null, result);
  }

  @Test
  public void test_getFor_should_return_LINKED_FULL_VoterStyle_for_its_propertyValue() {
    // Given:
    UserVisualization userVisualization = UserVisualization.LINKED_FULL;

    // When:
    UserVisualization result = UserVisualization.getFor(userVisualization.propertyValue);

    // Then:
    assertEquals(userVisualization, result);
  }
}