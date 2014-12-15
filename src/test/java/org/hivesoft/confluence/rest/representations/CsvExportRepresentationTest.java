package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsvExportRepresentationTest {

  @Test
  public void test_gettersSetters_success() {
    CSVExportRepresentation classUnderTest = new CSVExportRepresentation("someTitle", "someUri");

    assertThat(classUnderTest.getTitle(), is("someTitle"));
    assertThat(classUnderTest.getUri(), is("someUri"));

    CSVExportRepresentation anotherInstance = new CSVExportRepresentation("someTitle", "someUri");
    assertTrue(classUnderTest.equals(anotherInstance));
    assertFalse(classUnderTest.equals(null));
    assertFalse(classUnderTest.equals("someString"));

    assertThat(classUnderTest.hashCode(), is(anotherInstance.hashCode()));
  }
}
