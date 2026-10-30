package com.karolsol.nabu;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationStructureTests {

  ApplicationModules modules = ApplicationModules.of(NabuApplication.class);

  @Test
  void verifyModuleStructure() {
    modules.verify();
  }
}
