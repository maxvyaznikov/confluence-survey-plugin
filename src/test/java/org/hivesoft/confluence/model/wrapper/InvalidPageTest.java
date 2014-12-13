package org.hivesoft.confluence.model.wrapper;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class InvalidPageTest {

  InvalidPage classUnderTest;

  @Test
  public void test_unimplementedMethods_success() throws Exception {
    classUnderTest = new InvalidPage(Response.Status.OK, "someMessage");
    assertThat(classUnderTest.getLinkWikiMarkup(), is(nullValue()));
    assertThat(classUnderTest.getNameForComparison(), is(nullValue()));
    assertThat(classUnderTest.getType(), is(nullValue()));
    assertThat(classUnderTest.getUrlPath(), is("InvalidPageUrl"));
  }

  @Test
  public void testToResponse() throws Exception {
    classUnderTest = new InvalidPage(Response.Status.CREATED, "someMessage");
    Response result = classUnderTest.toResponse();

    assertThat(result.getStatus(), is(201));
    assertThat((String) result.getEntity(), is("someMessage"));
  }
}