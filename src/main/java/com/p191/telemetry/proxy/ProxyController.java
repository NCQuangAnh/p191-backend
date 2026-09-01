package com.p191.telemetry.proxy;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Reverse-proxy cho Google Places (New) + Google Cloud TTS. Truoc day app
 * Flutter goi thang Google voi key bundle trong APK (lib/config/secrets.dart)
 * - lo key khi decompile. App gio goi vao day, backend giu key that (bien
 * moi truong GOOGLE_PLACES_API_KEY/GOOGLE_TTS_API_KEY tren Render), forward
 * NGUYEN VEN body/response - khong doi logic parse phia Flutter
 * (nearby_station_service.dart / google_tts_service.dart).
 */
@RestController
@RequestMapping("/api/proxy")
@Tag(name = "Proxy")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.google.places-api-key:}")
    private String placesApiKey;

    @Value("${app.google.tts-api-key:}")
    private String ttsApiKey;

    /** Forward -> https://places.googleapis.com/v1/places:searchNearby */
    @PostMapping(value = "/places/searchNearby", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchNearby(@RequestBody String body) {
        return forwardPlaces("https://places.googleapis.com/v1/places:searchNearby", body);
    }

    /** Forward -> https://places.googleapis.com/v1/places:searchText */
    @PostMapping(value = "/places/searchText", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchText(@RequestBody String body) {
        return forwardPlaces("https://places.googleapis.com/v1/places:searchText", body);
    }

    /** Forward -> https://texttospeech.googleapis.com/v1/text:synthesize */
    @PostMapping(value = "/tts/synthesize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> ttsSynthesize(@RequestBody String body) {
        if (ttsApiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("{\"error\":\"GOOGLE_TTS_API_KEY chua duoc cau hinh tren server\"}");
        }
        try {
            var req = HttpRequest.newBuilder()
                    .uri(URI.create("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + ttsApiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(res.statusCode()).contentType(MediaType.APPLICATION_JSON).body(res.body());
        } catch (Exception e) {
            logger.error("Loi forward request toi Google TTS: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private ResponseEntity<String> forwardPlaces(String url, String body) {
        if (placesApiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("{\"error\":\"GOOGLE_PLACES_API_KEY chua duoc cau hinh tren server\"}");
        }
        try {
            var req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", placesApiKey)
                    .header("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.addressComponents,places.location")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(res.statusCode()).contentType(MediaType.APPLICATION_JSON).body(res.body());
        } catch (Exception e) {
            logger.error("Loi forward request toi Google Places: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
