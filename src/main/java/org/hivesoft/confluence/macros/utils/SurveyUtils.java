package org.hivesoft.confluence.macros.utils;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.v2.macro.MacroException;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.vote.VoteMacro;

import java.util.List;

public class SurveyUtils {
    private static final Logger.Log LOG = Logger.getInstance(SurveyUtils.class);

    // 1.1.7.7 define the max length that is storable to the propertyEntry-key field
    protected static final int MAX_STORABLE_KEY_LENGTH = 200;

    public static void validateMaxStorableKeyLength(List<String> ballotAndChoiceNames) throws MacroException {
        // 1.1.7.7 ballot title and choices too long will crash the system if exceeding 200 chars for entity_key. So check this on rendering
        String strExceedsKeyItems = "";
        for (String ballotChoiceKey : ballotAndChoiceNames) {
            try {
                // 1.1.7.8 check for unicode-characters. They consume more space than they sometimes are allowed. add 5 to the calculated length (prefix for vote)
                if (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length() > MAX_STORABLE_KEY_LENGTH) {
                    if (strExceedsKeyItems == "")
                        strExceedsKeyItems += ", ";
                    strExceedsKeyItems += ballotChoiceKey + " Length: " + (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length());
                }
            } catch (java.io.UnsupportedEncodingException e) {
                throw new MacroException(e);
            }
        }

        if (strExceedsKeyItems != "") {
            final String message = "Error detected Length of BallotTitle and Choices are to long to be stored to the database (MaxLength:" + MAX_STORABLE_KEY_LENGTH + "). Problematic Names: " + strExceedsKeyItems + "!";
            LOG.error(message);
            throw new MacroException(message);
        }
    }

    /**
     * Get the boolean value of a String and fallback to the defaultValue if its not a boolean
     */
    public static boolean getBooleanFromString(String stringToParse, boolean defaultValue) {
        if (StringUtils.defaultString(stringToParse).equals("")) {
            return defaultValue;
        } else {
            return Boolean.valueOf(stringToParse);
        }
    }


}
