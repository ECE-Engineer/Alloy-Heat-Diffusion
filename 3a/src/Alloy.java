import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Alloy {
    private static final double INIT_TEMP = 10000000.0;
    private static final double APPLY_TEMP = 7000.0;
    private static final int MAX_COMPOSITION_VARIANCE = 25;

    private double firstSection[];
    private double secondSection[];
    private double metal[];
    private Composition composition;
    private int width;
    private int height;
    private Random rand;

    Alloy (Composition materials, int width, int height) {
        rand = new Random(System.currentTimeMillis());

        composition = materials;
        this.width = width;
        this.height = height;

        firstSection = new double[width*height];
        secondSection = new double[width*height];
        metal = new double[width*height*3];
    }

    Alloy() {

    }

    public void updateEdgeForClient(Alloy someAlloy, int start, int end, boolean addToTop, boolean addToBottom) {
        if (addToTop && addToBottom) {//you need both edges updated
            //update top edge
            int counter = 0;
            for (int i = (start - someAlloy.width); i < start; i++) {
                someAlloy.firstSection[counter] = this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                someAlloy.secondSection[counter] = this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                for (int j = 0; j < 3; j++) {
                    someAlloy.metal[(setSpecial3DArray(someAlloy.width, someAlloy.height, j, counter))/(someAlloy.width*someAlloy.height)] = this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)];/////////////DIFFERENT THINGS SHOULD BE IN PLACE OF X AND Y!!!!
                }
                ++counter;
            }

            //update bottom edge
            counter = 0;
            for (int i = end; i < (end + someAlloy.width); i++) {
                someAlloy.firstSection[counter] = this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                someAlloy.secondSection[counter] = this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                for (int j = 0; j < 3; j++) {
                    someAlloy.metal[(setSpecial3DArray(someAlloy.width, someAlloy.height, j, counter))/(someAlloy.width*someAlloy.height)] = this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)];/////////////DIFFERENT THINGS SHOULD BE IN PLACE OF X AND Y!!!!
                }
                ++counter;
            }
        } else if (addToTop) {//update top edge
            int counter = 0;
            for (int i = (start - someAlloy.width); i < start; i++) {
                someAlloy.firstSection[counter] = this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                someAlloy.secondSection[counter] = this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                for (int j = 0; j < 3; j++) {
                    someAlloy.metal[(setSpecial3DArray(someAlloy.width, someAlloy.height, j, counter))/(someAlloy.width*someAlloy.height)] = this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)];/////////////DIFFERENT THINGS SHOULD BE IN PLACE OF X AND Y!!!!
                }
                ++counter;
            }
        } else if (addToBottom) {//update bottom edge
            int counter = 0;
            for (int i = end; i < (end + someAlloy.width); i++) {
                someAlloy.firstSection[counter] = this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                someAlloy.secondSection[counter] = this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];/////////////////////////////////////////////////////////////////
                for (int j = 0; j < 3; j++) {
                    someAlloy.metal[(setSpecial3DArray(someAlloy.width, someAlloy.height, j, counter))/(someAlloy.width*someAlloy.height)] = this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)];/////////////DIFFERENT THINGS SHOULD BE IN PLACE OF X AND Y!!!!
                }
                ++counter;
            }
        }
    }

    public void updateSectionOnHost(Alloy someAlloy, int start, int end, boolean addToTop, boolean addToBottom) {/**DO NOT CHANGE!!!*/
        if (addToTop && addToBottom) {//get both
            int counter = 0;
            for (int i = (start - someAlloy.width); i < (end + someAlloy.width); i++) {
                this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)] = someAlloy.firstSection[counter];
                this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)] = someAlloy.secondSection[counter];
                for (int j = 0; j < 3; j++) {
                    this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)] = someAlloy.metal[(setSpecial3DArray(someAlloy.width, someAlloy.height, j, counter))/(someAlloy.width*someAlloy.height)];
                }
                ++counter;
            }
        } else if (addToTop) {//get the top
            int counter = 0;
            for (int i = (start - someAlloy.width); i < end; i++) {
                this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)] = someAlloy.firstSection[counter];
                this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)] = someAlloy.secondSection[counter];
                for (int j = 0; j < 3; j++) {
                    this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)] = someAlloy.metal[(setSpecial3DArray(someAlloy.width, someAlloy.height, j, counter))/(someAlloy.width*someAlloy.height)];
                }
                ++counter;
            }
        } else if (addToBottom) {//get the bottom
            int counter = 0;
            for (int i = start; i < (end + someAlloy.width); i++) {
                this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)] = someAlloy.firstSection[counter];
                this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)] = someAlloy.secondSection[counter];
                for (int j = 0; j < 3; j++) {
                    this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)] = someAlloy.metal[(setSpecial3DArray(someAlloy.width, someAlloy.height, j, counter))/(someAlloy.width*someAlloy.height)];
                }
                ++counter;
            }
        }
    }

    public Alloy getSectionForClientInit(int start, int end, boolean addToTop, boolean addToBottom) {/**DO NOT CHANGE!!!*/
        Alloy newAlloy = new Alloy();
        newAlloy.rand = this.rand;
        newAlloy.composition = this.composition;
        newAlloy.width = this.width;

        if (addToTop && addToBottom) {//you need both
            newAlloy.height = (this.width + (end - start) + this.width)/this.width;
            newAlloy.firstSection = new double[this.width + (end - start) + this.width];
            newAlloy.secondSection = new double[this.width + (end - start) + this.width];
            newAlloy.metal = new double[this.width + (end - start) + this.width];

            int counter = 0;
            for (int i = (start - newAlloy.width); i < (end + newAlloy.width); i++) {
                newAlloy.firstSection[counter] = this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];
                newAlloy.secondSection[counter] = this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];
                for (int j = 0; j < 3; j++) {
                    newAlloy.metal[(setSpecial3DArray(newAlloy.width, newAlloy.height, j, counter))/(newAlloy.width*newAlloy.height)] = this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)];
                }
                ++counter;
            }
        } else if (addToTop) {//create a buffer row for the top
            newAlloy.height = (this.width + (end - start))/this.width;
            newAlloy.firstSection = new double[this.width + (end - start)];
            newAlloy.secondSection = new double[this.width + (end - start)];
            newAlloy.metal = new double[(this.width + (end - start))*3];

            int counter = 0;
            for (int i = (start - newAlloy.width); i < end; i++) {
                newAlloy.firstSection[counter] = this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];
                newAlloy.secondSection[counter] = this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];
                for (int j = 0; j < 3; j++) {
                    newAlloy.metal[(setSpecial3DArray(newAlloy.width, newAlloy.height, j, counter))/(newAlloy.width*newAlloy.height)] = this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)];
                }
                ++counter;
            }
        } else if (addToBottom) {//create a buffer row for the bottom
            newAlloy.height = ((end - start) + this.width)/this.width;
            newAlloy.firstSection = new double[(end - start) + this.width];
            newAlloy.secondSection = new double[(end - start) + this.width];
            newAlloy.metal = new double[(end - start) + this.width];

            int counter = 0;
            for (int i = start; i < (end + newAlloy.width); i++) {
                newAlloy.firstSection[counter] = this.firstSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];
                newAlloy.secondSection[counter] = this.secondSection[set2DArray(this.width, i%this.width, i/this.height)/(this.width*this.height)];
                for (int j = 0; j < 3; j++) {
                    newAlloy.metal[(setSpecial3DArray(newAlloy.width, newAlloy.height, j, counter))/(newAlloy.width*newAlloy.height)] = this.metal[(set3DArray(this.width, this.height, i%this.width, i/this.height, j))/(this.width*this.height)];
                }
                ++counter;
            }
        }
        return newAlloy;
    }

    public int setSpecial3DArray(int width, int height, int z, int oneDimensionalIteratorPos) {
        return (z * width * height) + oneDimensionalIteratorPos;
    }

    public static class Composition {
        private double const1;
        private double const2;
        private double const3;
        private double ratio1;
        private double ratio2;
        private double ratio3;

        public Composition () {}

        public Composition constants(double c1, double c2, double c3) {
            const1 = c1;
            const2 = c2;
            const3 = c3;

            ratio1 = 1.0 / 3.0;
            ratio2 = 1.0 / 3.0;
            ratio3 = 1.0 / 3.0;
            return this;
        }

        public Alloy build(int width, int height) {
            return new Alloy(this, width, height);
        }

        public double getConst1() {
            return const1;
        }

        public double getConst2() {
            return const2;
        }

        public double getConst3() {
            return const3;
        }
    }

    public void createPNG(Alloy tempAlloy, File file) throws IOException {
        int tempWidth = tempAlloy.width;
        int tempHeight = tempAlloy.height;

        BufferedImage bufferedImage = new BufferedImage(tempWidth, tempHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();

        int[] pixels  = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

        for (int x = 0; x < tempWidth; x++) {
            for (int y = 0; y < tempHeight; y++) {

                int value = (int) tempAlloy.firstSection[set2DArray(tempWidth, x, y)];

                int pixelValue = (int)(value / APPLY_TEMP);

                if (value < 0) {
                    pixelValue = 0;
                } else if (pixelValue > 255) {
                    pixelValue = 255;
                }

                pixels[(x * tempWidth) + y] = pixelValue*256*256;
            }
        }

        BufferedImage image = new BufferedImage(tempWidth, tempHeight, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, tempWidth, tempHeight, pixels, 0, tempWidth);
        ImageIO.write(bufferedImage, "png", file);

    }

    public void forkJoinAlloyUpdate(Alloy alloyPointer, int epoch, int startIndex, int endIndex) {
        double[] readToBlock, writeToBlock;

        if (epoch % 2 == 0) {
            readToBlock = alloyPointer.firstSection;
            writeToBlock = alloyPointer.secondSection;
        } else {
            readToBlock = alloyPointer.secondSection;
            writeToBlock = alloyPointer.firstSection;
        }

        for (int i = startIndex; i < endIndex; i++) {
            writeToBlock[set2DArray(width, i%width, i/height)] = updateTempAtPos(alloyPointer, readToBlock, i%width, i/height);/////////////////////////////////////////ERROR!!!!!!!!!!!!!!!!!!!!!!!!ARRAY OUT OF BOUNDS
        }
    }

    public double updateTempAtPos(Alloy tempAlloy, double[] readToBlock, int x, int y) {
        int num_neighbors = 0;

        int tempWidth = tempAlloy.width;
        int tempHeight = tempAlloy.height;

        double overallTemperature = 0.0;
        //for each thermal constant
        for (int thermalConstant = 0; thermalConstant < 3; thermalConstant++) {
            double materialTemperature = 0.0;
            //for each neighbor
            for (int i = x - 1; i <= x + 1; i++) {
                double neighborRegionTemperature = 0.0;
                for (int j = y - 1; j <= y + 1; j++) {
                    if (i >= 0 && j >= 0 && i < tempAlloy.width && j < tempAlloy.height) {
                        neighborRegionTemperature += readToBlock[set2DArray(tempWidth, i, j)] * tempAlloy.metal[set3DArray(tempWidth, tempHeight, i, j, thermalConstant)];
                        if (thermalConstant == 0) {
                            num_neighbors += 1;
                        }
                    }
                }

                materialTemperature += neighborRegionTemperature;
            }
            if (thermalConstant == 0) {
                overallTemperature += materialTemperature * tempAlloy.composition.const1;
            } else if (thermalConstant == 1) {
                overallTemperature += materialTemperature * tempAlloy.composition.const2;
            } else if (thermalConstant == 2) {
                overallTemperature += materialTemperature * tempAlloy.composition.const3;
            }
        }

        return (overallTemperature / num_neighbors);
    }

    public void updateAlloy(int epoch, Alloy tempAlloy) {
        double[] readToBlock, writeToBlock;

        if (epoch % 2 == 0) {
            readToBlock = tempAlloy.firstSection;
            writeToBlock = tempAlloy.secondSection;
        } else {
            readToBlock = tempAlloy.secondSection;
            writeToBlock = tempAlloy.firstSection;
        }

        int tempWidth = tempAlloy.width;

        for (int i = 0; i < tempAlloy.width; i++) {
            for (int j = 0; j < tempAlloy.height; j++) {
                writeToBlock[set2DArray(tempWidth, i, j)] = updateTempAtPos(tempAlloy, readToBlock, i, j);
            }
        }
    }

    public void setTemperatures(Alloy tempAlloy, double temperature) {
        int tempWidth = tempAlloy.width;

        for (int i = 0; i < tempAlloy.width; i++) {
            for (int j = 0; j < tempAlloy.height; j++) {

                tempAlloy.firstSection[set2DArray(tempWidth, i, j)] = temperature;
                tempAlloy.secondSection[set2DArray(tempWidth, i, j)] = temperature;
            }
        }
    }

    public void setRandComposition(Composition composition, double[] metal, int width, int height, int x, int y) {
        double tempRatio1 = Math.abs((double) ((rand.nextInt() % (2 * MAX_COMPOSITION_VARIANCE)) - MAX_COMPOSITION_VARIANCE));
        double tempRatio2 = Math.abs((double) ((rand.nextInt() % (2 * MAX_COMPOSITION_VARIANCE)) - MAX_COMPOSITION_VARIANCE));
        double tempRatio3 = Math.abs((double) ((rand.nextInt() % (2 * MAX_COMPOSITION_VARIANCE)) - MAX_COMPOSITION_VARIANCE));

        double compositionPercent1 = composition.ratio1 * 100 + tempRatio1;
        double compositionPercent2 = composition.ratio2 * 100 + tempRatio2;
        double compositionPercent3 = composition.ratio3 * 100 + tempRatio3;

        if (compositionPercent1 < 0.0) {
            compositionPercent1 = 0.0;
        }
        if (compositionPercent2 < 0.0) {
            compositionPercent2 = 0.0;
        }
        if (compositionPercent3 < 0.0) {
            compositionPercent3 = 0.0;
        }

        double total = compositionPercent1 + compositionPercent2 + compositionPercent3;

        metal[set3DArray(width,height,x,y, 0)] = compositionPercent1 / total;
        metal[set3DArray(width,height,x,y, 1)] = compositionPercent2 / total;
        metal[set3DArray(width,height,x,y, 2)] = compositionPercent3 / total;
    }

    public void initComposition(Alloy tempAlloy) {
        Composition tempComposition = tempAlloy.composition;
        int tempWidth = tempAlloy.width;
        int tempHeight = tempAlloy.height;

        for (int i = 0; i < tempWidth; i++) {
            for (int j = 0; j < tempHeight; j++) {
                setRandComposition(tempComposition, tempAlloy.metal, tempWidth, tempHeight, i, j);
            }
        }
    }

    public void applyTemperaturePattern(Alloy tempAlloy) {
        int tempWidth = 256;
        int tempHeight = 256;

        int outerEdge;
        int a = (int) ((tempWidth / 2) * 0.8);
        int b = (int) ((tempHeight / 2) * 0.8);

        if (a < b) {
            outerEdge = a;
        } else {
            outerEdge = b;
        }

        int layers = 6;
        int iterations = outerEdge / layers;

        int middleX = tempWidth / 2;
        int middleY = tempHeight / 2;

        for (int r = 1; r < outerEdge; r++) {
            if (r % iterations == 0) {
                int rotateVal = r / iterations * 2;
                int deg = 360 / rotateVal;

                for (int d = 0; d < rotateVal; d++) {
                    for (int i = 0; i < 6; i++) {
                        int x = (int) (middleX + r * Math.cos(((d * deg + i * 2 * Math.PI / 3))));
                        int y = (int) (middleY + r * Math.sin(((d * deg + i * 2 * Math.PI / 3))));

                        tempAlloy.firstSection[set2DArray(tempWidth, x, y)] += APPLY_TEMP;
                    }
                }
            }
        }

        double[] center = new double[3];
        center[0] = (double) tempWidth / (9);///////////////MY EDIT TO THE RADIUS
        center[1] = (double) tempHeight / (9);///////////////MY EDIT TO THE RADIUS
        center[2] = 0.0;

        double[] outer = new double[3];
        outer[0] = 0.0;
        outer[1] = 0.0;
        outer[2] = 0.0;

        double maxDist = (this.getEuclideanDist(center, outer) / 5.0);
        double theta1 = (90.0 * (Math.PI / 180.0));
        double theta2 = (210.0 * (Math.PI / 180.0));
        double theta3 = (330.0 * (Math.PI / 180.0));
        double height2 = ((tempWidth) / 2.0);
        double height4 = ((tempWidth) / 4.0);
        double width2 = ((tempHeight) / 2.0);

        double[] redCenter = new double[3];
        redCenter[0] = Math.cos(theta1) * height4;
        redCenter[1] = Math.sin(theta1) * height4;
        redCenter[2] = 0.0;
        double[] greenCenter = new double[3];
        greenCenter[0] = Math.cos(theta2) * height4;
        greenCenter[1] = Math.sin(theta2) * height4;
        greenCenter[2] = 0.0;
        double[] blueCenter = new double[3];
        blueCenter[0] = Math.cos(theta3) * height4;
        blueCenter[1] = Math.sin(theta3) * height4;
        blueCenter[2] = 0.0;

        redCenter[0] += width2;
        redCenter[1] += height2;
        redCenter[2] = 0.0;
        greenCenter[0] += width2;
        greenCenter[1] += height2;
        greenCenter[2] = 0.0;
        blueCenter[0] += width2;
        blueCenter[1] += height2;
        blueCenter[2] = 0.0;

        for (int i = 0; i < tempWidth; i++) {
            for (int j = 0; j < tempHeight; j++) {
                outer[0] = (double) j;
                outer[1] = (double) i;
                outer[2] = 0.0;

                double value = ((this.getEuclideanDist(outer, redCenter)) / maxDist);
                value *= 51.2;
                value /= 2.2;
                //set the values
                tempAlloy.firstSection[set2DArray(tempWidth, i, j)] += 256-value;

                value = (((this.getEuclideanDist(outer, greenCenter)) / maxDist));
                value *= 51.2;
                value /= 2.2;
                tempAlloy.firstSection[set2DArray(tempWidth, i, j)] += 256-value;

                value = (((this.getEuclideanDist(outer, blueCenter)))/ maxDist);
                value *= 51.2;
                value /= 2.2;

                tempAlloy.firstSection[set2DArray(tempWidth, i, j)] += 256-value;


                tempAlloy.firstSection[set2DArray(tempWidth, i, j)] *= APPLY_TEMP;
            }
        }
    }

    public double getEuclideanDist(double[] refArr, double[] actArr) {
        double[] tempVec = new double[3];
        tempVec[0] = 0.0;
        tempVec[1] = 0.0;
        tempVec[2] = 0.0;

        tempVec[0] = refArr[0]-actArr[0];
        tempVec[1] = refArr[1]-actArr[1];
        tempVec[2] = refArr[2]-actArr[2];

        tempVec[0] = Math.pow(tempVec[0], 2);
        tempVec[1] = Math.pow(tempVec[1], 2);
        tempVec[2] = Math.pow(tempVec[2], 2);

        return Math.sqrt(tempVec[0]+tempVec[1]+tempVec[2]);
    }

    public int set2DArray(int width, int x, int y) {
        return (y * width) + x;
    }

    public int set3DArray(int width, int height, int x, int y, int z) {
        return (z * width * height) + (y * width) + x;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return height;
    }

    public Composition getComposition() {
        return composition;
    }

    public double[] getFirstSection() {
        return firstSection;
    }

    public double[] getSecondSection() {
        return secondSection;
    }

    public double[] getMetal() {
        return metal;
    }
}
