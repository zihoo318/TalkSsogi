package com.talkssogi.TalkSsogi_server.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "chattingroom")
public class ChattingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cr_num")
    private Integer crNum;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "headcount")
    private int headcount;

    @ManyToOne
    @JoinColumn(name = "user_id") // 외래 키 컬럼 이름과 매핑
    private User user;

    @OneToOne(mappedBy = "chattingRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AnalysisResult analysisResult;

    // Getters and setters
    public Integer getCrNum() { return crNum;  }

    public void setCrNum(Integer crNum) { this.crNum = crNum;  }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getHeadcount() { return headcount; }

    public void setHeadcount(int headcount) {
        this.headcount = headcount;
    }

    public AnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(AnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
    }

}