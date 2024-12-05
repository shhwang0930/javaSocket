package pubsub;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Pserver {
    /*
    1. topic에 대한 컬렉션
    2. topic과 구독자에 대한 컬렉션
    >> 클라이언트가 메세지를 보낼때 서버에서 해당 구독자들에게만 보내는 방법
    1. 아이디를 받을 때 구독한 방도 같이 받게함
    2. 메세지를 보낼 때 해시맵에서 구독한 방의 채널들을 찾음
    3. 해당 채널들에게만 메세지 전송

    >> 일단 topic id 분리받아서 입력 가능하면 상태 또한 입력받아서 그걸로 구분해서 동작할 수 있도록 변경
     */

    public static void main(String[] args) {
        Set<SocketChannel> clients = new HashSet<>();
        HashMap<SocketChannel, String> topicSub = new HashMap<>();
        String topic;
        Broker broker = new Broker();
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(15000));
            serverChannel.configureBlocking(false);

            Selector selector = Selector.open(); // selector는 여러채널 중 가용한 채널이 존재하면 바로 알 수 있음
            serverChannel.register(selector, SelectionKey.OP_ACCEPT); //OP_ACCEPT : 접속 요청에 대한 이벤트 감지

            System.out.println("Server started");

            ByteBuffer ib = ByteBuffer.allocate(1024);
            ByteBuffer ob = ByteBuffer.allocate(1024);

            while (true) {
                selector.select(); //이벤트가 발생할때까지 블로킹되어 있다가 이벤트가 발생하면 다시 처리를 재개함

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); //selectedKeys set을 iterator에 담아줌
                while (iterator.hasNext()) {
                    //key : 현재 처리할 이벤트, iterator에서 key 지워줌
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        //연결 요청중인 이벤트 > 해당 요청에 대한 채널 생성
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();

                        // selector의 관리를 받기 위해 논블로킹으로 변경
                        client.configureBlocking(false);

                        //연결된 클라이언트 컬렉션에 추가
                        clients.add(client);

                        //아이디 입력받기 위한 출력을 해당 채널에 해줌
                        client.write(ByteBuffer.wrap("input id : ".getBytes()));

                        // 아이디를 입력받을 차례이므로 읽기모드로 셀렉터에 등록
                        client.register(selector, SelectionKey.OP_READ, new ClientInfo());

                    } else if (key.isReadable()) { //읽기 이벤트 발생 : 클라이언트 > 서버
                        // 현재 채널 정보 가져옴
                        SocketChannel readSocket = (SocketChannel) key.channel();
                        ClientInfo info = (ClientInfo) key.attachment(); // 사용자 정보
                        try {
                            readSocket.read(ib); // 데이터 읽어오기
                        } catch (Exception e) { // 클라이언트의 연결 종료
                            key.cancel(); // 셀렉터 관리대상에서 삭제
                            clients.remove(readSocket); // set에서 삭제

                            //서버 종료
                            topic = info.getID().substring(0, 1);
                            String end = topic + " : " + info.getID().substring(1) + "의 연결 종료";
                            broker.subscribe(readSocket, topic, topicSub);
                            System.out.println(end);

                            ob.put(end.getBytes());
                            broker.send(ob, readSocket, clients, topicSub, topic, info);
                            ob.clear();
                            continue;
                        }
                        if (info.isID()) { // 현재 아이디가 없을 경우 아이디 등록
                            //현재 ib의 내용 중 개행문자를 제외하고 가져와서 ID로 넣어줌
                            ib.limit(ib.position() - 2);
                            ib.position(0);
                            byte[] b = new byte[ib.limit()];
                            ib.get(b);
                            info.setID(new String(b));

                            // 서버 출력
                            topic = info.getID().substring(0, 1);
                            String enter = topic + " : " + info.getID().substring(1) + "의 입장";
                            broker.subscribe(readSocket, topic, topicSub);
                            System.out.println(enter);

                            ob.put(enter.getBytes());
                            broker.send(ob, readSocket, clients, topicSub, topic, info);
                            ib.clear();
                            ob.clear();

                            continue;
                        }
                        // 읽어온 데이터와 아이디 정보를 결합해 출력한 버퍼 생성
                        ib.flip();
                        ob.put((info.getID().substring(1) + " : ").getBytes());
                        ob.put(ib);
                        ob.flip();

                        topic = info.getID().substring(0, 1);
                        broker.subscribe(readSocket, topic, topicSub);
                        broker.send(ob, readSocket, clients, topicSub, topic, info);

                        ib.clear();
                        ob.clear();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientInfo {

    // 아직 아이디 입력이 안된 경우 true
    private boolean idCheck = true;
    private String id;
    private boolean topicCheck = true;
    private String topic;


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

class Broker {
    void send(ByteBuffer ob, SocketChannel readSocket, Set<SocketChannel> clients,
                     HashMap<SocketChannel, String> topicSub, String topic, ClientInfo info) throws IOException {
        topic = info.getID().substring(0, 1);
        topicSub.put(readSocket, topic);
        for (SocketChannel s : clients) {
            if (topicSub.get(s).equals(topic) && !readSocket.equals(s)) {
                s.write(ob);
                ob.flip();
            }
        }
    }

    void subscribe(SocketChannel client, String topic, HashMap<SocketChannel, String> topicSub) {
        topicSub.put(client, topic);
    }

}

