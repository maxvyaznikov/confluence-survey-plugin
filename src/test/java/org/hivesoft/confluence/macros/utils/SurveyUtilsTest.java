package org.hivesoft.confluence.macros.utils;

import com.atlassian.renderer.v2.macro.MacroException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    private static String getRandomString(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append((char) ((int) (Math.random() * 26) + 97));
        }
        return sb.toString();
    }
}