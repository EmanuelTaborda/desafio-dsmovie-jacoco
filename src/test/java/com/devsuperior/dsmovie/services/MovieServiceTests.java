package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MovieServiceTests {

    @InjectMocks
    private MovieService service;

    @Mock
    private MovieRepository repository;

    private MovieEntity movieEntity;
    private MovieDTO movieDTO;
    private PageImpl<MovieEntity> page;
    private String movieTitle;
    private Long existingId, nonExistingId, dependentId;

    @BeforeEach
    void setUp() {
        movieEntity = MovieFactory.createMovieEntity();
        movieDTO = MovieFactory.createMovieDTO();
        movieTitle = movieEntity.getTitle();
        page = new PageImpl<>(List.of(movieEntity));
        existingId = 1L;
        nonExistingId = 2L;
		dependentId = 3L;

        when(repository.findById(existingId)).thenReturn(Optional.of(movieEntity));
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		when(repository.getReferenceById(existingId)).thenReturn(movieEntity);
		doThrow(EntityNotFoundException.class).when(repository).getReferenceById(nonExistingId);

		when(repository.save(any(MovieEntity.class))).thenReturn(movieEntity);

		when(repository.existsById(existingId)).thenReturn(true);
		when(repository.existsById(nonExistingId)).thenReturn(false);
		when(repository.existsById(dependentId)).thenReturn(true);

		doNothing().when(repository).deleteById(existingId);
		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    @Test
    public void findAllShouldReturnPagedMovieDTO() {
        when(repository.searchByTitle(any(), any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 12);

        Page<MovieDTO> result = service.findAll(movieTitle, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(movieTitle, result.iterator().next().getTitle());
    }


    @Test
    public void findByIdShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.findById(existingId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getTitle(), movieEntity.getTitle());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		ResourceNotFoundException ex = assertThrows(
				ResourceNotFoundException.class,
				() -> service.findById(nonExistingId)
		);

		assertEquals("Recurso não encontrado", ex.getMessage());
    }

    @Test
    public void insertShouldReturnMovieDTO() {

		MovieDTO result = service.insert(movieDTO);

		assertNotNull(result);
		assertEquals(result.getId(), movieEntity.getId());
		assertEquals(result.getTitle(), movieEntity.getTitle());
    }

    @Test
    public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.update(existingId, movieDTO);

		assertNotNull(result);
		assertEquals(result.getId(), movieEntity.getId());
		assertEquals(result.getTitle(), movieEntity.getTitle());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		ResourceNotFoundException ex = assertThrows(
				ResourceNotFoundException.class,
				() -> service.update(nonExistingId, movieDTO)
		);

		assertEquals("Recurso não encontrado", ex.getMessage());
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
		assertDoesNotThrow(() -> service.delete(existingId));

		verify(repository, times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		ResourceNotFoundException ex = assertThrows(
				ResourceNotFoundException.class,
				() -> service.delete(nonExistingId)
		);

		assertEquals("Recurso não encontrado", ex.getMessage());
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		DatabaseException ex = assertThrows(
				DatabaseException.class,
				() -> service.delete(dependentId)
		);

		assertEquals("Falha de integridade referencial", ex.getMessage());
    }
}
