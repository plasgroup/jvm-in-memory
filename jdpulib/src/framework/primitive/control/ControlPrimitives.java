package framework.primitive.control;

public class ControlPrimitives {
    interface IDPUSingleFunction {
        Object function(Object... params);
    }
    public static Object dispatchFunction(IDPUSingleFunction function){
        return null;
    }
}
