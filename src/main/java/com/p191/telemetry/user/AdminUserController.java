package com.p191.telemetry.user;

import com.p191.telemetry.audit.AuditAction;
import com.p191.telemetry.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/// Man "Quản lý" tren Cai dat He thong (yeu cau nguoi dung 24/08): dem/xem
/// admin dang online + tao/xoa tai khoan admin. "/api/admin/users/**" da
/// gioi han SUPER_ADMIN ("Head Admin") trong SecurityConfig san co - ADMIN
/// thuong chi thay so luong qua /api/admin/dashboard/stats/... (khac path,
/// khac SecurityConfig rule).
@RestController
public class AdminUserController {
    private static final long ONLINE_WINDOW_SECONDS = 5 * 60;

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public AdminUserController(UserRepository users, RoleRepository roles, PasswordEncoder encoder, AuditService audit) {
        this.users = users; this.roles = roles; this.encoder = encoder; this.audit = audit;
    }

    // ADMIN + SUPER_ADMIN deu xem duoc SO LUONG (path /api/admin/dashboard/**,
    // SecurityConfig cho phep hasAnyRole ADMIN/SUPER_ADMIN).
    @GetMapping("/api/admin/dashboard/stats/admins-online-count")
    public Map<String, Object> onlineCount() {
        Instant since = Instant.now().minusSeconds(ONLINE_WINDOW_SECONDS);
        long online = users.countAdminsOnlineSince(since);
        long total = users.countAllAdmins();
        return Map.of("count", online, "total", total, "offline", total - online, "windowMinutes", ONLINE_WINDOW_SECONDS / 60);
    }

    // Chi SUPER_ADMIN ("Head Admin") - danh sach TAT CA tai khoan admin
    // (dung cho ca "Xem chi tiết ai online" lan man "Tạo/Xóa tài khoản",
    // client tu loc theo truong "online"). Khong tra ve password.
    @GetMapping("/api/admin/users")
    public List<AdminUserView> listAdmins() {
        Instant since = Instant.now().minusSeconds(ONLINE_WINDOW_SECONDS);
        return users.findAllAdmins().stream()
                .map(u -> AdminUserView.from(u, u.getLastSeenAt() != null && u.getLastSeenAt().isAfter(since)))
                .toList();
    }

    // DA XAC NHAN bang curl truc tiep len backend that: BAT KY route MOI
    // nao (chua tung duoc goi truoc do trong lich su traffic) deu bi 403 tu
    // ha tang truoc Render (Cloudflare?) - BAT KE co mapping trong code hay
    // khong, bat ke method/role/path-shape. Ke ca 1 route hoan toan chua
    // dang ky (vd "/api/admin/foobar") cung bi 403 y het 1 route MOI dang
    // ky dung. Chi cac route CU da duoc goi tu lau (GET/POST
    // "/api/admin/users", GET "/api/admin/dashboard/**") moi luon qua duoc.
    // Vi vay KHONG THE tao route moi cho doi mat khau/xoa tai khoan - phai
    // dung lai chinh route POST "/api/admin/users" da co san, phan biet
    // hanh dong qua field "action" trong body (yeu cau nguoi dung 26/08,
    // sau nhieu lan thu route moi deu that bai giong nhau).
    @PostMapping("/api/admin/users")
    public Object handleUsersPost(@RequestBody Map<String, Object> body, Authentication auth, HttpServletRequest http) {
        String action = (String) body.getOrDefault("action", "create");
        return switch (action) {
            case "change-password" -> { changePassword(body, auth, http); yield Map.of("ok", true); }
            case "delete" -> { deleteAdmin(body, auth, http); yield Map.of("ok", true); }
            default -> createAdmin(body, auth, http);
        };
    }

    private AdminUserView createAdmin(Map<String, Object> body, Authentication auth, HttpServletRequest http) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        boolean headAdmin = Boolean.TRUE.equals(body.get("headAdmin"));
        if (username == null || username.isBlank() || password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập không được trống, mật khẩu tối thiểu 6 ký tự");
        }
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
        }
        if (headAdmin && users.findAllAdmins().stream().anyMatch(u -> u.getRole().getName() == RoleName.SUPER_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được phép có 1 tài khoản Head Admin");
        }
        RoleName roleName = headAdmin ? RoleName.SUPER_ADMIN : RoleName.ADMIN;
        Role role = roles.findByName(roleName).orElseThrow();
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        users.save(u);
        audit.record(auth.getName(), AuditAction.ADMIN_CREATED, u.getUsername(), "role=" + roleName, clientIp(http));
        return AdminUserView.from(u, false);
    }

    // Chi SUPER_ADMIN - doi mat khau cho bat ky tai khoan nao (bao gom ca
    // chinh Head Admin dang dang nhap - yeu cau nguoi dung 26/08).
    private void changePassword(Map<String, Object> body, Authentication auth, HttpServletRequest http) {
        requireHeadAdmin(auth);
        Long id = idFrom(body);
        String password = (String) body.get("password");
        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu tối thiểu 6 ký tự");
        }
        User target = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        target.setPassword(encoder.encode(password));
        users.save(target);
        audit.record(auth.getName(), AuditAction.ADMIN_PASSWORD_CHANGED, target.getUsername(), null, clientIp(http));
    }

    private void deleteAdmin(Map<String, Object> body, Authentication auth, HttpServletRequest http) {
        requireHeadAdmin(auth);
        Long id = idFrom(body);
        User target = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        if (target.getUsername().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tự xóa tài khoản đang đăng nhập");
        }
        if (target.getRole().getName() == RoleName.SUPER_ADMIN) {
            long headAdminCount = users.findAllAdmins().stream().filter(u -> u.getRole().getName() == RoleName.SUPER_ADMIN).count();
            if (headAdminCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa Head Admin cuối cùng");
            }
        }
        users.deleteById(id);
        audit.record(auth.getName(), AuditAction.ADMIN_DELETED, target.getUsername(), null, clientIp(http));
    }

    private static Long idFrom(Map<String, Object> body) {
        Object raw = body.get("id");
        if (raw instanceof Number n) return n.longValue();
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu id tài khoản");
    }

    private static void requireHeadAdmin(Authentication auth) {
        boolean isHeadAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!isHeadAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ Head Admin được phép thực hiện thao tác này");
        }
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    public record AdminUserView(Long id, String username, String role, boolean online, Instant lastSeenAt, Instant createdAt) {
        static AdminUserView from(User u, boolean online) {
            return new AdminUserView(u.getId(), u.getUsername(), u.getRole().getName().name(), online, u.getLastSeenAt(), u.getCreatedAt());
        }
    }

}
