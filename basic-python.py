import sys
import re
import json
import os
from collections import defaultdict
from basicAnalysis import ChatRoom

def main():
    if len(sys.argv) < 2:
        print("Usage: python main_script.py <file_path>")
        sys.exit(1)

    file_path = sys.argv[1]
    chat_room = ChatRoom(file_path)
    chat_room.preprocess_and_analyze()
    chat_room.save_daily_message_count()
    chat_room.save_daily_hourly_message_count()

    print(chat_room.room_name)
    print(','.join(chat_room.get_members()))
    print(chat_room.get_headcount())

    file_paths = [
        'group.txt',
        'group_daily_message_count.txt',
        'group_daily_hourly_message_count.txt'
    ]

    for user_name in chat_room.get_members():
        file_paths.append(f"{user_name}_personal.txt")
        file_paths.append(f"{user_name}_daily_message_count.txt")
        file_paths.append(f"{user_name}_daily_hourly_message_count.txt")

    # chat_room.upload_files_to_s3(file_paths, "bucket_name")

    # basic 랭킹을 저장할 딕셔너리
    ranking_results_map = {}

    def safe_read_file(file_path):
        try:
            with open(file_path, 'r', encoding='utf-8') as file:
                return file.read()
        except Exception as e:
            print(f"Error reading file {file_path}: {e}")
            return ""

    def add_sorted_ranking_to_map(ranking, category):
        sorted_ranking = dict(sorted(ranking.items(), key=lambda item: item[1], reverse=True))
        ranking_results_map[category] = {user_name: str(count) for user_name, count in sorted_ranking.items()}

    # ----------------메시지 수 기준 랭킹 출력----------------
    message_count_ranking = chat_room.rank_users_by_message_count()
    add_sorted_ranking_to_map({user_name: count for user_name, count in message_count_ranking}, '메시지')

    # ----------------오타 수 기준 랭킹 출력----------------
    typo_count_ranking = chat_room.rank_users_by_typo_count()
    add_sorted_ranking_to_map({user_name: count for user_name, count in typo_count_ranking}, '오타')

    # ----------------초성 사용 횟수 기준 랭킹 출력----------------
    initial_message_count_ranking = chat_room.rank_users_by_initial_message_count()
    add_sorted_ranking_to_map({user_name: count for user_name, count in initial_message_count_ranking}, '초성')

    # ----------------언급----------------
    def count_mentions(chat_room):
        # 멤버들 이름을 키로 하고, 값을 0으로 초기화한 딕셔너리 생성
        mention_counts = {user_name: 0 for user_name in chat_room.get_members()}
        mentioned_counts = {user_name: 0 for user_name in chat_room.get_members()}

        # 각 사용자의 개인 파일 경로 가져오기
        members = chat_room.get_members()
        for member in members:
            personal_file_path = chat_room.get_personal_file_path(member)

            # 개인 파일을 읽어서 언급한 횟수와 언급된 횟수 계산
            if personal_file_path:
                with open(personal_file_path, 'r', encoding='utf-8') as file:
                    messages = file.read().split('\n')
                    for message in messages:
                        # 멤버 이름들을 @와 함께 찾기 위한 정규 표현식
                        mentions = re.findall(r'@(' + '|'.join(re.escape(name) for name in members) + r')', message)
                        for mentioned_name in mentions:
                            mentioned_name = mentioned_name.strip()
                            if mentioned_name in mention_counts:
                                # 언급한 사람의 횟수 증가
                                mention_counts[member] += 1
                                # 언급된 사람의 횟수 증가
                                mentioned_counts[mentioned_name] += 1

        # 값에 따라 내림차순으로 정렬
        mention_counts = dict(sorted(mention_counts.items(), key=lambda item: item[1], reverse=True))
        mentioned_counts = dict(sorted(mentioned_counts.items(), key=lambda item: item[1], reverse=True))


        return mention_counts, mentioned_counts

    mention, mentioned = count_mentions(chat_room)
    add_sorted_ranking_to_map(mention, '언급한')
    add_sorted_ranking_to_map(mentioned, '언급된')

    # ----------------사진 보낸 개수----------------
    def count_photos(chat_room):
        photo_count = {user_name: 0 for user_name in chat_room.get_members()}

        # 각 사용자의 개인 파일 경로 가져오기
        members = chat_room.get_members()
        for member in members:
            personal_file_path = chat_room.get_personal_file_path(member)

            # 개인 파일을 읽어서 사진 개수 계산
            if personal_file_path:

                messages = safe_read_file(personal_file_path).split(';')
                for message in messages:
                    message = message.strip()
                    matches = re.findall(r'사진\s*(\d*)\s*장?', message)
                    for match in matches:
                        if re.fullmatch(r'사진\s*\d*\s*장?', message):
                            if match.isdigit():
                                photo_count[member] += int(match)
                            else:
                                photo_count[member] += 1

        return photo_count

    photo = count_photos(chat_room)
    add_sorted_ranking_to_map(photo, '사진')

    # ----------------이모티콘 보낸 개수----------------
    def count_emojis(chat_room):
        emoji_count = {user_name: 0 for user_name in chat_room.get_members()}

        for member in chat_room.get_members():
            personal_file_path = chat_room.get_personal_file_path(member)
            if personal_file_path:
                messages = safe_read_file(personal_file_path).split(';')
                for message in messages:
                    message = message.strip()
                    if message == '이모티콘':
                        emoji_count[member] += 1

        basic_emoji_counts = chat_room.rank_users_by_emoji_count()
        for name, count in basic_emoji_counts:
            if name in emoji_count:
                emoji_count[name] += count
        return emoji_count

    emoji = count_emojis(chat_room)
    add_sorted_ranking_to_map(emoji, '이모티콘')

    # ----------------메시지 삭제 횟수----------------
    def deleted_message_results(chat_room):
        delete_count = {user_name: 0 for user_name in chat_room.get_members()}

        for member in chat_room.get_members():
            personal_file_path = chat_room.get_personal_file_path(member)
            if personal_file_path:
                messages = safe_read_file(personal_file_path).split(';')
                for message in messages:
                    message = message.strip()
                    if '삭제된 메시지입니다.' in message:
                        delete_count[member] += 1
        return delete_count

    deleted_message_results = deleted_message_results(chat_room)
    add_sorted_ranking_to_map(deleted_message_results, '삭제')

    # ----------------메시지 평균 길이----------------
    def calculate_average_message_length(chat_room):
        user_average_lengths = {}
        for user_name in chat_room.get_members():
            personal_file_path = chat_room.get_personal_file_path(user_name)
            if os.path.exists(personal_file_path):
                content = safe_read_file(personal_file_path).split(';')

                total_length = 0
                num_messages = 0
                for message in content:
                    message = message.strip()
                    if message:
                        total_length += len(message)
                        num_messages += 1

                average_length = total_length / num_messages if num_messages > 0 else 0
                user_average_lengths[user_name] = int(average_length)

        return user_average_lengths

    average_length = calculate_average_message_length(chat_room)
    add_sorted_ranking_to_map(average_length, '평균 길이')

    # basic result 결과를 JSON으로 변환하여 파일로 저장
    basic_output_file = 'C:/Talkssogi_Workspace/TalkSsogi/ranking_results.json'
    with open(basic_output_file, 'w', encoding='utf-8') as f:
        json.dump(ranking_results_map, f, ensure_ascii=False, indent=4)

    print(f"\nbasic 랭킹 결과가 '{basic_output_file}' 파일로 저장되었습니다.")

if __name__ == "__main__":
    main()
