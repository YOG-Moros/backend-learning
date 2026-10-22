package top.cch.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "student")
public class StudentProperties {

    private String name;

    private Integer age;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private List<String> hobbies;

    private Map<String, Integer> scores;

    private Address address;

    private List<Course> courses;

    @Data
    public static class Address {
        private String province;
        private String city;
    }

    @Data
    public static class Course {
        private String name;
        private Integer credit;
    }
}
