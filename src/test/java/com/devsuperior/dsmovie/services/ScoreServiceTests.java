package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

	@Mock
	private ScoreRepository repository;

	@Mock
	private UserService userService;

	@Mock
	private MovieRepository movieRepository;

	private UserEntity userEntity;
	private MovieEntity movieEntity;
	private ScoreDTO scoreDTO;
	private Long existingId;
	private Long nonExistingId;

	@BeforeEach
	void setUp() throws Exception{
		userEntity = UserFactory.createUserEntity();
		movieEntity = MovieFactory.createMovieEntity();
		scoreDTO = ScoreFactory.createScoreDTO();
		existingId = 1L;
		nonExistingId = 2L;

		when(userService.authenticated()).thenReturn(userEntity);

		when(movieRepository.findById(existingId)).thenReturn(Optional.of(movieEntity));
		when(movieRepository.findById(nonExistingId)).thenReturn(Optional.empty());

		when(repository.saveAndFlush(any(ScoreEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		when(movieRepository.save(movieEntity)).thenReturn(movieEntity);
	}
	
	@Test
	public void saveScoreShouldReturnMovieDTO() {
		MovieDTO result = service.saveScore(scoreDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(scoreDTO.getMovieId(), result.getId());
		Assertions.assertEquals(movieEntity.getScore(), result.getScore());
		Assertions.assertEquals(movieEntity.getScores().size(), result.getCount());

		verify(repository).saveAndFlush(any(ScoreEntity.class));
		verify(movieRepository).save(movieEntity);
	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
		scoreDTO.setMovieId(nonExistingId);

		ResourceNotFoundException ex = Assertions.assertThrows(
				ResourceNotFoundException.class,
				() -> service.saveScore(scoreDTO)
		);

		Assertions.assertEquals("Recurso não encontrado", ex.getMessage());
	}
}
