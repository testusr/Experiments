package smeo.experiments.playground.aop;

import smeo.experiments.playground.aop.domain.DomainA;
import smeo.experiments.playground.aop.domain.sub.DomainSubB;

public class TestApp {
    public static void main(String[] args) {
        DomainA domainA = new DomainA();
        DomainSubB domainSubB = new DomainSubB();
        domainA.getBaseVoid();
        int test = domainA.getBaseValue();
        int testB = domainA.getAbstractChild();
        domainA.getVoidDomainA();
        domainSubB.getAbstractChild();

    }
}
