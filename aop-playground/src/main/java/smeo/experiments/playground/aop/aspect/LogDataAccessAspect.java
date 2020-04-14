package smeo.experiments.playground.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class LogDataAccessAspect {

    @Before("execution(public * get*(..)) && !execution(public void get*(..))")
    public void logDataAccess(JoinPoint jp) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String className = String.valueOf(stackTrace[2]);
        System.out.println(jp.getTarget().getClass().getSimpleName() + "." + jp.getSignature().getName() + " called by " + className);
    }
}
