package org.hivesoft.confluence.admin;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminServletTest extends ConfluenceTestBase {

  private final UserManager mockUserManager = mock(UserManager.class);
  private final LoginUriProvider mockLoginUriProvider = mock(LoginUriProvider.class);
  private final TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);

  AdminServlet classUnderTest = new AdminServlet(mockUserManager, mockLoginUriProvider, mockTemplateRenderer);

  @Test
  public void test_doGet_success() throws IOException, ServletException {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    when(mockUserManager.getRemoteUsername()).thenReturn(SOME_USER1.getName());
    when(mockUserManager.isSystemAdmin(SOME_USER1.getName())).thenReturn(true);

    classUnderTest.doGet(mockRequest, mockResponse);
  }

  @Test
  public void test_doGet_userNotFound_failure() throws IOException, ServletException, URISyntaxException {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    when(mockUserManager.getRemoteUsername()).thenReturn(null);
    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer());
    when(mockRequest.getQueryString()).thenReturn("/someWikiPage");
    when(mockLoginUriProvider.getLoginUri(any(URI.class))).thenReturn(new URI("http://localhost:123/login"));

    classUnderTest.doGet(mockRequest, mockResponse);
  }
}
