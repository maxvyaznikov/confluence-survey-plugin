package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CSVExportRepresentationTest {
  CSVExportRepresentation classUnderTest;

  @Test
  public void test_gettersSetters_success() {
    classUnderTest = new CSVExportRepresentation("someUri");
    assertThat("someUri", is(equalTo(classUnderTest.getUri())));

    assertTrue(classUnderTest.equals(new CSVExportRepresentation("someUri")));
    assertFalse(classUnderTest.equals(null));
    assertFalse(classUnderTest.equals("someString"));
  }
}
