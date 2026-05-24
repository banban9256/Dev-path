package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    private String name;        // 사용자 이름 [cite: 7]
    private String password;    // 비밀번호 [cite: 14]
    private String birthdate;   // 생년월일 [cite: 19]
    private String major;       // 전공 [cite: 20]
    private String grade;       // 학년 (1/2/3/4/휴학생 등) [cite: 21, 22]

    @Column(name = "tracks_track_id")
    private Integer desiredTrackId; // 오타 수정 완료! (desiredTrackId) [cite: 23]

    private String level;       // 현재 본인 숙련도 (상/중/하) [cite: 24, 25, 26]
}