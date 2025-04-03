![header](https://capsule-render.vercel.app/api?type=waving&color=0&text=TeamHijacking)
# Team Hijacking  <br />

<div align="center">
  <h3>스마트 워치를 이용한, 시각장애인 일상생활 보조 애플리케이션</h3>
</div>


## 🔫 팀 소개 🔫
  Hijacking이라는 말과 같이, 사람들을 깜짝 놀라게 할 아이디어와 작품을 내고자 노력하는 팀입니다. 



## 개요 
  시각 장애로 인하여 확인하기 어려운 정보들을 웨어러블 기기를 통해 제공하는 통합 지원형 애플리케이션
    본 애플리케이션은 네 가지 기능을 제공합니다::
  1. 버스, 혹은 지하철을 탄 뒤 '타고 있는 역, 혹은 정거장'으로부터 몇 정거장을 건너야만 바라는 도착지에 도착할 수 있는지를 소리와 진동으로 알려주는 **교통 안내 기능**
  2. 횡단보도 유도 기능과 횡단보도 안내 기능을 스마트 워치로도 이용할 수 있도록 지원하는 시각 장애인용 음성 안내기 **리모컨 기능**
  3. 집의 기기, 조명, AI 스피커와 연동하여, 필요시 음성만으로도 집안의 기기를 조작할 수 있도록 제공하는 **홈 네트워크 기기 조작 기능** 

## 메인 기술
<details>
  <summary>교통 안내 기능</summary>
  
  1. **ODsay API 대중교통 길찾기** (`searchPubTransPathT`)를 사용하여 다음 정보를 추출:
     - 총 소요 시간
     - 환승 횟수
     - 주요 대중교통 정보
     - 상세 경로  
     *→ 추출된 정보를 기반으로 시각장애인의 중요도에 따른 **가중치 부여**를 통해 경로를 추천하고 재정렬.*

  2. **ODsay API 실시간 도착정보**  (`realtimeStation`)를 사용하여 다음 정보를 추출:
     - 남은 정류장 수
     - 도착 예정 시간 
     - 재차인원수  
       *→ 실시간 도착정보를 통한 시각장애인의 **버스 탑승 이용성** 증대*

 <!--  3. **ODsay API 실시간 위치정보**  (`realtimeRoute`)를 사용하여 다음 정보를 추출:
     - 
     -  
     - 
-->
</details>
<details>
  <summary>리모컨 기능</summary>
  
  1. **위치 안내 기능** :
     - 음성 유도기의 유도기 기능을 활성화 시킴
  2. **신호 안내 기능** :
     - 스마트 BLE 음향 신호기의 불빛 현황을 알려줌
  3. **음성 안내 기능** :
     - 음향 신호기의 음성 안내 기능을 활성화 시킴
  
</details>
  <details>
  <summary>홈 네트워크 기기 조작 기능</summary>
</details>

## 🖥 시스템 소프트웨어 아키텍처 🖥
<p align="center">
<img width="900" src = "https://github.com/user-attachments/assets/656a2ebb-ea70-41cb-acb7-3caef437a1bf">
</p>

## 🖋️ 시스템 구성도 🖋️

<p align="center">
<img width="900" src = "https://github.com/user-attachments/assets/b86e943d-aa16-4d88-a247-52fb5241a678">
</p>

###  시스템 상세 모듈
<table align="center">
    <tr>
        <td align="center">
            <h4>사용자 인증</h4>
            <img src="https://github.com/user-attachments/assets/c7764825-cbbf-450e-a765-9d44ddd13095" alt="사용자 인증 이미지" width="400">
        </td>
        <td align="center">
            <h4>대중교통 안내</h4>
            <img src="https://github.com/user-attachments/assets/e74e2c86-f935-454c-9d0b-c494fc3cd8ad" alt="대중교통 안내 이미지" width="400">
        </td>
    </tr>
    <tr>
        <td align="center">
            <h4>IOT 등록, 홈 서비스</h4>
            <img src="https://github.com/user-attachments/assets/758b457e-397b-42e4-8a75-566cebd0e3e2" alt="IOT 등록, 홈 서비스 이미지" width="400">
        </td>
        <td align="center">
            <h4>음향신호기 기능</h4>
            <img src="https://github.com/user-attachments/assets/90cf2ca1-5986-4b84-b856-db6bbe335253" alt="음향신호기 기능 이미지" width="400">
        </td>
    </tr>
</table>

## ⚙ 기술 스택 ⚙

<div align="center">
  
| 분야 | 사용 기술 |
|:------:|:--------------------------:|
|<strong>Frontend</strong>| <img src = "https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white"> <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> |
| <strong> Backend </strong> | <img src = "https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> |
| <strong> DataBase </strong> | <img src = "https://img.shields.io/badge/MySQL-4479A1?&style=for-the-badge&logo=MySQL&logoColor=black"> <img src="https://img.shields.io/badge/Amazon RDS-527FFF?style=for-the-badge&logo=Amazon RDS&logoColor=white"> |
| <strong> DevOps </strong> | <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/githubactions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"> <img src="https://img.shields.io/badge/Amazon EC2-FF9900?style=for-the-badge&logo=Amazon EC2&logoColor=white"> |
| <strong> ETC </strong> | <img src="https://img.shields.io/badge/figma-5B0BB5?style=for-the-badge&logo=figma&logoColor=white" alt="icon" /> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"> |

</div>

## 📆 일정 📆
<p align="center">
<img width="900" src = "https://github.com/user-attachments/assets/2dd07913-dcf8-4e25-97f1-817ae1a6046b"> <br>
<img width="900" src ="https://github.com/user-attachments/assets/58631842-2cf3-4c6f-8c23-70106c24e627">
<img width="900" src="https://github.com/user-attachments/assets/50bd6c60-0a6c-4443-9e4c-0a8f807151d4">
<img width="900" src="https://github.com/user-attachments/assets/ca5b881e-94e7-41b4-a932-f29ebd262d23">
</p>

> 상세일정을 보고싶다면?
> <a href = "https://drive.google.com/file/d/1rFrqaWC5YcvNnjylfoi3GrwuYjLE4Qvq/view?usp=drive_link"> TH_GanttChart</a>

## 📌 정기 회의 📌
매주 월요일 6pm / 금요일 2pm 

##  🗂자료실 🗂
 통합 자료
- <a href = "https://drive.google.com/file/d/1PWDy0bk7yWFf5zSxnszCO37uCx1hHFtU/view?usp=sharing">팀 하이재킹 아이디어</a>

 종합설계기획 발표 및 제출 서류
- <a href = "https://drive.google.com/file/d/176_STpg_pBsIT_EmeeFJf_z5qdy2MSUh/view?usp=sharing">팀 하이재킹 제안서</a>
- <a href = "https://drive.google.com/file/d/14pNoPenm9NCOFus4_Id3Xb9OWA8gTNcc/view?usp=sharing">팀 하이재킹 요약 계획서</a>
- <a href = "https://docs.google.com/presentation/d/1yz5gyiYBwBFY1xeWGXbyRp7kZKCSKEi1/edit?usp=drive_link&ouid=102873262839899259467&rtpof=true&sd=true">시스템 구성도, 개발 환경, 운용 환경, 데모 환경</a>
- <a href = "https://drive.google.com/file/d/106PggYcS9-Hg3sq0sLVcIDpOqJFWpi5G/view?usp=drive_link">에픽, 백로그, 1차스프린트</a>
- <a href = "https://docs.google.com/presentation/d/1QXtck9GL4IYpaKq5fG3id_MRxDs5QOBO/edit?usp=sharing&ouid=110442659794936061964&rtpof=true&sd=true">2차스프린트</a>
## 🚥 개발자 🚥
**김현빈** | **서하연** | **연찬호** | **김의연**
:------: | :-------: | :-------: | :------: 
<img src="https://avatars.githubusercontent.com/u/134044125?s=64&v=4" width="150" height="150"/> | <img src="https://avatars.githubusercontent.com/u/67358565?v=4" width="150" height="150"/> | <img src="https://avatars.githubusercontent.com/u/133322979?v=4" width="150" height="150"/> | <img src="https://avatars.githubusercontent.com/u/150454163?v=4" width="150" height="150"/>
[@HyunBeen0903](https://github.com/HyunBeen0903) | [@srn13542](https://github.com/srn13542) | [@kecetchup](https://github.com/kecetchup) | [@Ret751](https://github.com/Ret751)
