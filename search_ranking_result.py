import sys
import re
import json
import os
from collections import defaultdict
from basicAnalysis import ChatRoom

# 터미널 출력 인코딩 설정
sys.stdout.reconfigure(encoding='utf-8')

def main():
    if len(sys.argv) < 3:
        print("Usage: python search_ranking.py <file_path> <keyword>")
        sys.exit(1)

    file_path = sys.argv[1]
    keyword = sys.argv[2]
    chat_room = ChatRoom(file_path)
    chat_room.preprocess_and_analyze()

    # 검색어 랭킹을 저장할 딕셔너리
    search_results_map = {}

    def safe_read_file(file_path):
        try:
            with open(file_path, 'r', encoding='utf-8') as file:
                return file.read()
        except Exception as e:
            print(f"Error reading file {file_path}: {e}")
            return ""

    # ----------------검색어 받아서 검색하는 함수 만들기----------------
    def search_keyword_in_personal_files(chat_room, keyword):
        members = chat_room.get_members()
        search_results = defaultdict(int)

        for user_name in members:
            personal_file_path = chat_room.get_personal_file_path(user_name)
            if not os.path.exists(personal_file_path):
                continue

            messages = safe_read_file(personal_file_path).split(';')
            for message in messages:
                if keyword.strip() in message:
                    search_results[user_name] += 1

        sorted_results = dict(sorted(search_results.items(), key=lambda item: item[1], reverse=True))
        return sorted_results

    results = search_keyword_in_personal_files(chat_room, keyword)
    search_results_map['검색어'] = {user_name: str(count) for user_name, count in results.items()}

    # 결과를 JSON으로 변환하여 출력
    print(json.dumps(search_results_map, ensure_ascii=False, indent=4))

    # search result 결과를 JSON으로 변환하여 파일로 저장
    search_output_file = 'C:/Talkssogi_Workspace/TalkSsogi/search_ranking_results.json'
    with open(search_output_file, 'w', encoding='utf-8') as f:
        json.dump(search_results_map, f, ensure_ascii=False, indent=4)

    print(f"\nsearch 랭킹 결과가 '{search_output_file}' 파일로 저장되었습니다.")

if __name__ == "__main__":
    main()
