import java.util.List;
import java.util.Scanner;

public class Main {

    private Scanner s;
    private String server;
    private String nickname;
    private List<String> messages;

    Main(){
        this.s = new Scanner(System.in);
        this.server = "127.0.0.1";
        this.nickname = "Nickname";
    }

    void readLogin(){
        System.out.print("Server: (" + this.server + ") ");
        String server = this.s.nextLine();
        if(server.trim().length() != 0){
            this.server = server;
        }
        System.out.print("Nickname: (" + this.nickname + ") ");
        String nickname = this.s.nextLine();
        if(nickname.trim().length() != 0){
            this.nickname = nickname;
        }
        System.out.println("Anmeldung als " + this.nickname + " im Server " + this.server);
    }

    String readMessage(){
        System.out.print("> ");
        return this.s.nextLine();
    }

    void sendMessage(String message){
        // TODO: send Message
    }

    void receiveMessage(){
        // TODO: receive Messages
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.readLogin();
        do{
            main.readMessage();
        }while(true);
    }
}
