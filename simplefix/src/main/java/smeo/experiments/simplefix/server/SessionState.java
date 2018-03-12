package smeo.experiments.simplefix.server;

public class SessionState {
    int hearBeatId = 0;
    int seqNo = 0;

    public int nextSeqId() {
        return seqNo++;
    }
}
