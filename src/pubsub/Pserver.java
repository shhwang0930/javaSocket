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

                    } else if (key.isReadable()) {
                        SocketChannel readSocket = (SocketChannel) key.channel();
                        ClientInfo info = (ClientInfo) key.attachment();
                        try {
                            readSocket.read(ib); // 데이터 읽기
                        } catch (IOException e) {
                            // 클라이언트 연결 종료 처리
                            key.cancel();
                            clients.remove(readSocket);
                            topic = info.getTopic();
                            String end = topic + " : " + info.getID() + " has disconnected.";
                            broker.send(ob, readSocket, clients, topicSub, topic, info);
                            ob.clear();
                            continue;
                        }

                        // ID 입력 상태 확인
                        if (!info.isIDEntered()) {
                            ib.limit(ib.position() - 2);
                            ib.position(0);
                            byte[] b = new byte[ib.limit()];
                            ib.get(b);
                            info.setID(new String(b)); // ID 설정

                            // ID 입력 완료 후 Topic 요청
                            readSocket.write(ByteBuffer.wrap("input topic: ".getBytes()));
                            ib.clear();
                            continue;
                        }

                        // Topic 입력 상태 확인
                        if (!info.isTopicEntered()) {
                            ib.limit(ib.position() - 2);
                            ib.position(0);
                            byte[] b = new byte[ib.limit()];
                            ib.get(b);
                            info.setTopic(new String(b)); // Topic 설정

                            // Topic 등록 및 클라이언트 입장 메시지 전송
                            topic = info.getTopic();
                            String enter = topic + " : " + info.getID() + " has joined.";
                            broker.subscribe(readSocket, topic, topicSub);
                            System.out.println(enter);

                            ob.put(enter.getBytes());
                            broker.send(ob, readSocket, clients, topicSub, topic, info);
                            ob.clear();
                            ib.clear();
                            continue;
                        }

                        // 메시지 처리 (ID와 Topic이 모두 입력된 경우)
                        ib.flip();
                        ob.put((info.getID() + " : ").getBytes());
                        ob.put(ib);
                        ob.flip();

                        topic = info.getTopic();
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
    private boolean idCheck = true; // ID 입력 여부
    private boolean topicCheck = true; // Topic 입력 여부
    private String id; // ID
    private String topic; // Topic

    // ID가 입력되었는지 확인
    boolean isIDEntered() {
        return !idCheck;
    }

    // ID 입력
    void setID(String id) {
        this.id = id;
        this.idCheck = false; // ID 입력 완료 상태로 변경
    }

    String getID() {
        return id;
    }

    // Topic이 입력되었는지 확인
    boolean isTopicEntered() {
        return !topicCheck;
    }

    // Topic 입력
    void setTopic(String topic) {
        this.topic = topic;
        this.topicCheck = false; // Topic 입력 완료 상태로 변경
    }

    String getTopic() {
        return topic;
    }

}

class Broker {
    void send(ByteBuffer ob, SocketChannel readSocket, Set<SocketChannel> clients,
              HashMap<SocketChannel, String> topicSub, String topic, ClientInfo info) throws IOException {
        topic = info.getTopic(); // 클라이언트가 입력한 실제 Topic
        for (SocketChannel client : clients) {
            // 같은 Topic에 속한 클라이언트에게만 메시지 전송
            if (topic.equals(topicSub.get(client)) && !readSocket.equals(client)) {
                ob.rewind(); // ByteBuffer를 다시 읽기 상태로 설정
                client.write(ob);
            }
        }
    }

    void subscribe(SocketChannel client, String topic, HashMap<SocketChannel, String> topicSub) {
        topicSub.put(client, topic); // Topic 등록
    }

}

