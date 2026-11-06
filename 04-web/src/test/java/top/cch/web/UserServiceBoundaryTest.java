package top.cch.web;

import org.junit.jupiter.api.Test;
import top.cch.web.common.BusinessException;
import top.cch.web.dto.UserUpdateRequest;
import top.cch.web.service.UserService;
import static org.assertj.core.api.Assertions.*;

class UserServiceBoundaryTest {
    @Test void invalidAndVeryLargePageNumbersAreHandled() {
        var service = new UserService();
        assertThatThrownBy(() -> service.page(0, 10)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.page(1, 101)).isInstanceOf(BusinessException.class);
        assertThat(service.page(Integer.MAX_VALUE, 100).getList()).isEmpty();
    }
    @Test void updateAgeIsOptionalForExistingClients() {
        var service = new UserService();
        var request = new UserUpdateRequest();
        request.setUsername("用户"); request.setEmail("user@example.com");
        int original = service.getById(1L).getAge();
        assertThat(service.update(1L, request).getAge()).isEqualTo(original);
        request.setAge(30);
        assertThat(service.update(1L, request).getAge()).isEqualTo(30);
    }
}
