package host;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Phaser;
import servers.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        final int MAX_CLIENT_SERVERS = 3;
        //create and add heat to alloy
        //parameter is the number of CPUs to run the tasks
        System.out.println("number of cores: " + Runtime.getRuntime().availableProcessors());

        int width = 256;
        int height = 256;

        Alloy alloy = new Alloy.Composition()
                .constants(0.75, 1.0, 1.25)//1.0, 1.0, 1.0//1.50, 1.0, 0.50
                .build(width, height);

        alloy.initComposition(alloy);
        alloy.setTemperatures(alloy, 0.0);
        alloy.applyTemperaturePattern(alloy);


        //divide up the chunks that will go to the different servers
        ////MAKE SURE TO ACCOUNT FOR THE OVERLAP AND TO FILL IT (appropriately)!
        ////account for the chunk sizes being different
        ////use Runtime.getRuntime().availableProcessors() to use all the available cores
        ArrayList<ChunkData> arrayList = new ArrayList<>();
        int alloyLength = width*height;
        Alloy clientAlloy1;
        Alloy clientAlloy2;
        Alloy clientAlloy3;
        int s1 = 0;
        int e1 = 0;
        int s2 = 0;
        int e2 = 0;
        int s3 = 0;
        int e3 = 0;
        s1 = 0;
        e1 = (alloyLength/2);
        s2 = e1;
        e2 = e1 + (int)(e1 * (3.0/4.0));
        s3 = e2;
        e3 = alloyLength;
        clientAlloy1 = alloy.getSectionForClientInit(s1, e1, false, true);
        System.out.println("got first chunk");
        clientAlloy2 = alloy.getSectionForClientInit(s2, e3, true, true);
        System.out.println("got second chunk");
        clientAlloy3 = alloy.getSectionForClientInit(s3, e3, true, false);
        System.out.println("got third chunk");
        for (int i = 0; i < MAX_CLIENT_SERVERS; i++) {
            if (i == 0) {
                arrayList.add(new ChunkData(s1, e1, clientAlloy1, false, true, false));
            } else if (i == 1) {
                arrayList.add(new ChunkData(s2, e2, clientAlloy2, true, true, false));
            } else {
                arrayList.add(new ChunkData(s3, e3, clientAlloy3, true, false, false));
            }
        }

        /**----------------------------------------------------------------REMERGE WORKS!!!----------------------------------------------------------------*/
//        alloy.updateSectionOnHost(clientAlloy1, s1, e1, false, true);
//        alloy.updateSectionOnHost(clientAlloy2, s2, e2, true, true);
//        alloy.updateSectionOnHost(clientAlloy3, s3, e3, true, false);
//        //create path to save a file
//        String filename = "Images\\image" + "REMERGE" + ".png";
//        String absoluteFilePath = "";
//        String workingDirectory = System.getProperty("user.dir");
//        absoluteFilePath = workingDirectory + System.getProperty("file.separator") + filename;
//
//        File file = new File(absoluteFilePath);
//        alloy.createPNG(alloy, file);
//        System.out.println("EPOCH -> " + "REMERGE");
        /**----------------------------------------------------------------REMERGE WORKS!!!----------------------------------------------------------------*/










        Phaser phaser = new Phaser(3);

        String n1 = "rho";
        String n2 = "wolf";
        String n3 = "pi";

        ArrayList<Client> clients = new ArrayList<>();
        //create a hashmap of CLIENT to (start and end indices)
        HashMap<Client, ChunkData> hashMap = new HashMap<>();

        for (int i = 0; i < MAX_CLIENT_SERVERS; i++) {
            if (i == 0) {
                clients.add(new Client(n1, arrayList.get(i)));
            } else if (i == 1) {
                clients.add(new Client(n2, arrayList.get(i)));
            } else {
                clients.add(new Client(n3, arrayList.get(i)));
            }
            hashMap.put(clients.get(i), arrayList.get(i));
        }

        //create a server socket to START communicating with the other servers
        ////must have their ips/ids
        ////when there is a connection
        ServerSocket listener = new ServerSocket(6112);
        //run the initial chunks
        Process p;
        for (int i = 0; i < MAX_CLIENT_SERVERS; i++) {
            p = Runtime.getRuntime().exec("ssh -i /home/USER/.ssh/id_rsa " + clients.get(i).getName() + " java -cp /home/USER/public_html/Portfolio/coursework/csc375/3bHost/src servers.Client");
//            System.out.println("SSH ( " + clients.get(i).getName() + " ) ran");
//            p.waitFor();///// might need
            //System.out.println ("exit: " + p.exitValue());
            //p.destroy();
        }
        System.out.println("SERVERS INITIALIZED!");

