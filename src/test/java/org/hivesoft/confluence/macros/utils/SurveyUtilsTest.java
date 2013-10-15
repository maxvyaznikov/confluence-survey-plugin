package org.hivesoft.confluence.macros.utils;

import com.atlassian.renderer.v2.macro.MacroException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SurveyUtilsTest {

    public static final String BALLOT_AND_CHOICENAME1 = "someBallot.withSomeChoiceName1";
    public static final String BALLOT_AND_CHOICENAME2 = "someBallot.withSomeChoiceName2";
    public static final String BALLOT_AND_CHOICENAME3 = "someBallot.withSomeChoiceName3";


    @Test
    public void test_validateMaxStorableKeyLength_success() throws MacroException {
        List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

        ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME1);
        ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME2);
        ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME3);

        SurveyUtils.validateMaxStorableKeyLength(ballotAndChoicesWithValidLength);
    }

    @Test(expected = MacroException.class)
    public void test_validateMaxStorableKeyLength_exception() throws MacroException {
        List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

        ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH + 1));
        ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH));

        SurveyUtils.validateMaxStorableKeyLength(ballotAndChoicesWithValidLength);
    }

    @Test
    public void test_getBooleanFromString_success() {
        assertTrue(SurveyUtils.getBooleanFromString("true", false));
        assertFalse(SurveyUtils.getBooleanFromString("false", true));
        assertTrue(SurveyUtils.getBooleanFromString("", true));
        assertFalse(SurveyUtils.getBooleanFromString(null, false));
    }

    // local helper methods
    private static String getRandomString(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append((char) ((int) (Math.random() * 26) + 97));
        }
        return sb.toString();
    }
}