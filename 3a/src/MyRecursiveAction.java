import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class MyRecursiveAction extends RecursiveAction {
    private static final int MAX_WORKLOAD = 10000;
    private static final int INSTRUCTION_ESTIMATE = 150;
    private int startIndex;
    private int endIndex;
    private int epoch;
    private Alloy alloyPointer;

    public MyRecursiveAction(Alloy alloyPointer, int epoch, int startIndex, int endIndex) {
        this.alloyPointer = alloyPointer;
        this.epoch = epoch;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    protected void compute() {

        //if work is above threshold, break tasks up into smaller tasks
        if((endIndex-startIndex)*INSTRUCTION_ESTIMATE > MAX_WORKLOAD) {
//            System.out.println("Splitting workLoad : " + (endIndex-startIndex)*INSTRUCTION_ESTIMATE);

            List<MyRecursiveAction> subtasks = new ArrayList<MyRecursiveAction>();
            subtasks.addAll(createSubtasks());

            invokeAll(subtasks);
        } else {
//            System.out.println("Doing workLoad myself: " + (endIndex-startIndex)*INSTRUCTION_ESTIMATE);
//            System.out.println("start -> " + startIndex);
//            System.out.println("end -> " + endIndex);
            alloyPointer.forkJoinAlloyUpdate(alloyPointer, epoch, startIndex, endIndex);
        }
    }

    private List<MyRecursiveAction> createSubtasks() {
        List<MyRecursiveAction> subtasks = new ArrayList<MyRecursiveAction>();
        //calculate the first chunk size INDICES
        int bStart1 = this.startIndex;
        int bEnd1 = bStart1 + ((this.endIndex-this.startIndex) / 2);
        //calculate the second chunk size INDICES
        int bStart2 = bEnd1;
        int bEnd2 = this.endIndex;

        MyRecursiveAction subtask1 = new MyRecursiveAction(alloyPointer, epoch, bStart1, bEnd1);
        MyRecursiveAction subtask2 = new MyRecursiveAction(alloyPointer, epoch, bStart2, bEnd2);

        subtasks.add(subtask1);
        subtasks.add(subtask2);

        return subtasks;
    }

    public Alloy getAlloyPointer() {
        return alloyPointer;
    }
}