import sys
import os
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
from basicAnalysis import ChatRoom

def main():
    if len(sys.argv) < 3:
        print("Usage: python caller_prediction.py <file_path> <keyword>")
        sys.exit(1)

    file_path = sys.argv[1]
    keyword = sys.argv[2]
    chat_room = ChatRoom(file_path)
    chat_room.preprocess_and_analyze()

    # 사용자별 텍스트 파일 경로
    members = chat_room.get_members()
    base_dir = os.path.dirname(file_path)
    member_file_paths = {member: chat_room.get_personal_file_path(member) for member in members}

    # 데이터를 저장할 리스트
    data = []

    def safe_read_file(file_path):
        try:
            with open(file_path, 'r', encoding='utf-8') as file:
                return file.read()
        except Exception as e:
            print(f"Error reading file {file_path}: {e}")
            return ""

    # 각 사용자별 메시지를 읽어서 데이터 리스트에 추가
    for member, path in member_file_paths.items():
        text = safe_read_file(path)
        for line in text.split('\n'):
            data.append((line, member))

    # 데이터프레임 생성
    df = pd.DataFrame(data, columns=['text', 'label'])

    # 피처와 레이블 분리
    X = df['text']
    y = df['label']

    # 텍스트 데이터를 벡터화
    vectorizer = CountVectorizer()
    X_vectorized = vectorizer.fit_transform(X)

    # 훈련 데이터와 테스트 데이터로 분리
    X_train, X_test, y_train, y_test = train_test_split(X_vectorized, y, test_size=0.2, random_state=42)

    # 모델 훈련
    model = RandomForestClassifier()
    model.fit(X_train, y_train)

    # 예측 및 평가
    y_pred = model.predict(X_test)
    accuracy = accuracy_score(y_test, y_pred)
    # accuracy 확인이 필요할 경우 밑에 코드 출력
    # print(f"Model accuracy: {accuracy}")

    # 키워드에 대한 발신자 예측
    keyword_vectorized = vectorizer.transform([keyword])
    caller_prediction = model.predict(keyword_vectorized)
    print(caller_prediction[0])

if __name__ == "__main__":
    main()
