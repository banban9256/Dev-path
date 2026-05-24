package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Optional을 사용하기 위해 임포트 추가

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // 1. 중복 확인할 때 쓰는 메서드
    boolean existsByName(String name);

    // 2. [새로 추가] 로그인 성공 시 사용자 정보(학년, 전공 등)를 DB에서 쏙 꺼내오기 위한 메서드
    // 데이터가 없을 수도 있기 때문에 안전하게 Optional로 감싸서 리턴합니다.
    Optional<User> findByName(String name);
}