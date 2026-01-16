package com.creditapp.unit.shared.model;

import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrganizationEntityTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    void saveAndFindOrganization() {
        Organization org = new Organization();
        org.setName("Test Bank");
        org.setTaxId("TAX123");
        org.setCountryCode("US");
        org.setActive(true);

        organizationRepository.save(org);

        Optional<Organization> found = organizationRepository.findById(org.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Bank");
    }

    @Test
    void organizationPersistsWithId() {
        Organization org = new Organization();
        org.setName("Another Bank");
        org.setTaxId("TAX456");
        org.setCountryCode("MD");
        org.setActive(true);

        Organization saved = organizationRepository.save(org);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void organizationTimestampsAreSet() {
        Organization org = new Organization();
        org.setName("Timestamp Test Bank");
        org.setTaxId("TAX789");
        org.setCountryCode("EU");
        org.setActive(true);

        Organization saved = organizationRepository.save(org);
        assertThat(saved.getId()).isNotNull();
    }
}
