package com.example.demo.Controller;

import com.example.demo.entity.User;
import com.example.demo.Service.UserService;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate; // DB 기록을 직접 제어하기 위한 인터페이스

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * 1. 웹사이트 맨 처음 대문 페이지
     */
    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    /**
     * 2. 회원가입 페이지 요청
     */
    @GetMapping("/user/signup")
    public String signupForm() {
        return "signup";
    }

    /**
     * 3. 회원가입 처리 로직
     */
    @PostMapping("/user/signup")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/user/signup?success";
    }

    /**
     * 4. 중복확인 API
     */
    @GetMapping("/user/check-duplicate")
    @ResponseBody
    public boolean checkDuplicate(@RequestParam("name") String name) {
        return userRepository.existsByName(name);
    }

    /**
     * 5. 로그인 화면 요청
     */
    @GetMapping("/user/login")
    public String loginForm() {
        return "login";
    }

    /**
     * 6. 로그인 처리 및 과거 로드맵 기록 DB 동기화 불러오기 (NumberFormatException 완벽 방어)
     */
    @PostMapping("/user/login")
    public String processLogin(@RequestParam("name") String name,
                               @RequestParam("password") String password,
                               HttpServletRequest request) {

        String result = userService.loginUser(name, password);

        if (result != null && result.contains("성공")) {
            HttpSession session = request.getSession();
            User loggedInUser = userRepository.findByName(name).orElse(null);
            session.setAttribute("loginUser", loggedInUser);

            if (loggedInUser != null) {
                // 로그인 시 과거에 DB에 저장해둔 이 유저의 체크 박스 기록을 싹 긁어옵니다.
                String sql = "SELECT step_number, is_completed FROM user_roadmap WHERE user_id = ?";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, loggedInUser.getUserId());

                // 처음에는 전부 미완료(false)로 초기화 해둡니다 (신규 가입 유저는 자연스럽게 0% 처리)
                session.setAttribute("step1", false);
                session.setAttribute("step2", false);
                session.setAttribute("step3", false);
                session.setAttribute("step4", false);

                int completedCount = 0;
                for (Map<String, Object> row : rows) {
                    int stepNum = (int) row.get("step_number");

                    // [완벽 방어 연산식] DB에 1, "1", true, "true" 그 어떤 형태로 박혀있든 유연하게 캐치합니다.
                    Object isCompletedObj = row.get("is_completed");
                    boolean isDone = false;

                    if (isCompletedObj != null) {
                        String val = isCompletedObj.toString().trim().toLowerCase();
                        isDone = "1".equals(val) || "true".equals(val);
                    }

                    if (isDone) {
                        session.setAttribute("step" + stepNum, true);
                        completedCount++;
                    }
                }

                // 과거 진행률 복구 연산 후 세션에 저장
                int progress = (int) (((double) completedCount / 4.0) * 100);
                session.setAttribute("roadmapProgress", progress);
            }

            return "redirect:/user/roadmap";
        } else {
            return "redirect:/user/login?error";
        }
    }

    /**
     * 7. 로그인한 사용자 고유의 개인 로드맵 화면 보여주기
     */
    @GetMapping("/user/roadmap")
    public String userRoadmap(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");

        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", user);
        return "roadmap";
    }

    /**
     * 8. 체크박스 토글 시 실시간 게이지 변동 처리 API (순수 int 리턴으로 HTML 스크립트 결합 완료)
     */
    @PostMapping("/user/roadmap/toggle")
    @ResponseBody
    public int toggleRoadmapStep(@RequestParam("step") int step,
                                 @RequestParam("checked") boolean checked,
                                 HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");

        if (user == null) return 0;

        // [Upsert] 이미 기록이 있으면 수정(UPDATE)하고 없으면 새로 저장(INSERT)합니다.
        String saveSql = "INSERT INTO user_roadmap (user_id, step_number, is_completed) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE is_completed = ?";
        jdbcTemplate.update(saveSql, user.getUserId(), step, checked ? 1 : 0, checked ? 1 : 0);

        // 세션 메모리 실시간 동기화
        session.setAttribute("step" + step, checked);

        // 다시 전체 체크 개수를 해당 유저의 DB 기준으로 카운트합니다.
        String countSql = "SELECT COUNT(*) FROM user_roadmap WHERE user_id = ? AND is_completed = 1";
        Integer checkedCount = jdbcTemplate.queryForObject(countSql, Integer.class, user.getUserId());
        if (checkedCount == null) checkedCount = 0;

        int currentProgress = (int) (((double) checkedCount / 4.0) * 100);
        session.setAttribute("roadmapProgress", currentProgress);

        // 프론트 자바스크립트의 .json() 파싱 규격에 맞춰 순수 정수 퍼센트만 반환합니다.
        return currentProgress;
    }

    /**
     * 9. 다음 수준 과정으로 레벨업 전이 기능 처리 API
     */
    @PostMapping("/user/roadmap/levelup")
    @ResponseBody
    public String processLevelUp(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");

        if (user == null) return "fail";

        String currentLevel = user.getLevel();
        String nextLevel = currentLevel;

        if ("하".equals(currentLevel)) nextLevel = "중";
        else if ("중".equals(currentLevel)) nextLevel = "상";

        // 1. 진짜 DB 유저 테이블의 level 값을 다음 단계로 변경 업데이트
        String updateLevelSql = "UPDATE user SET level = ? WHERE user_id = ?";
        jdbcTemplate.update(updateLevelSql, nextLevel, user.getUserId());

        // 2. 등급이 변했으므로 새로운 숙련도의 단계를 새롭게 체크할 수 있게 기존 체크 리스트 완파 초기화
        String deleteLogsSql = "DELETE FROM user_roadmap WHERE user_id = ?";
        jdbcTemplate.update(deleteLogsSql, user.getUserId());

        // 3. 자바 세션 메모리 완전 초기화 및 새 레벨 반영
        user.setLevel(nextLevel);
        session.setAttribute("loginUser", user);
        session.setAttribute("roadmapProgress", 0);
        session.setAttribute("step1", false);
        session.setAttribute("step2", false);
        session.setAttribute("step3", false);
        session.setAttribute("step4", false);

        return "success";
    }
}