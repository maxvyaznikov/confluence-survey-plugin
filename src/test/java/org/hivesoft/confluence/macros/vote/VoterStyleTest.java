package org.hivesoft.confluence.macros.vote;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VoterStyleTest {

  @Test
  public void test_getFor_should_return_null_for_propertyValue_null() {
    // When:
    VoterStyle result = VoterStyle.getFor(null);

    // Then:
    assertEquals(null, result);
  }

  @Test
  public void test_getFor_should_return_null_for_unknown_propertyValue() {
    // When:
    VoterStyle result = VoterStyle.getFor("unknown");

    // Then:
    assertEquals(null, result);
  }

  @Test
  public void test_getFor_should_return_LINKED_FULL_VoterStyle_for_its_propertyValue() {
    // Given:
    VoterStyle voterStyle = VoterStyle.LINKED_FULL;

    // When:
    VoterStyle result = VoterStyle.getFor(voterStyle.propertyValue);

    // Then:
    assertEquals(voterStyle, result);
  }
}