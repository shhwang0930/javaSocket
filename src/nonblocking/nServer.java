package nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class nServer {
    public static void main(String[] args) {
        // 연결된 클라이언트의 소켓을 관리할 컬렉션
        Set<SocketChannel> allClinent = new HashSet<>();


        try(ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            //포트 설정, 논블로킹 모드 설정
            serverSocketChannel.bind(new InetSocketAddress(15000));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open(); // selector는 여러채널 중 가용한 채널이 존재하면 바로 알 수 있음
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //OP_ACCEPT : 접속 요청에 대한 이벤트 감지

            System.out.println("Server started");
            //버퍼의 모니터 출력

            //입출력 시 사용할 버퍼
            ByteBuffer ib = ByteBuffer.allocate(1024);
            ByteBuffer ob = ByteBuffer.allocate(1024);

            //클라이언트 접속 시작
            while (true){
                selector.select(); // 이벤트 발생까지 스레드 블로킹
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); //selectedKeys set을 iterator에 담아줌

                while (iterator.hasNext()){
                    //key : 현재 처리할 이벤트, iterator에서 key 지워줌
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()){
                        //연결 요청중인 이벤트 > 해당 요청에 대한 채널 생성
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();

                        // selector의 관리를 받기 위해 논블로킹으로 변경
                        client.configureBlocking(false);

                        //연결된 클라이언트 컬렉션에 추가
                        allClinent.add(client);

                        //아이디 입력받기 위한 출력을 해당 채널에 해줌
                        client.write(ByteBuffer.wrap("input id : ".getBytes()));

                        // 아이디를 입력받을 차례이므로 읽기모드로 셀렉터에 등록
                        client.register(selector, SelectionKey.OP_READ, new ClientInfo());

                    } else if (key.isReadable()){ //읽기 이벤트 발생 : 클라이언트 > 서버
                        // 현재 채널 정보 가져옴
                        SocketChannel readSocket = (SocketChannel) key.channel();
                        ClientInfo info = (ClientInfo) key.attachment(); // 사용자 정보
                        try {
                            readSocket.read(ib); // 데이터 읽어오기
                        } catch (Exception e) { // 클라이언트의 연결 종료
                            key.cancel(); // 셀렉터 관리대상에서 삭제
                            allClinent.remove(readSocket); // set에서 삭제

                            //서버 종료

                            String end = info.getID() +"의 연결 종료";
                            System.out.println(end);

                            ob.put(end.getBytes());
                            for(SocketChannel s : allClinent){
                                if(!readSocket.equals(s)){
                                    ob.flip(); // 버퍼의 상태 쓰기에서 읽기로 전환 > position 값을 limit으로 설정 후 position 0으로 설정
                                    s.write(ob); // 데이터를 네트워크로 전송 > 데이터를 채널에 작성하는 작업 수행
                                }
                            }
                            ob.clear();
                            continue;
                        }
                        if(info.isID()){ // 현재 아이디가 없을 경우 아이디 등록
                            //현재 ib의 내용 중 개행문자를 제외하고 가져와서 ID로 넣어줌
                            ib.limit(ib.position()-2);
                            ib.position(0);
                            byte[] b = new byte[ib.limit()];
                            ib.get(b);
                            info.setID(new String(b));

                            // 서버 출력
                            String enter = info.getID()+"의 입장";
                            System.out.println(enter);

                            ob.put(enter.getBytes());
                            for(SocketChannel s : allClinent){
                                if(!readSocket.equals(s)){ // id동일하면 전송 x > 여기서 판별해서 보내면 될듯
                                    ob.flip(); // 버퍼의 상태 쓰기에서 읽기로 전환 > position 값을 limit으로 설정 후 position 0으로 설정
                                    s.write(ob); // 데이터를 네트워크로 전송 > 데이터를 채널에 작성하는 작업 수행
                                }
                            }
                            ib.clear();
                            ob.clear();

                            continue;
                        }
                        // 읽어온 데이터와 아이디 정보를 결합해 출력한 버퍼 생성
                        ib.flip();
                        ob.put((info.getID() + " : ").getBytes());
                        ob.put(ib);
                        ob.flip();

                        for(SocketChannel s : allClinent) {
                            if (!readSocket.equals(s)) {
                                System.out.println(s);
                                s.write(ob);
                                ob.flip();
                            }
                        }

                        ib.clear();
                        ob.clear();
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientInfo {

    // 아직 아이디 입력이 안된 경우 true
    private boolean idCheck = true;
    private String id;

    // ID가 들어있는지 확인
    boolean isID() {

        return idCheck;
    }

    // ID를 입력받으면 false로 변경
    private void setCheck() {

        idCheck = false;
    }

    // ID 정보 반환
    String getID() {

        return id;
    }

    // ID 입력
    void setID(String id) {
        this.id = id;
        setCheck();
    }
}
