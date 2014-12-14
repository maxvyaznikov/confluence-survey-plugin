package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TransactionCallbackAddAttachmentTest {

  private final PageManager mockPageManager = mock(PageManager.class);
  private final AttachmentManager mockAttachmentManager = mock(AttachmentManager.class);

  private TransactionCallbackAddAttachment classUnderTest;

  @Test
  public void test_doInTransaction_success() {
    when(mockPageManager.getAttachmentManager()).thenReturn(mockAttachmentManager);

    classUnderTest = new TransactionCallbackAddAttachment(mockPageManager, new Page(), "someFileName", new byte[]{'a', 'b'});

    final Attachment returnAttachment = classUnderTest.doInTransaction();

    assertThat("someFileName", is(equalTo(returnAttachment.getFileName())));
  }

  @Test
  public void test_doInTransaction_IOException_expectNull_exception() throws IOException {

    when(mockPageManager.getAttachmentManager()).thenReturn(mockAttachmentManager);
    doThrow(new IOException("")).when(mockAttachmentManager).saveAttachment(any(Attachment.class), any(Attachment.class), any(InputStream.class));

    classUnderTest = new TransactionCallbackAddAttachment(mockPageManager, new Page(), "someFileName", new byte[]{'a', 'b'});

    final Attachment returnAttachment = classUnderTest.doInTransaction();

    assertThat(returnAttachment, nullValue());
  }
}
