import re
from konlpy.tag import Okt
from collections import defaultdict
import unicodedata

class User:
    def __init__(self, name):
        self.name = name
        self.typo_count = 0  # 오타 횟수
        self.initial_message_count = 0  # 초성 사용 횟수
        self.message_count = 0  # 메시지 보낸 횟수
        self.emoji_count = 0  # 이모티콘 횟수(카톡 임티는 포함X)
        self.personal_file_path = f'C:/Users/Master/TalkSsogi_Workspace/{self.name}_personal.txt'  # 개인 파일 경로 초기화

class ChatRoom:
    def __init__(self, file_path):
        self.file_path = file_path
        self.room_name, self.headcount = self.extract_room_name()  # room_name과 headcount 초기화
        self.members = {}  # 사용자 이름을 키로 하고 User 객체를 값으로 하는 딕셔너리
        self.user_chats = defaultdict(str)
        self.total_chats = []
        self.okt = Okt()  # Okt 객체 생성
        self.group_file_path = 'C:/Users/Master/TalkSsogi_Workspace/group.txt'
        self.excluded_jamo_set = {'ㅋ', 'ㅍ', 'ㅎ', 'ㅌ', 'ㅠ', 'ㅜ', '큐', '쿠', '튜', '투', '퓨', '푸', '튵', '큨', '캬', '컄', '헝', '엉', '어'}  # 제외할 자모음 집합
        self.additional_special_symbols = set(';.,:/?!')  # 제거할 특수 기호 목록

    def extract_room_name(self):
        # 파일의 첫 번째 줄에서 채팅방 이름과 인원 수를 추출
        with open(self.file_path, 'r', encoding='utf-8') as file:
            first_line = file.readline().strip()
            
            # 숫자가 있는 경우와 없는 경우를 구분하기 위한 정규 표현식
            match = re.match(r'(.+?) (\d*) 카카오톡 대화', first_line)

            if match:
                room_name = match.group(1).strip()
                number = match.group(2).strip()
            
                # 숫자가 없으면 기본값 2를 사용
                if not number:
                    number = '2'
                
                return room_name, number
            else:
                # 숫자가 없는 경우 " 님과"를 기준으로 채팅방 이름 추출 (=갠톡인 경우에 해당)
                match = re.match(r'(.+?) 님과 카카오톡 대화', first_line)
                if match:
                    room_name = match.group(1).strip()
                    number = '2'  # 기본값 2 설정
                    return room_name, number
                else:
                    raise ValueError("채팅방 이름을 추출할 수 없습니다.")

    def preprocess_and_analyze(self):
        # 텍스트 파일 읽기
        with open(self.file_path, 'r', encoding='utf-8') as file:
            lines = file.readlines()

        # 날짜와 채팅 내용 추출을 위한 패턴
        date_pattern = re.compile(r'\d{4}년 \d{1,2}월 \d{1,2}일 \w+ \d{1,2}:\d{2}')
        message_pattern = re.compile(r'(\d{4}년 \d{1,2}월 \d{1,2}일 \w+ \d{1,2}:\d{2}),?\s*(.+?) :\s*(.+)')

        for line in lines:
            message = None  # message 변수를 초기화

            # 채팅 내용을 날짜와 사용자 이름, 메시지로 분리
            match = message_pattern.search(line)
            if match:
                date_info, user_name, message = match.groups()
                user_name = user_name.strip()
                message = message.strip()

                # 사용자 객체 생성 또는 가져오기
                if user_name not in self.members:
                    self.members[user_name] = User(user_name)

                user = self.members[user_name]

                # 사용자별 메시지 추가
                self.user_chats[user_name] += message + "\n"

                # 전체 메시지 리스트에 추가 (메시지만 추가)
                self.total_chats.append(message + "\n")

                # 오타 처리
                tokens = message.split()  # 공백 기준으로 나누기
                for token in tokens:
                    if self.contains_alphabet(token):
                        # 영어는 판정 제외
                        self.user_chats[user_name] += token + ' '
                        self.total_chats.append(token + ' ')
                        continue

                    if not self.is_jamo_only(token):  # 특수기호, 이모티콘 제거
                        for char in token:
                            if self.is_emoji(char):
                                user.emoji_count += 1
                        token = self.remove_emojis(token)
                        token = self.remove_special_symbols(token)

                    # 1차 판정
                    check = self.okt.morphs(token)
                    if len(check) > 5:
                        user.typo_count += 1
                        continue  # 오타확정

                    # 2차 판정
                    result = self.classify_jamo_vowel(token)
                    if result == 0:  # 자모음 둘다
                        i = 0
                        for char in check:
                            if not self.is_excluded_jamo(char):
                                i += 1
                        if i >= 3:
                            user.typo_count += 1
                            continue  # 오타확정
                        else:
                            self.user_chats[user_name] += token + ' '
                            self.total_chats.append(token + ' ')
                    elif result == 1:  # 자음만
                        if not self.is_excluded_jamo(token):
                            user.initial_message_count += 1
                            self.user_chats[user_name] += token + ' '
                            self.total_chats.append(token + ' ')
                    elif result == 2:  # 모음만
                        if not self.is_excluded_jamo(token):
                            user.typo_count += 1
                            continue  # 오타확정
                    else:
                        self.user_chats[user_name] += token + ' '
                        self.total_chats.append(token + ' ')

                # 메시지 보낸 횟수 증가
                user.message_count += 1

        # 개인별 메시지 저장 (오타 거른 상태로 저장)
        for user_name, chat in self.user_chats.items():
            user = self.members[user_name]
            with open(user.personal_file_path, 'w', encoding='utf-8') as file:
                file.write(chat)

        # 전체 메시지 저장 (오타 거른 상태로 저장)
        chat_string = ''.join(self.total_chats)
        with open(self.group_file_path, 'w', encoding='utf-8') as file:
            file.write(chat_string)

        # 오타 카운트 결과 출력
        #print("오타 카운트:")
        #for user_name, user in self.members.items():
            #print(f"{user_name}: {user.typo_count}개")

    def is_special_symbol(self, character):
        """입력된 문자가 특수 기호인지 여부를 반환하는 함수"""
        category = unicodedata.category(character)
        if (category.startswith('S') and not category.startswith('So')) or character in self.additional_special_symbols:
            return True
        else:
            return False

    def remove_special_symbols(self, word):
        """특수 기호를 제거한 문자열을 반환하는 함수"""
        result = []
        for char in word:
            if not self.is_special_symbol(char):
                result.append(char)
        return ''.join(result)

    def is_emoji(self, character):
        """입력된 문자가 이모티콘인지 여부를 반환하는 함수"""
        category = unicodedata.category(character)
        if category.startswith('So'):
            return True
        else:
            return False

    def remove_emojis(self, word):
        """이모티콘을 제거한 문자열을 반환하는 함수"""
        result = []
        for char in word:
            if not self.is_emoji(char):
                result.append(char)
        return ''.join(result)

    def classify_jamo_vowel(self, word):
        jamo_pattern = re.compile("[ㄱ-ㅎ]+")  # 자음 패턴
        vowel_pattern = re.compile("[ㅏ-ㅣ]+")  # 모음 패턴

        has_jamo = bool(jamo_pattern.search(word))
        has_vowel = bool(vowel_pattern.search(word))

        if has_jamo and has_vowel:
            return 0  # 자음과 모음이 모두 있는 경우
        elif has_jamo:
            return 1  # 자음만 있는 경우
        elif has_vowel:
            return 2  # 모음만 있는 경우
        else:
            return -1  # 영어인 경우

    def is_jamo_only(self, word):
        # 자음 또는 모음으로만 있는지 체크
        jamo_pattern = re.compile("[ㄱ-ㅎㅏ-ㅣ]+")
        return bool(jamo_pattern.fullmatch(word))

    def is_excluded_jamo(self, word):
        # 제외할 자음 또는 모음으로만 구성된 단어인지 체크(ㅋ큐ㅠ)
        return all(char in self.excluded_jamo_set for char in word)

    def contains_alphabet(self, text):
        """문자열에 영어 알파벳 문자가 포함되어 있는지 여부를 반환하는 함수"""
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
        """사용자의 이모티콘 개수에 따라 사용자들을 정렬하여 반환"""
        emoji_counts = [(user.name, user.emoji_count) for user in self.members.values()]
        emoji_counts.sort(key=lambda x: x[1], reverse=True)
        return emoji_counts
