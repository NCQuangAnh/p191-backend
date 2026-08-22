package com.p191.telemetry.metrics;

import jakarta.persistence.*;

@Entity
@Table(name = "trip_channel_stats")
public class TripChannelStat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "trip_id")
    private Trip trip;

    private String channel;
    private int totalIncomingMessages;
    private int suggestionUsedCount;
    private int composedCount;
    private int suggestionEmptyCount;

    protected TripChannelStat() {}
    public TripChannelStat(String channel, int total, int used, int composed, int empty) {
        this.channel = channel; this.totalIncomingMessages = total;
        this.suggestionUsedCount = used; this.composedCount = composed; this.suggestionEmptyCount = empty;
    }
    public void setTrip(Trip t) { this.trip = t; }
}