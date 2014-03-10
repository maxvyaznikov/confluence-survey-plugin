package org.hivesoft.confluence.macros.vote;

import com.atlassian.confluence.content.render.xhtml.*;
import com.atlassian.confluence.content.render.xhtml.storage.DefaultContentTransformerFactory;
import com.atlassian.confluence.content.render.xhtml.storage.macro.AlwaysTransformMacroBody;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroMarshaller;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroUnmarshaller;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.user.impl.DefaultUser;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.hivesoft.confluence.macros.utils.VelocityAbstractionHelper;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.rest.callbacks.delegation.SurveyPluginSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteMacroTest {
  private final static DefaultUser SOME_USER1 = new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de");

  SurveyManager mockSurveyManager = mock(SurveyManager.class);
  TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
  PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);
  VelocityAbstractionHelper mockVelocityAbstractionHelper = mock(VelocityAbstractionHelper.class);
  ConversionContext mockConversionContext = mock(ConversionContext.class);


  VoteMacro classUnderTest;

  @Before
  public void setup() throws Exception {
    AuthenticatedUserThreadLocal.setUser(SOME_USER1);

    XMLOutputFactory xmlOutputFactory = (XMLOutputFactory) new XmlOutputFactoryFactoryBean(true).getObject();

    final EventPublisher mockEventPublisher = mock(EventPublisher.class);
    final Unmarshaller<MacroDefinition> macroDefinitionUnmarshaller = new StorageMacroUnmarshaller(new DefaultXmlEventReaderFactory(), xmlOutputFactory, new AlwaysTransformMacroBody());
    final DefaultXmlEventReaderFactory xmlEventReaderFactory = new DefaultXmlEventReaderFactory();
    final Marshaller<MacroDefinition> macroDefinitionMarshaller = new StorageMacroMarshaller(xmlOutputFactory);


    final DefaultContentTransformerFactory contentTransformerFactory = new DefaultContentTransformerFactory(macroDefinitionUnmarshaller, macroDefinitionMarshaller, xmlEventReaderFactory, xmlOutputFactory, mockEventPublisher);
    final XhtmlContent xhtmlContent = new DefaultXhtmlContent(null, null, null, null, null, null, null, null, null, null, contentTransformerFactory, null);

    classUnderTest = new VoteMacro(mockSurveyManager, mockTemplateRenderer, xhtmlContent, mockPluginSettingsFactory, mockVelocityAbstractionHelper);
  }

  @After
  public void tearDown() {
    AuthenticatedUserThreadLocal.setUser(null);
  }

  @Test(expected = MacroExecutionException.class)
  public void test_execute_noTitleParameter_exception() throws Exception {
    classUnderTest.execute(new HashMap(), "", mockConversionContext);
  }

  @Test(expected = MacroExecutionException.class)
  public void test_execute_voteTitleDuplicateDetected_exception() throws Exception {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someVoteTitle");

    ContentEntityObject somePage = new Page();
    somePage.setBodyAsString("<ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">someVoteTitle</ac:parameter></ac:macro><ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">someVoteTitle</ac:parameter></ac:macro>");
    final PageContext pageContext = new PageContext(somePage);

    when(mockConversionContext.getEntity()).thenReturn(somePage);
    when(mockConversionContext.getPageContext()).thenReturn(pageContext);

    classUnderTest.execute(parameters, "", mockConversionContext);
  }

  @Test(expected = MacroExecutionException.class)
  public void test_execute_voteAndSurveyBallotTitleDuplicateDetected_exception() throws Exception {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someVoteTitle");

    ContentEntityObject somePage = new Page();
    somePage.setBodyAsString("<ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">someVoteTitle</ac:parameter></ac:macro>" +
            "<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">someSurveyTitle</ac:parameter>" +
            "<ac:plain-text-body><![CDATA[notTheFirst\nsomeVoteTitle-subtitle\nsomeOther \"§$\"§$ ballotTitle]]></ac:plain-text-body></ac:macro>");
    final PageContext pageContext = new PageContext(somePage);

    when(mockConversionContext.getEntity()).thenReturn(somePage);
    when(mockConversionContext.getPageContext()).thenReturn(pageContext);

    classUnderTest.execute(parameters, "", mockConversionContext);
  }

  @Test
  public void test_execute_simpleMacroWithTitle_success() throws Exception {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someTitle");

    ContentEntityObject somePage = new Page();
    somePage.setBodyAsString("<ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">someTitle</ac:parameter></ac:macro><ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">someVoteTitle</ac:parameter></ac:macro>");
    final PageContext pageContext = new PageContext(somePage);

    Ballot ballot = new Ballot("someTitle", new VoteConfig(mock(PermissionEvaluator.class), parameters));

    when(mockConversionContext.getEntity()).thenReturn(somePage);
    when(mockConversionContext.getPageContext()).thenReturn(pageContext);
    when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());
    when(mockSurveyManager.reconstructBallotFromPlainTextMacroBody(eq(parameters), anyString(), any(ContentEntityObject.class))).thenReturn(ballot);
    final HashMap<String, Object> contextMap = new HashMap<String, Object>();
    contextMap.put(VelocityManager.ACTION, MacroUtils.getConfluenceActionSupport());
    when(mockVelocityAbstractionHelper.getDefaultVelocityContext()).thenReturn(contextMap);

    final String macroResultAsString = classUnderTest.execute(parameters, "", mockConversionContext);
    //TODO: find a way to remove the mock in templateRenderer otherwise it is a little stupid to simply fake it
    //assertTrue(macroResultAsString.contains("someTitle"));
  }

  @Test
  public void test_MacroProperties_success() {
    assertEquals(Macro.BodyType.PLAIN_TEXT, classUnderTest.getBodyType());
    assertEquals(Macro.OutputType.BLOCK, classUnderTest.getOutputType());
  }
}
