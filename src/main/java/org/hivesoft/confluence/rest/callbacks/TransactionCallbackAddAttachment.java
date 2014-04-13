/**
 * Copyright (c) 2006-2014, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.transaction.TransactionCallback;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TransactionCallbackAddAttachment implements TransactionCallback<Attachment> {
  private static final Logger.Log LOG = Logger.getInstance(TransactionCallbackAddAttachment.class);

  private PageManager pageManager;
  private AbstractPage abstractPage;
  private String fileName;
  private byte[] attachmentData;

  public TransactionCallbackAddAttachment(PageManager pageManager, AbstractPage abstractPage, String fileName, byte[] attachmentData) {
    this.pageManager = pageManager;
    this.abstractPage = abstractPage;
    this.fileName = fileName;
    this.attachmentData = attachmentData;
  }

  @Override
  public Attachment doInTransaction() {
    LOG.info("Try to store attachment with fileName: " + fileName);

    Attachment attachment = new Attachment(fileName, "text/plain", attachmentData.length, "survey export");
    abstractPage.addAttachment(attachment);

    try {
      pageManager.getAttachmentManager().saveAttachment(attachment, null, new ByteArrayInputStream(attachmentData));
    } catch (IOException e) {
      LOG.warn("There was a problem while trying to store the attachment: " + e.getMessage(), e);
      return null;
    }
    return attachment;
  }
}
