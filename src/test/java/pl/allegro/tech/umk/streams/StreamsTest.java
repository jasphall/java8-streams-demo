package pl.allegro.tech.umk.streams;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class StreamsTest {

    private static final Logger logger = LoggerFactory.getLogger(StreamsTest.class);

    @Test
    public void should_create_stream_from_array() {
        // when
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5);

        // then
        assertThat(stream.collect(Collectors.toList()))
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void should_create_stream_from_list() {
        // given
        List<Integer> ints = Arrays.asList(1, 2, 3, 4, 5);

        // when
        Stream<Integer> stream = ints.stream();

        // then
        assertThat(stream.collect(Collectors.toList()))
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void should_create_stream_by_stream_builder() {
        // when
        Stream<Integer> stream = Stream.<Integer>builder()
                .add(1)
                .add(2)
                .build();

        // then
        assertThat(stream.collect(Collectors.toList()))
                .containsOnly(1, 2);
    }

    @Test
    public void should_map_elements_in_stream_sequentially() {
        // when
        Stream<Integer> stream = IntStream
                .range(0, 10)
                .boxed()
                .map(this::getUserAccountState);

//        // then
        assertThat(stream.collect(Collectors.toList()))
                .containsOnly(0, 2, 4, 6, 8, 10, 12, 14, 16, 18);
    }

    @Test
    public void should_map_elements_in_stream_in_parallel() {
        // when
        Stream<Integer> stream = IntStream
                .range(0, 10)
                .boxed()
                .map(this::getUserAccountState)
                .parallel();

        // then
        assertThat(stream.collect(Collectors.toList()))
                .containsOnly(0, 2, 4, 6, 8, 10, 12, 14, 16, 18);
    }

    @Test
    public void should_map_elements_in_stream_in_parallel_with_custom_thread_pool() throws ExecutionException, InterruptedException {
        // given
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);

        // when
        List<Integer> result = forkJoinPool.submit(() -> IntStream
                .range(0, 10)
                .boxed()
                .parallel()
                .map(this::getUserAccountState)
                .collect(Collectors.toList())).get();

        // then
        assertThat(result)
                .containsOnly(0, 2, 4, 6, 8, 10, 12, 14, 16, 18);
    }

    @Test
    public void should_raise_error_when_try_to_access_closed_stream() {
        // given
        Stream<Integer> stream = IntStream
                .range(0, 10)
                .boxed();

        // when
        List<Integer> result = stream.collect(Collectors.toList());

        // then
        Throwable error = catchThrowable(() -> stream.collect(Collectors.toList()));
        assertThat(error).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_group_by_first_letter_of_name() {
        // given
        List<String> names = List.of("Karolina", "Jan", "Kacper", "Michał");

        // when
        Map<Character, List<String>> result = names
                .stream()
                .collect(Collectors.groupingBy(name -> name.charAt(0)));

        // then
        assertThat(result.get('K')).containsOnly("Karolina", "Kacper");
        assertThat(result.get('J')).containsOnly("Jan");
        assertThat(result.get('M')).containsOnly("Michał");
    }

    @Test
    public void should_group_by_first_letter_and_then_map_into_name_length() {
        // given
        List<String> names = List.of("Karolina", "Jan", "Kacper", "Michał");

        // when
        Map<Character, List<Integer>> result = names
                .stream()
                .collect(
                        Collectors.groupingBy(
                                name -> name.charAt(0),
                                Collectors.mapping(String::length, Collectors.toList())
                        )
                );

        // then
        assertThat(result.get('K')).containsOnly(8, 6);
        assertThat(result.get('J')).containsOnly(3);
        assertThat(result.get('M')).containsOnly(6);
    }

    @Test
    public void should_reduce_from_initial_state_by_given_outcomes() {
        // given
        int startAccountState = 500;
        List<Integer> outcomes = List.of(100, 300, 100, 50);

        // when
        int currentAccountState = outcomes
                .stream()
                .reduce(startAccountState, (v1, v2) -> v1 - v2);

        // then
        assertThat(currentAccountState).isEqualTo(-50);
    }

    @Test
    public void should_generate_infinite_stream() {
        // expect
        Stream
                .generate(() -> Math.PI)
                .limit(1000)
                .forEach(System.out::println);
    }

    @Test
    public void should_generate_infinite_stream_by_iterating() {
        // expect
        Stream
                .iterate(1.0, i -> 2 * i)
                .map(i -> i * Math.PI)
                .limit(10)
                .forEach(System.out::println);
    }

    @Test
    public void test_flatmap(){

        IntStream
                .range(0, 10)
                .flatMap(a -> IntStream.range(0, a))
                .forEach(v -> logger.info("{}", v));

    }

    private Integer getUserAccountState(Integer value) {
        try {
            Thread.sleep(500);
            int result = value * 2;
            logger.info("Calculated value: {}", result);
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
