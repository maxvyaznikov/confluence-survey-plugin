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
package org.hivesoft.confluence.model.wrapper;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.user.User;

public class SurveyUser implements User {

  private final String name;
  private final String fullName;
  private final String email;

  public SurveyUser(String name) {
    this(name, name, "");
  }

  private SurveyUser(String name, String fullName, String email) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("parameter name must not be empty!");
    }
    this.name = name;
    this.fullName = fullName;
    this.email = email;
  }

  public SurveyUser(User user) {
    this(user.getName(), user.getFullName(), user.getEmail());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getFullName() {
    return fullName;
  }

  @Override
  public String getEmail() {
    return email;
  }

  /**
   * a user is uniquely identified via the name (key) so the DefaultUser implementation is useless, sadly
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SurveyUser that = (SurveyUser) o;

    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "SurveyUser{" +
            "name='" + name + '\'' +
            ", fullName='" + fullName + '\'' +
            ", email='" + email + '\'' +
            '}';
  }
}
