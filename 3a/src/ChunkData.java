import java.io.Serializable;

public class ChunkData implements Serializable {
    private int start;
    private int end;
    private Alloy clientAlloy;
    private boolean addToTop;
    private boolean addToBottom;
    private boolean terminateFlag;

    public ChunkData(int start, int end, Alloy clientAlloy, boolean addToTop, boolean addToBottom, boolean terminateFlag) {
        this.start = start;
        this.end = end;
        this.clientAlloy = clientAlloy;
        this.addToTop = addToTop;
        this.addToBottom = addToBottom;
        this.terminateFlag = terminateFlag;
    }

    public void setTerminateFlag(boolean terminateFlag) {
        this.terminateFlag = terminateFlag;
    }

    public boolean isTerminateFlag() {
        return terminateFlag;
    }

    public boolean isAddToTop() {
        return addToTop;
    }

    public boolean isAddToBottom() {
        return addToBottom;
    }

    public Alloy getClientAlloy() {
        return clientAlloy;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }
}
