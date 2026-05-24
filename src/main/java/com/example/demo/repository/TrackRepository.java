package com.example.demo.repository; // 패키지 경로가 다르면 빨간 줄이 뜰 수 있으니 주의하세요!

import com.example.demo.entity.Track; // Track 엔티티를 가져옵니다.
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {
    // 여기에 추가 코드를 적지 않아도 DB 저장/조회 기능을 사용할 수 있습니다!
}