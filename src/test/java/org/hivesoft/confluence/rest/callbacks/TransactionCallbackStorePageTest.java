package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;


public class TransactionCallbackStorePageTest {
  PageManager mockPageManager = mock(PageManager.class);

  TransactionCallbackStorePage classUnderTest;

  @Test
  public void test_doInTransaction() {

    Page somePage = new Page();
    String someBody = "someContentToStore";

    classUnderTest = new TransactionCallbackStorePage(mockPageManager, somePage, someBody);

    final Boolean success = classUnderTest.doInTransaction();

    assertThat(success, is(true));
  }
}
