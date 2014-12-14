package org.hivesoft.confluence.macros;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Page;
import org.hivesoft.confluence.utils.SurveyManager;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MacroConstructionResultTest {

  private final SurveyManager mockSurveyManager = mock(SurveyManager.class);

  private MacroConstructionResult classUnderTest;

  @Before
  public void setUp() {
    ContentEntityObject contentEntityObject = new Page();
    contentEntityObject.setId(123L);

    when(mockSurveyManager.canCreatePage(contentEntityObject)).thenReturn(true);
    when(mockSurveyManager.canAttachFile(contentEntityObject)).thenReturn(true);

    classUnderTest = new MacroConstructionResult(mockSurveyManager, contentEntityObject);
  }

  /**
   * Actually this method is used by velocity and tested with the macros but getProblems isn't called within java
   */
  @Test
  public void test_getProblems() throws Exception {
    assertThat(classUnderTest.getProblems(), hasSize(0));
    assertThat(classUnderTest.hasProblems(), is(false));

    classUnderTest.addProblems("first problem", "second problem");

    assertThat(classUnderTest.getProblems(), containsInAnyOrder("first problem", "second problem"));
    assertThat(classUnderTest.hasProblems(), is(true));
  }

  @Test
  public void test_getGettersFilledByConstructor() {
    assertThat(classUnderTest.isCanAttachFile(), is(true));
    assertThat(classUnderTest.isCanCreatePage(), is(true));
    assertThat(classUnderTest.getContentId(), is(123L));
  }
}