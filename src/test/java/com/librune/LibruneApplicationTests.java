package com.librune;

import com.librune.entity.Book;
import com.librune.entity.Role;
import com.librune.entity.User;
import com.librune.repository.BookRepository;
import com.librune.repository.RoleRepository;
import com.librune.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LibruneApplicationTests {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextAndSeedDataAreConsistentWithSchema() {
        assertThat(roleRepository.findByName(Role.ADMIN)).isPresent();
        assertThat(roleRepository.findByName(Role.READER)).isPresent();

        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getRole().getName()).isEqualTo(Role.ADMIN);
        assertThat(passwordEncoder.matches("123456", admin.getPassword())).isTrue();

        User reader = userRepository.findByUsername("reader1").orElseThrow();
        assertThat(reader.getRole().getName()).isEqualTo(Role.READER);
        assertThat(passwordEncoder.matches("123456", reader.getPassword())).isTrue();

        List<Book> featured = bookRepository.findByFeaturedTrueOrderByCreatedAtDesc();
        assertThat(featured).hasSize(2);
    }
}
