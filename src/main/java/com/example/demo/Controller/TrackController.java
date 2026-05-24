package com.example.demo.Controller; // 재민님의 폴더명 대소문자에 맞춰서 자동 지정됩니다.

import com.example.demo.entity.Track;
import com.example.demo.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController // 1. 이 클래스가 사용자의 웹 요청을 처리하는 '문지기'라고 스프링 부트에게 알려줍니다.
public class TrackController {

    @Autowired // 2. 미리 만든 TrackService(두뇌)를 이 안에 자동으로 연결해 줍니다.
    private TrackService trackService;

    // 3. 사용자가 브라우저에 /tracks 라고 치고 들어오면 이 메소드가 실행됩니다!
    @GetMapping("/tracks")
    public List<Track> getTracks() {
        return trackService.getAllTracks(); // 서비스를 실행해서 DB 안의 모든 트랙 리스트를 반환합니다.
    }
}