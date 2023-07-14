package ru.practicum.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.HitDTO;
import ru.practicum.dto.ViewStatDTO;

import java.util.List;
import java.util.Map;

@Service
public class StatClient extends BaseClient {
    //private static final String API_PREFIX = "/hit";

    @Autowired
    public StatClient(@Value("${statistic-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addStatistic(HitDTO hitDto) {
        return post("/hit", hitDto);
    }

    public List<ViewStatDTO> getStatistic(String start, String end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", String.join(",", uris),
                "unique", unique
        );
        ResponseEntity<Object> response = get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
        ObjectMapper objectMapper = new ObjectMapper();
        List<ViewStatDTO> viewStatDTO = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
        });
        return viewStatDTO;
    }
}