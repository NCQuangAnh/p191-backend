package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips", indexes = @Index(name = "ix_trip_device", columnList = "device_id"))
public class Trip {
    @Id @Column(name = "trip_id", length = 64)
    private String tripId;

    @Column(name = "device_id", nullable = false, length = 64) private String deviceId;
    private String driverId;
    private String appVersion;
    private Instant startedAt;
    private Instant endedAt;
    private Integer durationMinutes;                         // §8.2

    private int totalIncomingMessages;                       // §2
    private int declinedListenCount;
    private int repliedCount;
    private int suggestionUsedCount;
    private int composedCount;
    private int suggestionEmptyCount;

    private int summarySuccessCount;                         // §8.6
    private int summaryFallbackCount;
    private int summaryGuardrailBlockedCount;

    private int clarifyRetryCount;                           // §8.10
    private Integer avgProcessingMs;                         // §8.5

    private Boolean countsConsistent;                        // cờ cross-check §2

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripChannelStat> channels = new ArrayList<>();  // §8.4

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    protected Trip() {}
    public Trip(String tripId) { this.tripId = tripId; }

    public List<TripChannelStat> getChannels() { return channels; }
    public void addChannel(TripChannelStat c) { c.setTrip(this); channels.add(c); }

    // --- getters (dùng cho TripReadService) ---
    public String getTripId() { return tripId; }
    public String getDeviceId() { return deviceId; }
    public String getDriverId() { return driverId; }
    public String getAppVersion() { return appVersion; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public int getTotalIncomingMessages() { return totalIncomingMessages; }
    public int getDeclinedListenCount() { return declinedListenCount; }
    public int getRepliedCount() { return repliedCount; }
    public int getSuggestionUsedCount() { return suggestionUsedCount; }
    public int getComposedCount() { return composedCount; }
    public int getSuggestionEmptyCount() { return suggestionEmptyCount; }
    public int getSummarySuccessCount() { return summarySuccessCount; }
    public int getSummaryFallbackCount() { return summaryFallbackCount; }
    public int getSummaryGuardrailBlockedCount() { return summaryGuardrailBlockedCount; }
    public int getClarifyRetryCount() { return clarifyRetryCount; }
    public Integer getAvgProcessingMs() { return avgProcessingMs; }
    public Boolean getCountsConsistent() { return countsConsistent; }
    public Instant getReceivedAt() { return receivedAt; }

    // --- setters (dùng cho TripService) ---
    public void setDeviceId(String v){deviceId=v;} public void setDriverId(String v){driverId=v;}
    public void setAppVersion(String v){appVersion=v;} public void setStartedAt(Instant v){startedAt=v;}
    public void setEndedAt(Instant v){endedAt=v;} public void setDurationMinutes(Integer v){durationMinutes=v;}
    public void setTotalIncomingMessages(int v){totalIncomingMessages=v;} public void setDeclinedListenCount(int v){declinedListenCount=v;}
    public void setRepliedCount(int v){repliedCount=v;} public void setSuggestionUsedCount(int v){suggestionUsedCount=v;}
    public void setComposedCount(int v){composedCount=v;} public void setSuggestionEmptyCount(int v){suggestionEmptyCount=v;}
    public void setSummarySuccessCount(int v){summarySuccessCount=v;} public void setSummaryFallbackCount(int v){summaryFallbackCount=v;}
    public void setSummaryGuardrailBlockedCount(int v){summaryGuardrailBlockedCount=v;}
    public void setClarifyRetryCount(int v){clarifyRetryCount=v;} public void setAvgProcessingMs(Integer v){avgProcessingMs=v;}
    public void setCountsConsistent(Boolean v){countsConsistent=v;} public void setReceivedAt(Instant v){receivedAt=v;}
}