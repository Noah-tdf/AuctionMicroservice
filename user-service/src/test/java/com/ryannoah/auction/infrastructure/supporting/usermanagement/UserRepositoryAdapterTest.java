package com.ryannoah.auction.infrastructure.supporting.usermanagement;

import com.ryannoah.auction.domain.supporting.usermanagement.Address;
import com.ryannoah.auction.domain.supporting.usermanagement.Email;
import com.ryannoah.auction.domain.supporting.usermanagement.User;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepositoryAdapter repositoryAdapter;

    @Test
    void shouldSaveAndLoadUser() {
        User saved = repositoryAdapter.save(User.create(
                "new-user",
                new Email("new-user@example.com"),
                true,
                new Address("1 Main", "Montreal", "H1H1H1", "Canada")
        ));

        User loadedById = repositoryAdapter.findById(saved.getUserId()).orElseThrow();
        User loadedByEmail = repositoryAdapter.findByEmail("new-user@example.com").orElseThrow();

        assertThat(loadedById.getUserId().value()).isEqualTo(saved.getUserId().value());
        assertThat(loadedById.getEmail().address()).isEqualTo("new-user@example.com");
        assertThat(loadedByEmail.getUsername()).isEqualTo("new-user");
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        assertThat(repositoryAdapter.findById(new UserId("missing-user"))).isEmpty();
        assertThat(repositoryAdapter.findByEmail("missing@example.com")).isEmpty();
    }
}
