import re
from konlpy.tag import Okt
from collections import defaultdict
import unicodedata
from datetime import datetime, timedelta
import boto3
import os

class User:
    def __init__(self, name, index):
        self.name = name
        self.daily_message_count = defaultdict(int) # 딕셔너리로 일별 메시지 수 기록
        self.daily_hourly_message_count = defaultdict(lambda: defaultdict(int)) # 일별로 시간대별 메시지 수 기록
        self.message_count = 0
        self.typo_count = 0
        self.initial_message_count = 0
        self.emoji_count = 0
        self.personal_file_path = f"{index}_personal.txt" # 인덱스 숫자로 파일명 저장

class ChatRoom:
    def __init__(self, file_path):
        self.file_path = file_path
        self.room_name, self.headcount = self.extract_room_name() # room_name과 headcount 초기화
        self.group_file_path = f"group.txt"
        self.members = {}
        self.user_list = [] # 사용자 이름을 저장할 리스트 (파일 이름을 한글 대신 리스트 인덱스로 매핑하기 위)
        self.user_chats = defaultdict(str)
        self.total_chats = []
        self.daily_group_message_count = defaultdict(int) # 단체의 일별 메시지 수 딕셔너리
        self.daily_hourly_group_message_count = defaultdict(lambda: defaultdict(int)) # 단체의 일별 시간대별 메시지 수 딕셔너리
        self.excluded_jamo_set = {'ㅋ', 'ㅍ', 'ㅎ', 'ㅌ', 'ㅠ', 'ㅜ', '큐', '쿠', '튜', '투', '퓨', '푸', '튵', '큨', '캬', '컄', '헝', '엉', '어'} # 제외할 자모음 집합
        self.additional_special_symbols = set(';.,:/?!') # 제거할 특수 기호 목록
        self.okt = Okt() # Okt 객체 생성

    def extract_room_name(self):
        with open(self.file_path, 'r', encoding='utf-8') as file:
            first_line = file.readline().strip()
            match = re.match(r'(.+?) (\d*) 카카오톡 대화', first_line)
            if match:
                room_name = match.group(1).strip()
                number = match.group(2).strip()
                if not number:
                    number = '2'
                return room_name, number
            else:
                match = re.match(r'(.+?) 님과 카카오톡 대화', first_line)
                if match:
                    room_name = match.group(1).strip()
                    number = '2'
                    return room_name, number
                else:
                    raise ValueError("채팅방 이름을 추출할 수 없습니다.")

    def preprocess_and_analyze(self):
        with open(self.file_path, 'r', encoding='utf-8') as file:
            lines = file.readlines()

        message_pattern = re.compile(r'(\d{4}년 \d{1,2}월 \d{1,2}일 \w+ \d{1,2}:\d{2}),\s*(.+?) :\s*(.+)')
        date_pattern = re.compile(r'\d{4}년 \d{1,2}월 \d{1,2}일 \w+ \d{1,2}:\d{2}')

        last_user_name = None

        for line in lines:
            line = line.strip()
            if not line:
                continue

            match = message_pattern.search(line)
            if match:
                date_info, user_name, message = match.groups()
                user_name = user_name.strip()
                message = message.strip()

                try:
                    date_info = date_info.replace('오후', 'PM').replace('오전', 'AM')
                    date_obj = datetime.strptime(date_info, '%Y년 %m월 %d일 %p %I:%M')
                except ValueError:
                    print(f"날짜 포맷 오류: {date_info}")
                    continue

                date_str = date_obj.strftime('%Y-%m-%d')
                hour_str = date_obj.strftime('%H')

                if user_name not in self.members:
                    index = len(self.user_list)
                    self.user_list.append(user_name)
                    self.members[user_name] = User(user_name, index)

                user = self.members[user_name]

                self.user_chats[user_name] += message + "\n"
                self.total_chats.append(message + "\n")

                user.daily_message_count[date_str] += 1
                self.daily_group_message_count[date_str] += 1

                user.daily_hourly_message_count[date_str][hour_str] += 1
                self.daily_hourly_group_message_count[date_str][hour_str] += 1

                user.message_count += 1

                tokens = message.split()
                for token in tokens:
                    if self.contains_alphabet(token):
                        self.user_chats[user_name] += token + '\n'
                        self.total_chats.append(token + '\n')
                        continue

                    if not self.is_jamo_only(token):
                        for char in token:
                            if self.is_emoji(char):
                                user.emoji_count += 1
                        token = self.remove_emojis(token)
                        token = self.remove_special_symbols(token)

                    check = self.okt.morphs(token)
                    if len(check) > 5:
                        user.typo_count += 1
                        continue

                    result = self.classify_jamo_vowel(token)
                    if result == 0:
                        i = 0
                        for char in check:
                            if not self.is_excluded_jamo(char):
                                i += 1
                        if i >= 3:
                            user.typo_count += 1
                            continue
                        else:
                            self.user_chats[user_name] += token + '\n'
                            self.total_chats.append(token + '\n')
                    elif result == 1:
                        if not self.is_excluded_jamo(token):
                            user.initial_message_count += 1
                            self.user_chats[user_name] += token + '\n'
                            self.total_chats.append(token + '\n')
                    elif result == 2:
                        if not self.is_excluded_jamo(token):
                            user.typo_count += 1
                            continue
                    else:
                        self.user_chats[user_name] += token + '\n'
                        self.total_chats.append(token + '\n')
                last_user_name = user_name

            else:
                if not date_pattern.match(line) and line:
                    if last_user_name:
                        self.user_chats[last_user_name] += line + "\n"
                        self.total_chats.append(line + "\n")

        for user_name, chat in self.user_chats.items():
            user = self.members[user_name]
            with open(user.personal_file_path, 'w', encoding='utf-8') as file:
                file.write(chat)

        chat_string = ''.join(self.total_chats)
        with open(self.group_file_path, 'w', encoding='utf-8') as file:
            file.write(chat_string)

        self.fill_missing_dates_and_hours()

    def fill_missing_dates_and_hours(self):
        min_date = min(self.daily_group_message_count.keys())
        max_date = max(self.daily_group_message_count.keys())
        #print(f"날짜 범위: {min_date} ~ {max_date}")

        min_date_obj = datetime.strptime(min_date, '%Y-%m-%d')
        max_date_obj = datetime.strptime(max_date, '%Y-%m-%d')

        current_date = min_date_obj
        while current_date <= max_date_obj:
            date_str = current_date.strftime('%Y-%m-%d')

            if date_str not in self.daily_group_message_count:
                self.daily_group_message_count[date_str] = 0

            if date_str not in self.daily_hourly_group_message_count:
                self.daily_hourly_group_message_count[date_str] = {f"{hour:02d}": 0 for hour in range(24)}

            for hour in range(24):
                hour_str = f"{hour:02d}"
                if hour_str not in self.daily_hourly_group_message_count[date_str]:
                    self.daily_hourly_group_message_count[date_str][hour_str] = 0

                for user in self.members.values():
                    if date_str not in user.daily_message_count:
                        user.daily_message_count[date_str] = 0
                    if hour_str not in user.daily_hourly_message_count[date_str]:
                        user.daily_hourly_message_count[date_str][hour_str] = 0

            current_date += timedelta(days=1)

    def is_special_symbol(self, character):
        category = unicodedata.category(character)
        if (category.startswith('S') and not category.startswith('So')) or character in self.additional_special_symbols:
            return True
        else:
            return False

    def remove_special_symbols(self, word):
        result = []
        for char in word:
            if not self.is_special_symbol(char):
                result.append(char)
        return ''.join(result)

    def is_emoji(self, character):
        category = unicodedata.category(character)
        if category.startswith('So'):
            return True
        else:
            return False

    def remove_emojis(self, word):
        result = []
        for char in word:
            if not self.is_emoji(char):
                result.append(char)
        return ''.join(result)

    def classify_jamo_vowel(self, word):
        jamo_pattern = re.compile("[ㄱ-ㅎ]+")
        vowel_pattern = re.compile("[ㅏ-ㅣ]+")

        has_jamo = bool(jamo_pattern.search(word))
        has_vowel = bool(vowel_pattern.search(word))

        if has_jamo and has_vowel:
            return 0
        elif has_jamo:
            return 1
        elif has_vowel:
            return 2
        else:
            return -1

    def is_jamo_only(self, word):
        jamo_pattern = re.compile("[ㄱ-ㅎㅏ-ㅣ]+")
        return bool(jamo_pattern.fullmatch(word))

    def is_excluded_jamo(self, word):
        return all(char in self.excluded_jamo_set for char in word)

    def contains_alphabet(self, text):
        for char in text:
            if 'a' <= char <= 'z' or 'A' <= char <= 'Z':
                return True
        return False

    def get_headcount(self):
        return self.headcount

    def get_members(self):
        return list(self.members.keys())

    def get_user_info(self, user_name):
        if user_name in self.members:
            user = self.members[user_name]
            return f"이름: {user.name}, 오타 개수: {user.typo_count}, 초성 사용 횟수: {user.initial_message_count}, 메시지 수: {user.message_count}"
        else:
            return f"{user_name} 사용자 정보 없음"

    def get_personal_file_path(self, user_name):
        if user_name in self.members:
            return self.members[user_name].personal_file_path
        else:
            return None

    def get_group_file_path(self):
        return self.group_file_path

    def rank_users_by_message_count(self):
        message_counts = [(user.name, user.message_count) for user in self.members.values()]
        message_counts.sort(key=lambda x: x[1], reverse=True)
        return message_counts

    def rank_users_by_typo_count(self):
        typo_counts = [(user.name, user.typo_count) for user in self.members.values()]
        typo_counts.sort(key=lambda x: x[1], reverse=True)
        return typo_counts

    def rank_users_by_initial_message_count(self):
        initial_message_counts = [(user.name, user.initial_message_count) for user in self.members.values()]
        initial_message_counts.sort(key=lambda x: x[1], reverse=True)
        return initial_message_counts

    def rank_users_by_emoji_count(self):
        emoji_counts = [(user.name, user.emoji_count) for user in self.members.values()]
        emoji_counts.sort(key=lambda x: x[1], reverse=True)
        return emoji_counts

    def save_daily_message_count(self):
        for user_name, user in self.members.items():
            user_index = self.get_index_by_username(user_name)
            file_path = f"{user_index}_daily_message_count.txt"
            with open(file_path, 'w', encoding='utf-8') as file:
                for date, count in sorted(user.daily_message_count.items()):
                    file.write(f"{date}:{count}\n")
        
        with open("group_daily_message_count.txt", 'w', encoding='utf-8') as file:
            for date, count in sorted(self.daily_group_message_count.items()):
                file.write(f"{date}:{count}\n")

    def save_daily_hourly_message_count(self):
        def get_3hour_slot(hour):
            return (hour // 3) * 3

        for user_name, user in self.members.items():
            user_index = self.get_index_by_username(user_name)
            file_path = f"{user_index}_daily_hourly_message_count.txt"
            with open(file_path, 'w', encoding='utf-8') as file:
                for date, hours in sorted(user.daily_hourly_message_count.items()):
                    hourly_counts = defaultdict(int)
                    for hour, count in hours.items():
                        slot = get_3hour_slot(int(hour))
                        hourly_counts[slot] += count
                
                    hours_str = ",".join(f"{slot:02d}-{slot+2:02d}:{count}" for slot, count in sorted(hourly_counts.items()))
                    file.write(f"{date}({hours_str})\n")

        with open("group_daily_hourly_message_count.txt", 'w', encoding='utf-8') as file:
            for date, hours in sorted(self.daily_hourly_group_message_count.items()):
                hourly_counts = defaultdict(int)
                for hour, count in hours.items():
                    slot = get_3hour_slot(int(hour))
                    hourly_counts[slot] += count
                
                hours_str = ",".join(f"{slot:02d}-{slot+2:02d}:{count}" for slot, count in sorted(hourly_counts.items()))
                file.write(f"{date}({hours_str})\n")

    def get_index_by_username(self, user_name):
        try:
            return self.user_list.index(user_name)
        except ValueError:  
            return None

    def get_username_by_index(self, index):
        try:
            return self.user_list[index]
        except IndexError:
            return None