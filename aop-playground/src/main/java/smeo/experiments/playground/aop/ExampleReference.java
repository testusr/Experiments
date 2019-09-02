package smeo.experiments.playground.aop;

/**
 * Created by smeo on 28.04.16.
 */
public class ExampleReference {

    public ExampleReference(long id) {
        this.id = id;
    }

    private final long id;
    private String referenceData = null;

    public void updatedData(){
        referenceData = "fill"+id+"#1";
        referenceData = "fill"+id+"#2";

    }
}
