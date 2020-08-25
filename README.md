# VideoStreaming

## 결과화면
![result](https://user-images.githubusercontent.com/50979183/90458094-5fce5f80-e138-11ea-897f-bbd9124f4ea1.png)

## 시연영상
![Watch the video](https://img.youtube.com/vi/QKAQjdldyWg/0.jpg)]    (https://www.youtube.com/watch?v=QKAQjdldyWg)

## Wifi-Direct
생각보다 많이 구리다...    
거의 모든 핸드폰이 와이파이 다이렉트 기능을 제공하긴 하는데,    
안드로이드 스튜디오에서 와이파이 다이렉트를 사용하려고 하면    
인식을 못하는 경우가 굉장히 많았다.    
    
또 호스트를 정하는 과정에서 LG핸드폰은 한 번 호스트를 잡으면    
로그를 삭제하기전까진 호스트를 놓지 않으려고 한다.    
이것 또한 엄청 스트레스 많이 받았다. ㅠ    
    
어쨋든 근거리 통신에서 사용할 수 있는 수단이 이것 밖에 없어서    
사용했지만, 와이파이 다이렉트는 별로인걸로..    
    
    
## Network
사실 이거 만들면서 80% 이상의 시간을 할애한 부분이다.    
네트워크 프로그래밍 수업때 소켓을 처음써보고 2년만에 다시 사용했다.    
그 당시 프로젝트 할 때는 로컬에서 통신했기 때문에 아이피는 따로 생각할    
필요가 없었는데, 이건 3대 각각 다른 핸드폰에서 통신하기 때문에    
각각 와이파이 다이렉트로 연결된 IP 알아내고 연결하고, 화면 전환할 때 마다    
소켓정보, IP정보들을 건네줘야 했다.    
    
**~~ㅅㅂ멘붕~~**    
    
진짜 하루에 10시간 코딩한다 치면 구글 검색만 8시간은 했다.    
메인 Thread에서는 소켓이나 네트워크 통신이 안되는 것 조차도 몰라서    
며칠을 헤매다 Async Task를 알게 되었다(사실 이 프로젝트는 Async Task보단    
그냥 쓰레드를 사용하는게 훨씬 좋았다.). 근데 이게 또 문제인게, 작업이 끝나면    
그대로 작업만 마치면 되는데, 소켓이 끊어져 버려서 더 이상 네트워크 통신을 할 수    
없는 상태가 되어버렸다. (**네트워크 중에서도 가장 빡쳤던 부분**)     
    
반복문 써서 소켓 안끊어지게 해보기도 하고, Async Task에서 그냥 무한루프로    
화면이 바뀌든 어쩌든 데이터 계속 보내보기도 하고, 오지게 삽질했던 것 같다.    
    
결국 해결한 방법은 통신이 한 번 끝나면 해당 비동기 작업 끝내면서 소켓 끊어버리고    
다시 비동기 쓰레드 생성하면서 소켓 연결을 다시 해줬다.    
그냥 서버 하나 만들어 놓으면 모든게 해결되지만, 프로젝트 설계 당시 와이파이 다이렉트를     
사용할 것이라고 호언장담을 해놨기 때문에 별 수 없었다.....    
(심지어 서버가 어떻게 돌아가는지 구조적인 부분도 몰랐음 ㅎ >.*)
