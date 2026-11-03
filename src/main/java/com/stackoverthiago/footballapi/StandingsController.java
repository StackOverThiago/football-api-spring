package com.stackoverthiago.footballapi;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class StandingsController {

    private final RestClient client = RestClient.create();

    @Value("${football.api.key}")
    private String apiKey;

    public record TeamInfo(String name, String crest) {}
    public record Row(int position, TeamInfo team, int points, int playedGames, int won, int goalDifference) {}
    public record Standing(List<Row> table) {}
    public record ApiResponse(List<Standing> standings) {}

    // Full raw JSON
    @GetMapping(value = "/api/standings/{league}", produces = "application/json")
    public String standings(@PathVariable String league) {
        return client.get()
                .uri("https://api.football-data.org/v4/competitions/" + league + "/standings")
                .header("X-Auth-Token", apiKey)
                .retrieve()
                .body(String.class);
    }

    // Trimmed top N teams
    @GetMapping("/api/standings/{league}/top")
    public List<Row> top(@PathVariable String league,
                         @RequestParam(defaultValue = "4") int limit) {
        ApiResponse res = client.get()
                .uri("https://api.football-data.org/v4/competitions/" + league + "/standings")
                .header("X-Auth-Token", apiKey)
                .retrieve()
                .body(ApiResponse.class);

        return res.standings().get(0).table().stream().limit(limit).toList();
    }
}