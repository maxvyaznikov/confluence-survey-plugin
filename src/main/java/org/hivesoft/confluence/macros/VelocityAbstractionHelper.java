package org.hivesoft.confluence.macros;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;

import java.util.Map;

public class VelocityAbstractionHelper {

    public Map<String, Object> getDefaultVelocityContext() {
        return MacroUtils.defaultVelocityContext();
    }
}
