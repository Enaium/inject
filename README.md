# inject

Inject method into another

## Usage

```java
@Inject("cn.enaium.inject.Test")
public class TestInject {
    @Method(name = "render", type = @At(type = At.Type.HEAD))
    private void renderInject(String var1, int var2, boolean var3) {
        System.out.println("HELLO INJECT");
    }
}
```

into

```java
public class Test {
    public void render(String var1, int var2, boolean var3) {
        System.out.println(var1);
    }
}
```

After inject

```java
public class Test {
    public void render(String var1, int var2, boolean var3) {
        this.renderInject(var1, var2, var3);
        System.out.println(var1);
    }

    private void renderInject(String var1, int var2, boolean var3) {
        System.out.println("HELLO INJECT");
    }
}
```

## Configuration

```json
{
  "injects": [
    "cn.enaium.inject.TestInject"
  ]
}
```

```java
public static void main(String[] args) {
    //Add configuration
    Inject.addConfiguration("inject.config.json");

    //Transform
    Inject.transform(basic)
}
```