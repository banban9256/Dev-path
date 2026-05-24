package com.example.demo.Service; // 재민님의 실제 패키지 경로에 맞게 지정됩니다.

import com.example.demo.entity.Track;
import com.example.demo.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service // 1. 스프링 부트한테 "네가 관리할 핵심 비즈니스 로직 클래스야"라고 알려줍니다.
public class TrackService {

    @Autowired // 2. 스프링 부트가 미리 만들어둔 TrackRepository(창고 관리인)를 자동으로 이 안에 주입(연결)해 줍니다.
    private TrackRepository trackRepository;

    // 3. 웹페이지에 전체 트랙 리스트('인공지능', '데이터 사이언스' 등)를 보여주기 위한 기능입니다.
    public List<Track> getAllTracks() {
        return trackRepository.findAll(); // 창고 관리인에게 테이블의 모든 데이터를 가져오라고 시킵니다.
    }
}