package net.skycomposer.moviebets.movie.controller;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.movie.AddCommentRequest;
import net.skycomposer.moviebets.common.dto.movie.CreateMovieRequest;
import net.skycomposer.moviebets.common.dto.movie.MovieDto;
import net.skycomposer.moviebets.common.dto.movie.UpdateMovieRequest;
import net.skycomposer.moviebets.movie.dao.entity.Movie;
import net.skycomposer.moviebets.movie.mapper.MovieDtoMapper;
import net.skycomposer.moviebets.movie.service.MovieService;

@RequiredArgsConstructor
@RestController
public class MovieController {

    private final MovieService movieService;
    private final MovieDtoMapper movieMapper;

    @GetMapping
    public List<MovieDto> getMovies() {
        return movieService.getMovies().stream().map(movieMapper::toMovieDto).toList();
    }

    @GetMapping("/{imdbId}")
    public MovieDto getMovie(@PathVariable String imdbId) {
        Movie movie = movieService.validateAndGetMovie(imdbId);
        return movieMapper.toMovieDto(movie);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MovieDto createMovie(@Valid @RequestBody CreateMovieRequest createMovieRequest) {
        Movie movie = Movie.from(createMovieRequest);
        movie = movieService.saveMovie(movie);
        return movieMapper.toMovieDto(movie);
    }

    @PutMapping("/{imdbId}")
    public MovieDto updateMovie(@PathVariable String imdbId, @Valid @RequestBody UpdateMovieRequest updateMovieRequest) {
        Movie movie = movieService.validateAndGetMovie(imdbId);
        Movie.updateFrom(updateMovieRequest, movie);
        movie = movieService.saveMovie(movie);
        return movieMapper.toMovieDto(movie);
    }

    @DeleteMapping("/{imdbId}")
    public MovieDto deleteMovie(@PathVariable String imdbId) {
        Movie movie = movieService.validateAndGetMovie(imdbId);
        movieService.deleteMovie(movie);
        return movieMapper.toMovieDto(movie);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{imdbId}/comments")
    public MovieDto addMovieComment(@PathVariable String imdbId,
                                    @Valid @RequestBody AddCommentRequest addCommentRequest,
                                    Principal principal) {
        Movie movie = movieService.validateAndGetMovie(imdbId);
        Movie.Comment comment = new Movie.Comment(principal.getName(), addCommentRequest.getText(), Instant.now());
        movie.getComments().addFirst(comment);
        movie = movieService.saveMovie(movie);
        return movieMapper.toMovieDto(movie);
    }
}