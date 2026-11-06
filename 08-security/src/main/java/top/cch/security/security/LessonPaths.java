package top.cch.security.security;

import jakarta.servlet.http.HttpServletRequest;

/** 旧任务系统占用 /api/tasks，课程认证必须避开这一已有命名空间。 */
final class LessonPaths {
    private LessonPaths() { }
    static boolean isLessonApi(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/api/") && !path.equals("/api/tasks") && !path.startsWith("/api/tasks/");
    }
}
