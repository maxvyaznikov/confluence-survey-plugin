package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.transaction.TransactionCallback;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TransactionCallbackAddAttachment implements TransactionCallback<Attachment> {
  private static final Logger.Log LOG = Logger.getInstance(TransactionCallbackAddAttachment.class);

  private PageManager pageManager;
  private ContentEntityObject contentEntityObject;
  private String fileName;
  private byte[] attachmentData;

  public TransactionCallbackAddAttachment(PageManager pageManager, Page contentEntityObject, String fileName, byte[] attachmentData) {
    this.pageManager = pageManager;
    this.contentEntityObject = contentEntityObject;
    this.fileName = fileName;
    this.attachmentData = attachmentData;
  }

  @Override
  public Attachment doInTransaction() {
    LOG.info("adding fileName: " + fileName);

    Attachment attachment = new Attachment();
    attachment.setFileName(fileName);
    attachment.setContentType("text/plain");
    //attachment.setVersion(1);
    attachment.setFileSize(attachmentData.length);
    attachment.setComment("survey export");
    attachment.setContent(contentEntityObject);

    contentEntityObject.addAttachment(attachment);
    try {
      pageManager.getAttachmentManager().saveAttachment(attachment, null, new ByteArrayInputStream(attachmentData));
    } catch (IOException e) {
      LOG.warn("fileName made problems: " + e.getMessage(), e);
    }
    return attachment;
  }
}
