package ch.luca008.Admin.Editor.Sockets;

import ch.luca008.Admin.Editor.SessionManager;
import ch.luca008.Admin.Editor.Sockets.Packets.IPacket;
import ch.luca008.Admin.Editor.Sockets.Packets.KeepAlivePacket;
import ch.luca008.Admin.Editor.Sockets.Packets.LoginPacket;
import ch.luca008.Challenges;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SocketManager {

    public boolean active = false;

    public void newSession(){
        new Thread(()->{
            try{
                active = true;
                listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void listen() throws IOException {
        ServerSocket server = new ServerSocket(Challenges.getGlobalConfig().getEditorPort());
        server.setSoTimeout(500);
        int attempts = 0;
        int maxAttempts = Challenges.getGlobalConfig().getEditorWaitingAttemps();
        ClientThread c = null;
        SessionManager.Session session = Challenges.getEditor().getCurrent();
        while(attempts <= maxAttempts){
            if(!active){
                server.close();
                return;
            }
            attempts++;
            Socket s = accept(server);
            if(s!=null) {
                if (s.getInetAddress().getHostAddress().equals(session.getOwner().getAddress().getAddress().getHostAddress())) {
                    c = new ClientThread(s);
                    break;
                }else{
                    s.close();
                }
            }
        }
        active = false;
        server.close();
        if(c!=null){
            session.setClient(c);
            session.activeSince = System.currentTimeMillis();
            c.session = session;
        }else{
            Challenges.getEditor().stopSession(null);
        }
    }

    private Socket accept(ServerSocket server) {
        try {
            return server.accept();
        } catch (IOException e) {
            return null;
        }
    }

    public static class ClientThread{
        public SessionManager.Session session;
        private final Socket s;
        private InputStream in;
        private OutputStream out;
        public long keepAlive = System.currentTimeMillis();
        private long timeout;
        private boolean doLog;
        private final Events events;
        public boolean active = true;
        public boolean valid = false;

        public ClientThread(Socket s){
            this.s = s;
            this.events = new Events(this);
            this.timeout = Challenges.getGlobalConfig().getEditorKeepAliveTimeout();
            this.doLog = Challenges.getGlobalConfig().doEditorLogIO();
            Bukkit.getServer().getPluginManager().registerEvents(events, Challenges.Main);
            new Thread(()->{
                try{
                    listen();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }).start();
        }

        private void listen() throws IOException {
            this.in = s.getInputStream();
            this.out = s.getOutputStream();
            while(active){
                int data = in.available();
                if(data>0){
                    IPacket packet = receive(read());
                    if(packet!=null&&(valid||packet instanceof LoginPacket)){
                        if(doLog&&!(packet instanceof KeepAlivePacket)){
                            System.out.println("[Challenges-Editor] [IN] "+packet.getClass().getSimpleName());
                        }
                        try{
                            events.PacketReceiveEvent(packet);
                        }catch(Exception e){
                            System.err.println("Server couldn't handle the last packet ("+packet.getClass().getSimpleName()+") received from the client. Error;");
                            e.printStackTrace();
                        }
                    }
                }
                if(System.currentTimeMillis()-keepAlive > timeout){
                    session.sendMessage("§cLa session avec l'éditeur va être fermée. §7Raison: §8Le serveur ne recoit plus aucune information du client. (Timeout)");
                    Challenges.getEditor().stopSession(null);
                }
            }
            this.in.close();
            this.out.close();
            this.s.close();
            HandlerList.unregisterAll(events);
            System.gc();
        }
        private void send(byte[] b) {
            try {
                byte[] h = ByteBuffer.allocate(4).putInt(b.length).array();
                this.out.write(h);
                this.out.write(b);
            } catch (IOException e) {
                System.err.println("Socket can't send packet to client, does the app crashes or timeout ? (KeepAlive:"+(System.currentTimeMillis()-keepAlive)/1000+"s)");
                e.printStackTrace();
            }
        }
        public void send(IPacket packet){
            if(packet!=null){
                JSONObject j = IPacket.getPacket(packet);
                if(!j.isEmpty()){
                    if(doLog){
                        System.out.println("[Challenges-Editor] [OUT] "+packet.getClass().getSimpleName());
                    }
                    send(j.toJSONString().getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        private byte[] read(int size) throws IOException {
            byte[] buf = new byte[size];
            int offset = 0;
            int bytesLeft = size;
            while (bytesLeft > 0) {
                int bRead = this.in.read(buf, offset, bytesLeft);
                offset += bRead;
                bytesLeft -= bRead;
            }
            return buf;
        }
        private byte[] read() throws IOException {
            return read(ByteBuffer.wrap(read(4)).getInt());
        }
        private IPacket receive(byte[] packet){
            String j = new String(packet);
            try{
                JSONObject json = (JSONObject) new JSONParser().parse(j);
                if(json!=null){
                    return IPacket.load(json);
                }
            } catch (ParseException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
