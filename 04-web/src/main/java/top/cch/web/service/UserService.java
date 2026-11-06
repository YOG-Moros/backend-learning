package top.cch.web.service;

import org.springframework.stereotype.Service;
import top.cch.web.common.BusinessException;
import top.cch.web.common.ErrorCode;
import top.cch.web.common.PageResult;
import top.cch.web.dto.UserCreateRequest;
import top.cch.web.dto.UserUpdateRequest;
import top.cch.web.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public UserService() {
        seed("张三", 18, "zhangsan@example.com", "江苏南京");
        seed("李四", 19, "lisi@example.com", "江苏苏州");
        seed("王五", 20, "wangwu@example.com", "浙江杭州");
        seed("赵六", 21, "zhaoliu@example.com", "上海");
        seed("孙七", 22, "sunqi@example.com", "安徽合肥");
        seed("周八", 23, "zhouba@example.com", "山东济南");
    }

    private void seed(String username, int age, String email, String address) {
        User user = new User();
        user.setId(idGenerator.incrementAndGet());
        user.setUsername(username);
        user.setAge(age);
        user.setEmail(email);
        user.setAddress(address);
        user.setBirthday(LocalDate.of(2005, (age % 12) + 1, 1));
        store.put(user.getId(), user);
    }

    public PageResult<User> page(int pageNum, int pageSize) {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<User> all = new ArrayList<>(store.values());
        all.sort(Comparator.comparing(User::getId));
        long offset = (long) (pageNum - 1) * pageSize;
        int from = (int) Math.min(offset, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<User> list = from >= all.size() ? List.of() : all.subList(from, to);
        return PageResult.of(list, all.size(), pageNum, pageSize);
    }

    public User getById(Long id) {
        User user = store.get(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return user;
    }

    public User create(UserCreateRequest request) {
        User user = new User();
        user.setId(idGenerator.incrementAndGet());
        user.setUsername(request.getUsername());
        user.setAge(request.getAge());
        user.setEmail(request.getEmail());
        user.setAddress("江苏南京");
        user.setBirthday(LocalDate.now());
        store.put(user.getId(), user);
        return user;
    }

    public User update(Long id, UserUpdateRequest request) {
        User user = getById(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getAge() != null) user.setAge(request.getAge());
        return user;
    }

    public void delete(Long id) {
        if (store.remove(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }
}
