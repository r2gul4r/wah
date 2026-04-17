package com.example.demo.repository;

import com.example.demo.dto.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovieRepository {

    private final JdbcTemplate jdbc;

    // 테이블 생성 (rank 예약어 백틱 처리 및 TiDB 최적화)
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS movie (" +
                "id BIGINT PRIMARY KEY AUTO_RANDOM, " +
                "`rank` INT NOT NULL, " +
                "title VARCHAR(255), " +
                "open_date DATE, " +
                "rating VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_rank (`rank`)" +
                ")";
        jdbc.execute(sql);
    }

    // 전체 삭제
    public void deleteAll() {
        jdbc.update("DELETE FROM movie");
    }

    // 저장 (날짜 파싱 에러 방지 로직 추가)
    public void saveAll(List<Movie> list) {
        // INSERT 문에서도 rank 컬럼에 백틱을 사용하는 것이 안전합니다.
        String sql = "INSERT INTO movie(`rank`, title, open_date, rating) VALUES (?, ?, ?, ?)";

        // 날짜 형식이 2024.04.16 처럼 점(.)일 경우를 대비한 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        for (Movie m : list) {
            String rawDate = m.getOpenDate();
            LocalDate parsedDate = null;

            // 데이터가 있고 빈 문자열이 아닐 때만 파싱 시도
            if (rawDate != null && !rawDate.trim().isEmpty()) {
                try {
                    // 먼저 하이픈(-) 형식 시도, 실패하면 점(.) 형식 시도
                    if (rawDate.contains(".")) {
                        parsedDate = LocalDate.parse(rawDate, formatter);
                    } else {
                        parsedDate = LocalDate.parse(rawDate);
                    }
                } catch (Exception e) {
                    // 파싱 실패 시 로그를 남기거나 null 처리 (시스템 중단 방지)
                    System.err.println("날짜 파싱 실패: " + rawDate + " - " + e.getMessage());
                }
            }

            jdbc.update(sql,
                    m.getRank(),
                    m.getTitle(),
                    parsedDate,
                    m.getRating()
            );
        }
    }

    // 조회 (ORDER BY에도 백틱 추가)
    public List<Movie> findAll() {
        return jdbc.query("SELECT * FROM movie ORDER BY `rank` ASC",
                (rs, i) -> {
                    Movie m = new Movie();
                    m.setRank(rs.getInt("rank"));
                    m.setTitle(rs.getString("title"));
                    m.setOpenDate(rs.getString("open_date"));
                    m.setRating(rs.getString("rating"));
                    return m;
                });
    }
}
