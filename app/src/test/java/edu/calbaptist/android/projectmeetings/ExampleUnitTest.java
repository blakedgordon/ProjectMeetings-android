package edu.calbaptist.android.projectmeetings;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import edu.calbaptist.android.projectmeetings.java_phoenix_channels.Channel;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.Envelope;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.IErrorCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.IMessageCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.ISocketCloseCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.ISocketOpenCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.Socket;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() throws Exception {
        final HashSet<Integer> connections = new HashSet<>();

        while(true) {
            final Socket socket = new Socket("ws://ec2-34-226-155-228.compute-1.amazonaws.com:8080/socket/websocket?token=eyJhbGciOiJSUzI1NiIsImtpZCI6IjdkODMyZDI5ZGM4ZDFmZGM2NTc4MWY4MmNiNmI3MWE4NWE4ZTljYTgifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vcHJvamVjdG1lZXRpbmctMTgzNzA2IiwibmFtZSI6IkNhbGViIFNvbG9yaW8iLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDUuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1kMjY4ai1CZ2k2SS9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS9BTlEwa2Y3RXZjOEJFd1NpNHVralVucHdVV1hINW5GMmlRL3M5Ni1jL3Bob3RvLmpwZyIsImF1ZCI6InByb2plY3RtZWV0aW5nLTE4MzcwNiIsImF1dGhfdGltZSI6MTUxMjg1MjUzMCwidXNlcl9pZCI6IndLblZmQk9HT1dZVUNCRzlCbW9uM2V5OEtjbjEiLCJzdWIiOiJ3S25WZkJPR09XWVVDQkc5Qm1vbjNleThLY24xIiwiaWF0IjoxNTEyODUyNTMxLCJleHAiOjE1MTI4NTYxMzEsImVtYWlsIjoiY2FsZWIuc29sb3Jpb0BnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjExNDkyMDg4NjgxNjI0MDAyMTI5MSJdLCJlbWFpbCI6WyJjYWxlYi5zb2xvcmlvQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6Imdvb2dsZS5jb20ifX0.AiOHK_ciavzWRN1D9Tmgf4Kohhss9VaMH-DdACesRr0xe0SaDjBpYQKvK6xbqwEGtnegdIJ5-cNXAediMZWeqiOpw-zHbJOsfFz2bPxP279BImplHBzcyzW5J93M628LwEDkBi4WGo1FApYFxXI1azq-st8ziSWOHujt4JmYLM8uIRVfpa9OLdRefUjYNJ-6IohtZbVHzc-8vybOfmbaTy13VSluSa6lV756RPp3FwhdxcNZ2lRvrio9an3VOsaTDE1De0KNHFsDa21sU7MSqtmKYZ4zaV47zqk53bHk8Tc8xEQcg0NE92s-fw5b2e2bN5pe9USjKf3-M1ymu4co6A");

            socket.onOpen(new ISocketOpenCallback() {
                @Override
                public void onOpen() {
                    final Channel channel = socket.chan("mMeeting:073b72e1-f6d8-4cb8-b237-a78c3f4a9fba", null);

                    try {
                        channel.join()
                                .receive("ignore", new IMessageCallback() {
                                    @Override
                                    public void onMessage(Envelope envelope) {
                                        System.out.println("IGNORE " + connections.size());
                                    }
                                })
                                .receive("ok", new IMessageCallback() {
                                    @Override
                                    public void onMessage(final Envelope envelope) {
                                        connections.add(socket.hashCode());
                                        System.out.println("JOINED " + connections.size());
                                    }
                                });
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            })
                    .onClose(new ISocketCloseCallback() {
                        @Override
                        public void onClose() {
                            connections.remove(socket.hashCode());
                            System.out.println("CLOSED " + connections.size());
                        }
                    })
                    .onError(new IErrorCallback() {
                        @Override
                        public void onError(final String reason) {
                            connections.remove(socket.hashCode());
                            System.out.println("ERRORS " + connections.size()  + " " + reason);
                        }
                    })
                    .connect();
        }
    }
}