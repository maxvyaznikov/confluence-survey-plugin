package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.content.render.xhtml.*;
import com.atlassian.confluence.content.render.xhtml.storage.DefaultContentTransformerFactory;
import com.atlassian.confluence.content.render.xhtml.storage.macro.AlwaysTransformMacroBody;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroMarshaller;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroUnmarshaller;
import com.atlassian.confluence.core.ContentEntityObject;
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
import org.hivesoft.confluence.macros.VelocityAbstractionHelper;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.rest.callbacks.SurveyPluginSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyMacroTest {
  private final static DefaultUser SOME_USER1 = new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de");
  private final static String SOME_SURVEY_TITLE = "someSurveyTitle";

  SurveyManager mockSurveyManager = mock(SurveyManager.class);
  TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
  PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);
  VelocityAbstractionHelper mockVelocityAbstractionHelper = mock(VelocityAbstractionHelper.class);
  EventPublisher mockEventPublisher = mock(EventPublisher.class);

  ConversionContext mockConversionContext = mock(ConversionContext.class);

  SurveyMacro classUnderTest;

  @Before
  public void setup() throws Exception {
    XMLOutputFactory xmlOutputFactory = (XMLOutputFactory) new XmlOutputFactoryFactoryBean(true).getObject();

    final Unmarshaller<MacroDefinition> macroDefinitionUnmarshaller = new StorageMacroUnmarshaller(new DefaultXmlEventReaderFactory(), xmlOutputFactory, new AlwaysTransformMacroBody());
    final DefaultXmlEventReaderFactory xmlEventReaderFactory = new DefaultXmlEventReaderFactory();
    final Marshaller<MacroDefinition> macroDefinitionMarshaller = new StorageMacroMarshaller(xmlOutputFactory);

    final DefaultContentTransformerFactory contentTransformerFactory = new DefaultContentTransformerFactory(macroDefinitionUnmarshaller, macroDefinitionMarshaller, xmlEventReaderFactory, xmlOutputFactory, mockEventPublisher);
    final XhtmlContent xhtmlContent = new DefaultXhtmlContent(null, null, null, null, null, null, null, null, null, null, contentTransformerFactory, null);

    //when(mockRequest.getParameter(VoteMacro.REQUEST_PARAMETER_BALLOT)).thenReturn(SurveyUtilsTest.SOME_BALLOT_TITLE);
    //when(mockRequest.getParameter(VoteMacro.REQUEST_PARAMETER_CHOICE)).thenReturn(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    AuthenticatedUserThreadLocal.setUser(SOME_USER1);

    classUnderTest = new SurveyMacro(mockPluginSettingsFactory, mockSurveyManager, mockTemplateRenderer, xhtmlContent, mockVelocityAbstractionHelper);
  }

  @After
  public void tearDown() {
    AuthenticatedUserThreadLocal.setUser(null);

  }

  @Test
  public void test_execute_simpleMacroWithTitle_success() throws Exception {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_TITLE, SOME_SURVEY_TITLE);

    ContentEntityObject somePage = new Page();
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    final PageContext pageContext = new PageContext(somePage);

    final SurveyConfig config = new SurveyConfig(mock(PermissionEvaluator.class), parameters);
    Survey survey = new Survey(config);
    survey.addBallot(new Ballot("Should this be exported?", config));
    survey.addBallot(new Ballot("How do you like the modern iconSet?", config));

    when(mockConversionContext.getEntity()).thenReturn(somePage);
    when(mockConversionContext.getPageContext()).thenReturn(pageContext);
    when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());
    final Map<String, Object> contextMap = new HashMap<String, Object>();
    contextMap.put(VelocityManager.ACTION, MacroUtils.getConfluenceActionSupport());
    when(mockVelocityAbstractionHelper.getDefaultVelocityContext()).thenReturn(contextMap);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), any(ContentEntityObject.class), eq(parameters))).thenReturn(survey);

    final String macroResultAsString = classUnderTest.execute(parameters, "Should this be exported?\nHow do you like the modern iconSet?", mockConversionContext);
    System.out.println("Macro result: " + macroResultAsString);
    //TODO: find a way to let the templateRenderer render something... as it is a currently a mock, it ultimately returns nothing
    //assertTrue(macroResultAsString.contains(SOME_SURVEY_TITLE));
  }

  @Test//(expected = MacroExecutionException.class)
  public void test_execute_simpleMacroWithTitle_sameBallotTwice_exception() throws Exception {
    final Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_TITLE, SOME_SURVEY_TITLE);

    ContentEntityObject somePage = new Page();
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?\nShould this be exported?]]></ac:plain-text-body></ac:macro>");
    final PageContext pageContext = new PageContext(somePage);


    final SurveyConfig config = new SurveyConfig(mock(PermissionEvaluator.class), parameters);
    Survey survey = new Survey(config);
    survey.addBallot(new Ballot("Should this be exported?", config));
    survey.addBallot(new Ballot("How do you like the modern iconSet?", config));
    survey.addBallot(new Ballot("Should this be exported?", config));

    when(mockConversionContext.getEntity()).thenReturn(somePage);
    when(mockConversionContext.getPageContext()).thenReturn(pageContext);
    when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), any(ContentEntityObject.class), eq(parameters))).thenReturn(survey);

    classUnderTest.execute(parameters, "Should this be exported?\nHow do you like the modern iconSet?\nShould this be exported?", mockConversionContext);
  }
}
