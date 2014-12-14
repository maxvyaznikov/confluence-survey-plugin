package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.content.render.xhtml.*;
import com.atlassian.confluence.content.render.xhtml.storage.DefaultContentTransformerFactory;
import com.atlassian.confluence.content.render.xhtml.storage.macro.AlwaysTransformMacroBody;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroMarshaller;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroUnmarshaller;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.model.Survey;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Comment;
import org.hivesoft.confluence.model.wrapper.TestTemplateRenderer;
import org.hivesoft.confluence.rest.callbacks.delegation.SurveyPluginSettings;
import org.hivesoft.confluence.utils.PermissionEvaluator;
import org.hivesoft.confluence.utils.SurveyManager;
import org.hivesoft.confluence.utils.SurveyUtils;
import org.hivesoft.confluence.utils.VelocityAbstractionHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyMacroTest extends ConfluenceTestBase {

  private final SurveyManager mockSurveyManager = mock(SurveyManager.class);
  private final PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);
  private final VelocityAbstractionHelper mockVelocityAbstractionHelper = mock(VelocityAbstractionHelper.class);
  private final EventPublisher mockEventPublisher = mock(EventPublisher.class);
  private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
  private final PageManager mockPageManager = mock(PageManager.class);
  private final PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
  private final ConversionContext mockConversionContext = mock(ConversionContext.class);

  private final TemplateRenderer testTemplateRenderer = new TestTemplateRenderer();

  private SurveyMacro classUnderTest;

  @Before
  public void setup() throws Exception {
    XMLOutputFactory xmlOutputFactory = (XMLOutputFactory) new XmlOutputFactoryFactoryBean(true).getObject();

    final Unmarshaller<MacroDefinition> macroDefinitionUnmarshaller = new StorageMacroUnmarshaller(new DefaultXmlEventReaderFactory(), xmlOutputFactory, new AlwaysTransformMacroBody());
    final DefaultXmlEventReaderFactory xmlEventReaderFactory = new DefaultXmlEventReaderFactory();
    final Marshaller<MacroDefinition> macroDefinitionMarshaller = new StorageMacroMarshaller(xmlOutputFactory);

    final DefaultContentTransformerFactory contentTransformerFactory = new DefaultContentTransformerFactory(macroDefinitionUnmarshaller, macroDefinitionMarshaller, xmlEventReaderFactory, xmlOutputFactory, mockEventPublisher);
    final XhtmlContent xhtmlContent = new DefaultXhtmlContent(null, null, null, null, null, null, null, null, null, null, contentTransformerFactory, null);

    AuthenticatedUserThreadLocal.setUser(SOME_USER1);

    classUnderTest = new SurveyMacro(mockPluginSettingsFactory, mockSurveyManager, testTemplateRenderer, xhtmlContent, mockVelocityAbstractionHelper, mockPageManager, transactionTemplate);
  }

  @After
  public void tearDown() {
    AuthenticatedUserThreadLocal.setUser(null);
  }

  @Test
  public void test_execute_simpleMacroWithTitle_success() throws Exception {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_TITLE, SOME_SURVEY_TITLE);

    final String[] ballotTitles = {"Should this be exported?", "How do you like the modern iconSet?"};

    ContentEntityObject somePage = new Page();
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body>"
            + "<![CDATA["
            + StringUtils.join(ballotTitles, "\n")
            + "]]></ac:plain-text-body></ac:macro>");
    final PageContext pageContext = new PageContext(somePage);

    when(mockPermissionEvaluator.getRemoteUser()).thenReturn(SOME_USER1);
    when(mockPermissionEvaluator.isPermissionListEmptyOrContainsGivenUser(any(List.class), eq(SOME_USER1))).thenReturn(true);

    Survey survey = surveyWithBallots(parameters, ballotTitles);

    when(mockConversionContext.getEntity()).thenReturn(somePage);
    when(mockConversionContext.getPageContext()).thenReturn(pageContext);
    when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());
    final Map<String, Object> contextMap = new HashMap<String, Object>();
    contextMap.put(VelocityManager.ACTION, MacroUtils.getConfluenceActionSupport());
    when(mockVelocityAbstractionHelper.getDefaultVelocityContext()).thenReturn(contextMap);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), any(ContentEntityObject.class), eq(parameters))).thenReturn(survey);

    final String result = classUnderTest.execute(parameters, StringUtils.join(ballotTitles, "\n"), mockConversionContext);

    assertThat(result, is("templates/macros/survey/surveymacro.vm"));
  }

  @Test(expected = MacroExecutionException.class)
  public void test_execute_entityNull_exception() throws Exception {
    final Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_TITLE, SOME_SURVEY_TITLE);

    final String[] ballotTitles = {"Should this be exported?", "How do you like the modern iconSet?", "Should this be exported?"};

    when(mockConversionContext.getEntity()).thenReturn(null);

    classUnderTest.execute(parameters, StringUtils.join(ballotTitles, "\n"), mockConversionContext);
  }

  @Test
  public void test_execute_simpleMacroWithTitle_sameBallotTwice_renderWarning_success() throws Exception {
    final Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_TITLE, SOME_SURVEY_TITLE);

    final String[] ballotTitles = {"Should this be exported?", "How do you like the modern iconSet?", "Should this be exported?"};

    ContentEntityObject somePage = new Page();
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body>"
            + "<![CDATA["
            + StringUtils.join(ballotTitles, "\n")
            + "]]></ac:plain-text-body></ac:macro>");
    final PageContext pageContext = new PageContext(somePage);


    Survey survey = surveyWithBallots(parameters, ballotTitles);

    when(mockConversionContext.getEntity()).thenReturn(somePage);
    when(mockConversionContext.getPageContext()).thenReturn(pageContext);
    when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), any(ContentEntityObject.class), eq(parameters))).thenReturn(survey);

    final String result = classUnderTest.execute(parameters, StringUtils.join(ballotTitles, "\n"), mockConversionContext);

    assertThat(result, is("templates/macros/survey/surveymacro-renderproblems.vm"));
  }

  @Test
  public void test_MacroProperties_success() {
    assertThat(classUnderTest.getBodyType(), is(Macro.BodyType.PLAIN_TEXT));
    assertThat(classUnderTest.getOutputType(), is(Macro.OutputType.BLOCK));
  }

  /**
   * Helper Methods
   */
  private Survey surveyWithBallots(Map<String, String> parameters, String... ballotTitles) {
    final SurveyConfig config = new SurveyConfig(mockPermissionEvaluator, parameters);
    Survey survey = new Survey(config);
    for (String ballotTitle : ballotTitles) {
      survey.addBallot(new Ballot(ballotTitle, "", config, SurveyUtils.getDefaultChoices(), new ArrayList<Comment>()));
    }
    return survey;
  }
}
