package smeo.experiments.playground.aop.domain;

public abstract class AbstractDomainBase {
    long somethingImportantB;

    public void getBaseVoid(){
        this.somethingImportantB = System.currentTimeMillis();
    }

    public int getBaseValue(){
        return 1;
    }

    public abstract int getAbstractChild();
}
