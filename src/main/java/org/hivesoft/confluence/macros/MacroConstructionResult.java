package org.hivesoft.confluence.macros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacroConstructionResult {
  private final List<String> problems = new ArrayList<String>();

  public void addProblems(String... problem) {
    problems.addAll(Arrays.asList(problem));
  }

  public List<String> getProblems() {
    return problems;
  }

  public boolean hasProblems() {
    return !problems.isEmpty();
  }
}
