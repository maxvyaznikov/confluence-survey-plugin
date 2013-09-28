package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.opensymphony.xwork.Action;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class AddCommentActionTest {

    ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);

    AddCommentAction classUnderTest;

    @Before
    public void setup() {
        classUnderTest = new AddCommentAction();
        classUnderTest.setContentPropertyManager(mockContentPropertyManager);
    }

    @Test
    public void test_execute_noBallot_success() {
        classUnderTest.setBallotTitle(null);

        final String returnValue = classUnderTest.execute();

        assertEquals(Action.ERROR, returnValue);
    }
}
