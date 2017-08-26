package smeo.experiments.simplefix.server;

public class SessionContext {
    int hearBeatId = 0;
    int seqNo = 0;

    public int nextSeqId() {
        return seqNo++;
    }
}
