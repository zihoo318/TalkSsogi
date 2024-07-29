import sys

def main():
    if len(sys.argv) < 3:
        print("Usage: python script.py <file_path> <headcount>")
        sys.exit(1)

    file_path = sys.argv[1]
    headcount = sys.argv[2]

    # 출력할 내용
    print("공경진!")
    print("강윤지,강지후,정가을,조유진")
    print(f"Headcount: {headcount}")

if __name__ == "__main__":
    main()
