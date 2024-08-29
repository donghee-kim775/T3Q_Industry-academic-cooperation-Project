FROM ubuntu:20.04

# 기본 패키지 업데이트 및 설치
RUN apt update && apt install -y \
    python3-pip \
    python3-venv \
    gcc \
    libpq-dev

# 작업 디렉토리 설정
WORKDIR /app

# 가상 환경 생성
RUN python3 -m venv venv

RUN venv/bin/pip install --upgrade pip setuptools

# requirements.txt 파일을 컨테이너에 복사
COPY requirements.txt .

# requirements.txt에 명시된 패키지 설치
RUN venv/bin/pip install -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 가상 환경을 기본 PATH에 추가
ENV PATH="/app/venv/bin:$PATH"

# Flask 애플리케이션 실행
CMD ["flask", "run", "--host=0.0.0.0"]