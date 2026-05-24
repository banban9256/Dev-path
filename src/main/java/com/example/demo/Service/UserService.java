package com.example.demo.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. 회원가입 기능 [cite: 13]
    public User registerUser(User user) {
        // 실제 프로젝트에서는 비밀번호를 암호화해야 하지만, 지금은 흐름 테스트를 위해 그대로 저장합니다. [cite: 14]
        return userRepository.save(user);
    }

    // 2. 로그인 기능 [cite: 9]
    public String loginUser(String name, String password) {
        // DB에서 이름으로 유저를 찾습니다 [cite: 7]
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> u.getName().equals(name))
                .findFirst();

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) { // 비밀번호 일치 확인 [cite: 14]
                return "로그인 성공! " + user.getName() + "님의 길을 응원합니다."; // [cite: 37]
            }
        }
        return "로그인 실패: 아이디 또는 비밀번호를 확인하세요.";
    }
}