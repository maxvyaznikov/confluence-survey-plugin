package org.hivesoft.confluence.admin;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminResourceTest {

    private final UserManager mockUserManager = mock(UserManager.class);
    private final PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);
    private final TransactionTemplate mockTransactionTemplate = mock(TransactionTemplate.class);

    private static final String TEST_USERNAME = "someUserName";

    AdminResource classUnderTest = new AdminResource(mockUserManager, mockPluginSettingsFactory, mockTransactionTemplate);

    @Test
    public void test_getConfig_default_success() {
        when(mockUserManager.getRemoteUsername()).thenReturn(TEST_USERNAME);
        when(mockUserManager.isSystemAdmin(TEST_USERNAME)).thenReturn(true);
        final Response response = classUnderTest.getConfig();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_getConfig_userNotAnAdmin_failure() {

        when(mockUserManager.getRemoteUsername()).thenReturn(TEST_USERNAME);
        when(mockUserManager.isSystemAdmin(TEST_USERNAME)).thenReturn(false);

        final Response response = classUnderTest.getConfig();

        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void test_getConfig_userNotFound_failure() {
        when(mockUserManager.getRemoteUsername()).thenReturn(null);

        final Response response = classUnderTest.getConfig();

        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void test_putConfig_default_success() {
        when(mockUserManager.getRemoteUsername()).thenReturn(TEST_USERNAME);
        when(mockUserManager.isSystemAdmin(TEST_USERNAME)).thenReturn(true);
        final Response response = classUnderTest.putConfig(null);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_putConfig_userNotAnAdmin_failure() {

        when(mockUserManager.getRemoteUsername()).thenReturn(TEST_USERNAME);
        when(mockUserManager.isSystemAdmin(TEST_USERNAME)).thenReturn(false);

        final Response response = classUnderTest.putConfig(null);

        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void test_putConfig_userNotFound_failure() {
        when(mockUserManager.getRemoteUsername()).thenReturn(null);

        final Response response = classUnderTest.putConfig(null);

        assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
