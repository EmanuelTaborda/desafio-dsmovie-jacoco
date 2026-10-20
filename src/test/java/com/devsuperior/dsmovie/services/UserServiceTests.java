package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTests {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CustomUserUtil userUtil;

    private UserEntity userEntity;
    private String username, existingUsername, invalidUsername;
	private List<UserDetailsProjection> userDetailsProjections;

    @BeforeEach
    void setUp() throws Exception {
        userEntity = UserFactory.createUserEntity();
        username = userEntity.getUsername();
        invalidUsername = "invalid User";
		existingUsername = "Emanuel";

		userDetailsProjections = UserDetailsFactory.createCustomAdminClientUser(existingUsername);

        when(userUtil.getLoggedUsername()).thenReturn(username);

        when(repository.findByUsername(username)).thenReturn(Optional.ofNullable(userEntity));
        doThrow(RuntimeException.class).when(repository).findByUsername(invalidUsername);

		when(repository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userDetailsProjections);
		when(repository.searchUserAndRolesByUsername(invalidUsername)).thenReturn(new ArrayList<>());



    }

    @Test
    public void authenticatedShouldReturnUserEntityWhenUserExists() {

        UserEntity result = service.authenticated();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), userEntity.getId());
        Assertions.assertEquals(result.getUsername(), userEntity.getUsername());
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		when(userUtil.getLoggedUsername()).thenReturn(invalidUsername);

		UsernameNotFoundException ex = Assertions.assertThrows(
				UsernameNotFoundException.class,
				() -> service.authenticated()
		);

		Assertions.assertEquals("Invalid user", ex.getMessage());
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {

		UserDetails result = service.loadUserByUsername(existingUsername);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(existingUsername, result.getUsername());
		Assertions.assertEquals("123", result.getPassword());
		Assertions.assertEquals(2, result.getAuthorities().size());
	}

    @Test
    public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

		UsernameNotFoundException ex = Assertions.assertThrows(
				UsernameNotFoundException.class,
				() -> service.loadUserByUsername(invalidUsername)
		);

		Assertions.assertEquals("Email not found", ex.getMessage());
    }
}
