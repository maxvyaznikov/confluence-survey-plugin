package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallbackAddAttachmentTest {

  PageManager mockPageManager = mock(PageManager.class);

  TransactionCallbackAddAttachment classUnderTest;

  @Test
  public void test_doInTransaction_success() {
    classUnderTest = new TransactionCallbackAddAttachment(mockPageManager, new Page(), "someFileName", new byte[]{'a', 'b'});
    AttachmentManager mockAttachmentManager = mock(AttachmentManager.class);
    when(mockPageManager.getAttachmentManager()).thenReturn(mockAttachmentManager);

    final Attachment returnAttachment = classUnderTest.doInTransaction();

    assertThat("someFileName", is(equalTo(returnAttachment.getFileName())));
  }
}
