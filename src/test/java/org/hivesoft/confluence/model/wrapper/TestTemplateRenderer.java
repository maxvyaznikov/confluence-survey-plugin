package org.hivesoft.confluence.model.wrapper;

import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class TestTemplateRenderer implements TemplateRenderer {

  @Override
  public void render(String s, Writer writer) throws RenderingException, IOException {

  }

  @Override
  public void render(String pathToTemplate, Map<String, Object> map, Writer writer) throws RenderingException, IOException {
    writer.write(pathToTemplate);
  }

  @Override
  public String renderFragment(String s, Map<String, Object> map) throws RenderingException {
    return null;
  }

  @Override
  public boolean resolve(String s) {
    return false;
  }
}