//        System.out.println("DID I GET HERE CHECK!!!!!!!!!!!");
        int counter = 0;


        try {
//            System.out.println("SUBCHECK 1");
            for (int i = 0; i < MAX_CLIENT_SERVERS; i++) {
                //                System.out.println("SUBCHECK BEFORE LISTENER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                //set up a listener
                Socket socket = listener.accept();

                //                System.out.println("ACCEPTED BY " + listener.getInetAddress().getHostName());

                System.out.println("phase -> " + phaser.getPhase());
                new Thread(() -> {
//                    final int clientNum = incrementer;
                    Client thisClient = null;
                    final Socket socketcopy = socket;

                    String clientName = socketcopy.getInetAddress().getHostName();
                    System.out.println("CLIENT NAME -> " + clientName);

                    ObjectOutputStream objectOutputStream = null;
                    try {
                        objectOutputStream = new ObjectOutputStream(socketcopy.getOutputStream());///first
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("SETUP FOR INPUT!");
                    ObjectInputStream objectInputStream = null;
                    try {
                        objectInputStream = new ObjectInputStream(socketcopy.getInputStream());///////////////////////////////waiting forever
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("SETUP FOR INPUT! END");

                    int clientIndex = 0;
                    //make sure to find the corresponding client in the list
                    for (int j = 0; j < clients.size(); j++) {
                        if (clients.get(j).getName().equalsIgnoreCase(clientName)) {
                            clientIndex = j;
                            break;
                        }
                    }


                    // serialize object
                    thisClient = clients.get(clientIndex);
                    //set the epoch
                    thisClient.setEpoch(phaser.getPhase());
                    //send the data back to the client
                    try {
                        objectOutputStream.writeObject(thisClient);
                        //flush
//                        objectOutputStream.flush();////////////////////////should this go in a loop?!
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("INITIAL DATA SENT!");


//                    while (true) {
//                        // read responses / results from client
//                        System.out.println("READ FROM CLIENT");
//                        try {
////                            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//
//
//
//                            thisClient = (Client) objectInputStream.readObject();
//                            /**-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
//
//                            phaser.arriveAndAwaitAdvance();
//
//                            //update host alloy
//                            Alloy clientChunk = thisClient.getChunkData().getClientAlloy();
//                            int begin = thisClient.getChunkData().getStart();
//                            int finish = thisClient.getChunkData().getEnd();
//                            boolean isTopEdge = thisClient.getChunkData().isAddToTop();
//                            boolean isBottomEdge = thisClient.getChunkData().isAddToBottom();
//                            alloy.updateSectionOnHost(clientChunk, begin, finish, isTopEdge, isBottomEdge);
//
//
////                            for (int j = 0; j < alloy.getHeight()*alloy.getWidth(); j++) {
////                                System.out.print(alloy.getFirstSection()[j] + " ");
////                            }
////                            System.out.println();
//                            //////////////////////////////////////////////////////////////////////////////////////SOLUTION MIGHT BE TO CREATE A NEW INPUT OR OUTPUT STREAM EVERY ITERATION!!!
//
//                            //get the phase number
//                            int phase = phaser.getPhase();
//
//                            //create path to save a file
//                            String filename = "Images/imageCLUSTER" + phase + ".png";
//                            String absoluteFilePath = "";
//                            String workingDirectory = System.getProperty("user.dir");
//                            absoluteFilePath = workingDirectory + System.getProperty("file.separator") + filename;
//                            File file = new File(absoluteFilePath);
//                            alloy.createPNG(alloy, file);
//                            System.out.println("EPOCH -> " + phase);
//
//                            //set edge on client end by using the update edge
//                            alloy.updateEdgeForClient(clientChunk, begin, finish, isTopEdge, isBottomEdge);
//
//                            if (phase >= 500) {
//                                thisClient.getChunkData().setTerminateFlag(true);
//                            }
//
//                            //set the epoch
//                            thisClient.setEpoch(phase);
//
//
//
//                            // serialize object
//                            thisClient = clients.get(clientIndex);
//                            //set the epoch
//                            thisClient.setEpoch(phaser.getPhase());
//                            //send the data back to the client
//                            try {
//                                objectOutputStream.writeObject(thisClient);
//                                //flush
////                                objectOutputStream.flush();////////////////////////should this go in a loop?!
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                break;
//                            }
//                            System.out.println("MORE DATA SENT!");
//
//
//
//                            if (phase >= 500) {
//                                break;
//                            }
//
////                            objectInputStream = new ObjectInputStream(socket.getInputStream());
//                            /**-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
//                        } catch (IOException | ClassNotFoundException e) {
//                            e.printStackTrace();
//                            break;
//                        }
//
//                        clientName = socketcopy.getInetAddress().getHostName();
//                        System.out.println("GOT TO NEXT PART! -> server -> " + clientName);
//                        System.out.println("CHECK");
//                        System.out.println("phaser -> " + phaser.getPhase() + " server -> " + clientName);
//
//
//                    }
                }).start();
            }
        } finally {
            listener.close();
        }
    }
}