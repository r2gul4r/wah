# 메가박스 박스오피스 JSON 생성
import json
import os

import requests


BASE = os.path.dirname(os.path.dirname(__file__))
PATH = os.path.join(BASE, "data", "movies.json")

url = "https://www.megabox.co.kr/on/oh/oha/Movie/selectMovieList.do"

# 1. 브라우저 정보를 담은 헤더 추가
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Referer": "https://www.megabox.co.kr/movie",  # 어디서 왔는지 알려줌
}

# 2. post 요청 시 headers 인자 추가
res = requests.post(
    url,
    json={"orderType": "boxoffice", "pageNo": 1},
    headers=headers,  # 이 부분이 핵심!
    timeout=15,
)
res.raise_for_status()

data = res.json()

movies = []
for i, m in enumerate(data.get("movieList", []), start=1):  # movieList가 없으면 빈 리스트 반환
    movies.append(
        {
            # m["rnum"] 대신 m.get("rnum", 0)을 써서 키가 없어도 에러 안 나게 함
            "rank": int(m.get("rnum", 0) or m.get("rank", 0) or m.get("boxoRank", 0) or m.get("rowNum", 0) or i),
            "title": m.get("movieNm", "제목 없음"),
            "openDate": m.get("openDt", "") or m.get("rfilmDe", ""),
            "rating": m.get("watchGradeNm", "") or m.get("admisClassNm", ""),
        }
    )

os.makedirs(os.path.dirname(PATH), exist_ok=True)

with open(PATH, "w", encoding="utf-8") as f:
    json.dump(movies, f, ensure_ascii=False, indent=2)

print("완료:", PATH)
