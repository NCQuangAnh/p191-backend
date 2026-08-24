package com.p191.telemetry.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByGoogleId(String googleId);

    // Danh sach tai khoan admin/head-admin (khong lay DRIVER) - man "Quản
    // lý" tren Cai dat He thong (yeu cau nguoi dung 24/08).
    @Query("select u from User u where u.role.name in ('ADMIN', 'SUPER_ADMIN') order by u.username")
    List<User> findAllAdmins();

    @Query("select count(u) from User u where u.role.name in ('ADMIN', 'SUPER_ADMIN') and u.lastSeenAt >= :since")
    long countAdminsOnlineSince(@Param("since") Instant since);

    @Query("select count(u) from User u where u.role.name in ('ADMIN', 'SUPER_ADMIN')")
    long countAllAdmins();

    @Query("select u from User u where u.role.name in ('ADMIN', 'SUPER_ADMIN') and u.lastSeenAt >= :since order by u.lastSeenAt desc")
    List<User> findAdminsOnlineSince(@Param("since") Instant since);
}