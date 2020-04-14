package smeo.experiments.playground.aop.domain;

public class DomainA extends AbstractDomainBase {
    long somethingImportant;
    @Override
    public int getAbstractChild() {
        return 2;
    }

    public int getDomainA(){
        return 3;
    }

    public void getVoidDomainA(){
        somethingImportant = System.currentTimeMillis();
    }
}
