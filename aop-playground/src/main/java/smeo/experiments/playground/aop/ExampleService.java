package smeo.experiments.playground.aop;

import org.axonframework.eventhandling.annotation.EventHandler;

/**
 * Created by smeo on 28.04.16.
 */
public class ExampleService {
    ExampleData exampleData;

    public void methodCreatingExampleData(){
        exampleData = new ExampleData();
    }

    public void methodUpdatingExampleDataAttributes(){
        exampleData.updateAttributes();
    }

    public void methodUpdatingExampleDataReferences(){
        exampleData.updateReferences();
    }

    @EventHandler
    public void eventHandlerCreatingExampleData(){
        exampleData = new ExampleData();
    }

    @EventHandler
    public void eventHandlerUpdatingExampleDataAttributes(){
        exampleData.updateAttributes();
    }

    @EventHandler
    public void eventHandlerUpdatingExampleDataReferences(){
        exampleData.updateReferences();
    }

}
