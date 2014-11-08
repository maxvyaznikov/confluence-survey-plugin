package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.Modification;
import com.atlassian.confluence.core.SaveContext;
import com.atlassian.confluence.pages.PageManager;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class TransactionCallbackStorePageTest {
  PageManager mockPageManager = mock(PageManager.class);
  ContentEntityObject mockContentEntityObject = mock(ContentEntityObject.class);

  TransactionCallbackStorePage classUnderTest;

  @Test
  public void test_doInTransaction_modified_success() {
    String someBody = "someContentToStore";

    classUnderTest = new TransactionCallbackStorePage(mockPageManager, mockContentEntityObject, someBody);

    final Boolean result = classUnderTest.doInTransaction();

    verify(mockPageManager).saveNewVersion(eq(mockContentEntityObject), any(Modification.class), any(SaveContext.class));
    //how to test that inner class ? .. verify(mockContentEntityObject).setBodyAsString(someBody);
    assertThat(result, is(true));
  }
}
