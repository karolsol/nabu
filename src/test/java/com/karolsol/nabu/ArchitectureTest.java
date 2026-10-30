package com.karolsol.nabu;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.karolsol.nabu")
public class ArchitectureTest {

  @ArchTest static final ArchRule no_field_injection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

  @ArchTest
  static final ArchRule controllers_should_be_properly_named =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller");

  @ArchTest
  static final ArchRule controllers_must_reside_in_internal_package =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("..internal");

  @ArchTest
  static final ArchRule controllers_should_not_depend_on_repositories =
      noClasses()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .dependOnClassesThat(assignableTo(JpaRepository.class));

  @ArchTest
  static final ArchRule services_should_be_named_correctly =
      classes()
          .that()
          .areAnnotatedWith(Service.class)
          .should()
          .haveSimpleNameEndingWith("Service")
          .orShould()
          .haveSimpleNameEndingWith("Facade");

  @ArchTest
  static final ArchRule internal_services_should_end_with_service =
      classes()
          .that()
          .areAnnotatedWith(Service.class)
          .and()
          .resideInAPackage("..internal..")
          .should()
          .haveSimpleNameEndingWith("Service");

  @ArchTest
  static final ArchRule public_services_should_end_with_facade =
      classes()
          .that()
          .areAnnotatedWith(Service.class)
          .and()
          .resideOutsideOfPackage("..internal..")
          .should()
          .haveSimpleNameEndingWith("Facade");

  @ArchTest
  static final ArchRule entities_should_be_in_internal_package =
      classes()
          .that()
          .areAnnotatedWith(jakarta.persistence.Entity.class)
          .should()
          .resideInAPackage("..internal");

  @ArchTest
  static final ArchRule entities_should_be_named_correctly =
      classes()
          .that()
          .areAnnotatedWith(jakarta.persistence.Entity.class)
          .should()
          .haveSimpleNameEndingWith("Entity");

  @ArchTest
  static final ArchRule repositories_should_be_named_correctly =
      classes()
          .that(assignableTo(JpaRepository.class))
          .should()
          .haveSimpleNameEndingWith("Repository");

  @ArchTest
  static final ArchRule mappers_should_be_in_internal =
      classes().that().haveSimpleNameEndingWith("Mapper").should().resideInAPackage("..internal..");
}
