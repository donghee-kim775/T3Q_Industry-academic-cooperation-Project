- 현재 Project에서 API Refactoring 을 위해 rpy2 라이브러리를 도입하고자함.
- 또한 해당 프로젝트에서 api는 실행환경에 따라 달라지는 이 실행환경은 사내 server에서 k8s에서 실행 환경에 배포됨.
- 이에 따른 docker file과 requirements.txt 파일을 올리고자 함.
- 이는 다른 이들도 사용 가능하다고 판별하기에 docker file과 requirements.txt를 올림
- checkpoint 형식으로 build 하여서 4번째 환경을 build하면 됨

해당 파일을 build 하는 순서

```
<<<<<<< HEAD
cd /Deploy_RPY2_5
=======
git clone https://github.com/donghee-kim775/T3Q_Industry-academic-cooperation-Project

cd T3Q_Industry-academic-cooperation-Project/Deploy_RPY2_5
>>>>>>> 9ff4d409cf37bea7481ac552d9518291a7f43aac

### Image build
docker build -t rpy2_app_5 .

### docker run
docker run -d -p 5000:5000 rpy2_app_2
```

- (POST) "http://localhost:5000/api7"
- Request : 기존과 같음
