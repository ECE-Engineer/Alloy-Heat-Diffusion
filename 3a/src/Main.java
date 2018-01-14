import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) throws IOException {
        //parameter is the number of CPUs to run the tasks
        System.out.println("number of cores: " + Runtime.getRuntime().availableProcessors());
        final int MAX_CLIENT_SERVERS = 3;

        int width = 256;
        int height = 256;

        Alloy alloy = new Alloy.Composition()
                .constants(0.75, 1.0, 1.25)//1.0, 1.0, 1.0//1.50, 1.0, 0.50
                .build(width, height);

        alloy.initComposition(alloy);
        alloy.setTemperatures(alloy, 0.0);
        alloy.applyTemperaturePattern(alloy);


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

        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        MyRecursiveAction myRecursiveAction;
        int epochs = 500;

        /**++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
        clientAlloy1 = alloy.getSectionForClientInit(s1, e1, false, true);
        System.out.println("got first chunk");
        clientAlloy2 = alloy.getSectionForClientInit(s2, e3, true, true);
        System.out.println("got second chunk");
        clientAlloy3 = alloy.getSectionForClientInit(s3, e3, true, false);
        System.out.println("got third chunk");
        for (int j = 0; j < MAX_CLIENT_SERVERS; j++) {
            if (j == 0) {
                arrayList.add(new ChunkData(s1, e1, clientAlloy1, false, true, false));
            } else if (j == 1) {
                arrayList.add(new ChunkData(s2, e2, clientAlloy2, true, true, false));
            } else {
                arrayList.add(new ChunkData(s3, e3, clientAlloy3, true, false, false));
            }
        }
        /**++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

        for (int i = 0; i < epochs; i++) {
            myRecursiveAction = new MyRecursiveAction(alloy, i,0, alloy.getHeight()*alloy.getWidth());
            forkJoinPool.invoke(myRecursiveAction);
//            alloy.updateAlloy(i, alloy);

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            /**----------------------------------------------------------------REMERGE WORKS!!!----------------------------------------------------------------*/
            alloy.updateSectionOnHost(clientAlloy1, s1, e1, false, true);
            alloy.updateSectionOnHost(clientAlloy2, s2, e2, true, true);
            alloy.updateSectionOnHost(clientAlloy3, s3, e3, true, false);
            //create path to save a file
            String filename = "ImagesRemergeTest\\image" + "REMERGE" + i + ".png";
            String absoluteFilePath = "";
            String workingDirectory = System.getProperty("user.dir");
            absoluteFilePath = workingDirectory + System.getProperty("file.separator") + filename;

            File file = new File(absoluteFilePath);
            alloy.createPNG(alloy, file);
            System.out.println("EPOCH -> " + "REMERGE -> " + i);
            /**----------------------------------------------------------------REMERGE WORKS!!!----------------------------------------------------------------*/

            /**++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
            for (int j = 0; j < MAX_CLIENT_SERVERS; j++) {
                if (j == 0) {
                    //set edge on client end by using the update edge
                    alloy.updateEdgeForClient(clientAlloy1, s1, e1, false, true);
                } else if (j == 1) {
                    alloy.updateEdgeForClient(clientAlloy1, s2, e2, true, true);
                } else {
                    alloy.updateEdgeForClient(clientAlloy1, s3, e3, true, false);
                }
            }
            /**++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//            //create path to save a file
//            String filename = "Images\\imageFORKJOIN" + i + ".png";
//            String absoluteFilePath = "";
//            String workingDirectory = System.getProperty("user.dir");
//            absoluteFilePath = workingDirectory + System.getProperty("file.separator") + filename;
//
//            File file = new File(absoluteFilePath);
//            alloy.createPNG(alloy, file);
//            System.out.println("EPOCH -> " + i);
        }
    }
}