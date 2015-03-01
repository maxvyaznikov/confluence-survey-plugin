/**
 * Copyright (c) 2006-2015, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.DefaultSaveContext;
import com.atlassian.confluence.core.Modification;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.extras.common.log.Logger;

public class TransactionCallbackStorePage implements com.atlassian.sal.api.transaction.TransactionCallback {
  private static final Logger.Log LOG = Logger.getInstance(TransactionCallbackStorePage.class);

  private final PageManager pageManager;
  private final ContentEntityObject contentEntityObject;
  private final String body;

  public TransactionCallbackStorePage(PageManager pageManager, ContentEntityObject contentEntityObject, String body) {
    this.pageManager = pageManager;
    this.contentEntityObject = contentEntityObject;
    this.body = body;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean doInTransaction() {
    pageManager.saveNewVersion(contentEntityObject, new Modification<ContentEntityObject>() {
      public void modify(ContentEntityObject page) {
        page.setBodyAsString(body);
      }
    }, new DefaultSaveContext(true, false, true));

    LOG.debug("page has been updated with body: " + body);

    return true;
  }
}
