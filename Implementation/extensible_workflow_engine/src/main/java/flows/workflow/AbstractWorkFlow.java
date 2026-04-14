package flows.workflow;

abstract class AbstractWorkFlow implements WorkFlow {

    private final String name;

    AbstractWorkFlow(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
