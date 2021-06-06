package nextstep.subway.station;

import static nextstep.subway.station.StationAcceptanceFixture.PATH;
import static nextstep.subway.station.StationAcceptanceFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.station.dto.StationResponse;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {

    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        final ExtractableResponse<Response> response = 지하철역_생성_요청("강남역");

        // then
        assertThat(statusCode(response)).isEqualTo(statusCode(CREATED));
        assertThat(response.header("Location")).isNotBlank();
    }

    private int statusCode(final ExtractableResponse<Response> response) {
        return response.statusCode();
    }

    private int statusCode(final HttpStatus created) {
        return created.value();
    }

    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
    @Test
    void createStationWithDuplicateName() {
        // given
        지하철역_생성_요청("강남역");

        // when
        final ExtractableResponse<Response> response = 지하철역_생성_요청("강남역");

        // then
        assertThat(statusCode(response)).isEqualTo(statusCode(BAD_REQUEST));
    }

    @DisplayName("지하철역을 조회한다.")
    @Test
    void getStations() {
        /// given
        final ExtractableResponse<Response> createResponse1 = 지하철역_생성_요청("강남역");
        final ExtractableResponse<Response> createResponse2 = 지하철역_생성_요청("역삼역");

        // when
        final ExtractableResponse<Response> response = 지하철역_조회_요청(PATH);

        // then
        assertThat(statusCode(response)).isEqualTo(statusCode(OK));
        assertThat(actual(response)).containsAll(expected(createResponse1, createResponse2));
    }

    private List<Long> actual(final ExtractableResponse<Response> response) {
        return response.jsonPath()
            .getList(".", StationResponse.class)
            .stream()
            .map(StationResponse::getId)
            .collect(Collectors.toList());
    }

    private List<Long> expected(final ExtractableResponse<Response> createResponse1,
        final ExtractableResponse<Response> createResponse2) {

        return Stream.of(createResponse1, createResponse2)
            .map(it -> Long.parseLong(id(it)))
            .collect(Collectors.toList());
    }

    private String id(final ExtractableResponse<Response> it) {
        return it.header("Location")
            .split("/")[2];
    }

    @DisplayName("지하철역을 제거한다.")
    @Test
    void deleteStation() {
        // given
        final ExtractableResponse<Response> createResponse = 지하철역_생성_요청("강남역");

        // when
        final String uri = createResponse.header("Location");
        final ExtractableResponse<Response> response = 지하철역_삭제_요청(uri);

        // then
        assertThat(statusCode(response)).isEqualTo(statusCode(NO_CONTENT));
    }
}
