package servers;

import host.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;

public class Client implements Serializable {
    private String name;
    private ChunkData chunkData;
    private int epoch;
    //    private Phaser phaser;
    private static final String serverAddress = "altair";

    public Client(String name, ChunkData chunkData) {//, Phaser phaser
        this.epoch = 0;
        this.name = name;
        this.chunkData = chunkData;
//        this.phaser = phaser;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public String getName() {
        return name;
    }

    public ChunkData getChunkData() {
        return chunkData;
    }

    public static void main(String[] args) throws IOException {
        PrintWriter writer;
        System.out.println("STARTING -> ");
        System.out.println("cores -> " + Runtime.getRuntime().availableProcessors());
        Client thisClient = null;
        Socket socket = null;
        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        MyRecursiveAction myRecursiveAction;

        // establish a connection
        try {
            boolean terminate = false;
            //set up a socket to listen on this SERVER
            socket = new Socket(serverAddress, 6112);


            ObjectOutputStream objectOutputStream = null;
            ObjectInputStream  objectInputStream;




            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            do {

                //takes input from HOST
                //receive data

//                objectOutputStream.flush();//////////////////////////////WHY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                thisClient = (Client) objectInputStream.readObject();

//                int phase = thisClient.phaser.getPhase();
                if (thisClient != null) {
                    writer = new PrintWriter("/home/kzeller/public_html/Portfolio/coursework/csc375/3bHost/GOOD_START_" + thisClient.getName(), "UTF-8");
                    writer.println("Start up OK");
                    writer.flush();

                    writer.println("BEFORE RECURSIVEACTION");
                    writer.flush();

                    System.out.println("Connected -> " + thisClient.getName());

                    Alloy clientAlloy;
                    ChunkData clientChunkData;
                    int genNum;
                    int clientAlloyWidth;
                    int clientAlloyHeight;
                    boolean addTop;
                    boolean addBottom;
                    try {
                        clientChunkData = thisClient.chunkData;
                        clientAlloy = clientChunkData.getClientAlloy();
                        addTop = clientChunkData.isAddToTop();
                        addBottom = clientChunkData.isAddToBottom();
                        genNum = thisClient.epoch;
                        clientAlloyWidth = clientAlloy.getWidth();
                        clientAlloyHeight = clientAlloy.getHeight();

                        /**SAY WHAT THIS STUFF DOES!!!!!!!!!!!!!!!!!!!!!!*/
                        int newLength = clientAlloyWidth*clientAlloyHeight;/**!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!THIS IS ALL YOU NEED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
//                        if (addTop && addBottom) {
//                            newLength = clientAlloyWidth*clientAlloyHeight + (2*clientAlloyWidth);/**WHAT IS THE 2 HERE FOR!!!???                      THE ISSUE IS PROBABLY WITH ASSIGNING THE ACTUAL SIZE OF THIS THING!!!!!*/
//                        } else if (addTop) {
//                            newLength = clientAlloyWidth*clientAlloyHeight + (clientAlloyWidth);////must distinguish top to recursive action
//                        } else if (addBottom) {
//                            newLength = clientAlloyWidth*clientAlloyHeight + (clientAlloyWidth);////must distinguish bottom to recursive action
//                        }

                        myRecursiveAction = new MyRecursiveAction(clientAlloy, genNum,0, newLength);
                        writer.println("BEFORE FORK");
                        writer.flush();
                        forkJoinPool.invoke(myRecursiveAction);/**THIS IS WHERE THE ERROR OCCURS WITH THE STACK TRACE!!!!!!!!!!!!!!!!!!!!!!!!!!!but it might not be this to be the actual issue -> it might be "RIGHT ABOVE" where I assign the sizes----(start & end)!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
                        writer.println("AFTER FORK");
                        writer.flush();
                    } catch (Exception npe) {
                        npe.printStackTrace(writer);
                        writer.flush();
                        terminate = true;
                    }

                    //sends output to the socket
                    //send the data back to the client
//                ByteArrayOutputStream b = new ByteArrayOutputStream();
                    writer.println("Wrote AN OBJECT to SERVER");
                    writer.flush();



                    objectOutputStream.writeObject(thisClient);
                    //flush
//                    objectOutputStream.flush();////////////////////////should this go in a loop?!

                    terminate = thisClient.getChunkData().isTerminateFlag();///////////////////////////////////////////////////////////////////////////////YOU WILL NEED TO "CHANGE" THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                } else {
                    writer = new PrintWriter("BAD_START", "UTF-8");
                    writer.println("Startup Bad");
                    writer.flush();
                    terminate = true;
                }
//                // close the connection
//                try {
////                    objectInputStream.close();
////                    objectOutputStream.close();
//                    socket.close();
//                } catch(IOException i) {
//                    System.out.println(i);
//                }
                writer.println("finished!!!!!!!!!!!!!!\tepoch -> " + thisClient.epoch);
                writer.flush();

                if (terminate || thisClient.epoch >= 500 || true) {
                    break;
                }
            } while (true);

//            if (thisClient != null) {
                writer = new PrintWriter("/home/kzeller/public_html/Portfolio/coursework/csc375/3bHost/TERMINATE_FLAG_" + thisClient.getName(), "UTF-8");
                writer.println(thisClient.chunkData.isTerminateFlag());
                writer.flush();
//            }

        } catch(IOException i) {
            System.out.println(i);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
