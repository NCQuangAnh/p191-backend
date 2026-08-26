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

    @PostMapping("/api/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserView createAdmin(@RequestBody CreateAdminRequest req, Authentication auth, HttpServletRequest http) {
        if (req.username() == null || req.username().isBlank() || req.password() == null || req.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập không được trống, mật khẩu tối thiểu 6 ký tự");
        }
        if (users.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
        }
        if (req.headAdmin() && users.findAllAdmins().stream().anyMatch(u -> u.getRole().getName() == RoleName.SUPER_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được phép có 1 tài khoản Head Admin");
        }
        RoleName roleName = req.headAdmin() ? RoleName.SUPER_ADMIN : RoleName.ADMIN;
        Role role = roles.findByName(roleName).orElseThrow();
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(role);
        users.save(u);
        audit.record(auth.getName(), AuditAction.ADMIN_CREATED, u.getUsername(), "role=" + roleName, clientIp(http));
        return AdminUserView.from(u, false);
    }

    // Chi SUPER_ADMIN - doi mat khau cho bat ky tai khoan nao (bao gom ca
    // chinh Head Admin dang dang nhap - yeu cau nguoi dung 26/08). Dung
    // POST thay vi PUT: da xac nhan bang curl truc tiep len backend that
    // (voi JWT SUPER_ADMIN hop le) - PUT/DELETE/PATCH luon bi 403 tu ha
    // tang truoc Render (Cloudflare?) du role dung 100%, trong khi GET/POST
    // qua binh thuong - khong phai loi code/role/CSRF, chi doi method la het.
    @PostMapping("/api/admin/users/{id}/password")
    public void changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest req, Authentication auth, HttpServletRequest http) {
        if (req.password() == null || req.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu tối thiểu 6 ký tự");
        }
        User target = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        target.setPassword(encoder.encode(req.password()));
        users.save(target);
        audit.record(auth.getName(), AuditAction.ADMIN_PASSWORD_CHANGED, target.getUsername(), null, clientIp(http));
    }

    @DeleteMapping("/api/admin/users/{id}")
    public void deleteAdmin(@PathVariable Long id, Authentication auth, HttpServletRequest http) {
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

    public record CreateAdminRequest(String username, String password, boolean headAdmin) {}
    public record ChangePasswordRequest(String password) {}
}
