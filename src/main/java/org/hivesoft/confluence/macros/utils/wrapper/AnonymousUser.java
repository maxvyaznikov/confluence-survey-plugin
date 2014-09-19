package org.hivesoft.confluence.macros.utils.wrapper;

import com.atlassian.user.User;

public class AnonymousUser implements User {
  @Override
  public String getFullName() {
    return "I am not the User you are looking for";
  }

  @Override
  public String getEmail() {
    return "NoOne@hivesoft.org";
  }

  @Override
  public String getName() {
    return "Anonymous";
  }
}
