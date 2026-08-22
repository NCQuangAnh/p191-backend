package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {   // khoá = tripId (String)
    List<Trip> findTop50ByOrderByReceivedAtDesc();
}