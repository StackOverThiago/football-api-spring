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

    // Standings shapes
    public record TeamInfo(String name, String crest) {}
    public record Row(int position, TeamInfo team, int points, int playedGames, int won, int goalDifference) {}
    public record Standing(List<Row> table) {}
    public record StandingsResponse(List<Standing> standings) {}

    // Scorers shapes
    public record PlayerInfo(String name) {}
    public record Scorer(PlayerInfo player, TeamInfo team, Integer goals, Integer assists) {}
    public record ScorersResponse(List<Scorer> scorers) {}

    // Fixtures shapes
    public record Match(String utcDate, TeamInfo homeTeam, TeamInfo awayTeam) {}
    public record MatchesResponse(List<Match> matches) {}

    // Helper: one place that talks to football-data.org
    private <T> T fetch(String path, Class<T> type) {
        return client.get()
                .uri("https://api.football-data.org/v4/competitions/" + path)
                .header("X-Auth-Token", apiKey)
                .retrieve()
                .body(type);
    }

    @GetMapping(value = "/api/standings/{league}", produces = "application/json")
    public String rawStandings(@PathVariable String league) {
        return fetch(league + "/standings", String.class);
    }

    @GetMapping("/api/standings/{league}/top")
    public List<Row> topStandings(@PathVariable String league,
                                  @RequestParam(defaultValue = "4") int limit) {
        StandingsResponse res = fetch(league + "/standings", StandingsResponse.class);
        return res.standings().get(0).table().stream().limit(limit).toList();
    }

    @GetMapping("/api/scorers/{league}/top")
    public List<Scorer> topScorers(@PathVariable String league,
                                   @RequestParam(defaultValue = "4") int limit) {
        ScorersResponse res = fetch(league + "/scorers?limit=" + limit, ScorersResponse.class);
        return res.scorers().stream().limit(limit).toList();
    }

    @GetMapping("/api/fixtures/{league}")
    public List<Match> fixtures(@PathVariable String league,
                                @RequestParam(defaultValue = "4") int limit) {
        MatchesResponse res = fetch(league + "/matches?status=SCHEDULED", MatchesResponse.class);
        return res.matches().stream().limit(limit).toList();
    }
}