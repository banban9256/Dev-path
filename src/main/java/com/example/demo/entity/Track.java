package com.example.demo.entity; // 재민님의 폴더 경로

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tracks") // MySQL의 tracks 테이블과 연결
@Data
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id") // 만약 DB 컬럼명이 track_id라면 명시해 주는 것이 안전합니다.
    private Integer trackId;

    @Column(name = "track_name")
    private String trackName;

    private String description;

    // ⚠️ 여기에 혹시 userId나 user 관련 변수가 있다면 과감하게 지워주세요!
}