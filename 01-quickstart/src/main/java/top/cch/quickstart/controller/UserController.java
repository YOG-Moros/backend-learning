package top.cch.quickstart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.quickstart.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/user")
public class UserController {

    private final List<User> users = new CopyOnWriteArrayList<>(List.of(
            new User(1001L, "张三", LocalDate.of(2005, 10, 24)),
            new User(1002L, "李四", LocalDate.of(2004, 3, 8)),
            new User(1003L, "王五", LocalDate.of(2006, 11, 30))
    ));

    private final AtomicLong idGenerator = new AtomicLong(1003L);

    @GetMapping("/hello")
    public String getHello() {
        return "hello world";
    }

    @GetMapping("/info")
    public User getUserInfo() {
        return new User(1001L, "张三", LocalDate.of(2005, 10, 24));
    }

    @GetMapping("/list")
    public List<User> list(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return users;
        }
        return users.stream().filter(u -> u.getName().contains(keyword)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public User create(@RequestBody User user) {
        user.setId(idGenerator.incrementAndGet());
        users.add(user);
        return user;
    }
}
