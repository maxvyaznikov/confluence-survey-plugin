package org.hivesoft.confluence.admin;

import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserProfile;

import java.net.URI;

//TEST Helper Class
final class TestUserProfile implements UserProfile {

    private UserKey userKey;
    private String userName;

    public TestUserProfile(String userKeyString, String userName) {
        this.userKey = new UserKey(userKeyString);
        this.userName = userName;
    }

    @Override
    public UserKey getUserKey() {
        return userKey;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public URI getProfilePictureUri(int width, int height) {
        return null;
    }

    @Override
    public URI getProfilePictureUri() {
        return null;
    }

    @Override
    public URI getProfilePageUri() {
        return null;
    }
}
