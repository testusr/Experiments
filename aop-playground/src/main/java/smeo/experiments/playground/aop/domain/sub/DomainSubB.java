package smeo.experiments.playground.aop.domain.sub;

import smeo.experiments.playground.aop.domain.AbstractDomainBase;

public class DomainSubB extends AbstractDomainBase {
    @Override
    public int getAbstractChild() {
        return 10;
    }
}
