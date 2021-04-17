# inject

Inject method into another

## Install

### [Maven](https://repo1.maven.org/maven2/cn/enaium/inject/)

[![Maven Central](https://img.shields.io/maven-central/v/cn.enaium/inject?style=flat-square)](https://search.maven.org/artifact/cn.enaium/inject/)

### [Enaium Maven](https://maven.enaium.cn)

[![Maven URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.enaium.cn%2Fcn%2Fenaium%2Finject%2Fmaven-metadata.xml&style=flat-square)](https://maven.enaium.cn)

## Usage

```java
@Inject(Test.class)
public class TestInject {
    @Method(name = "render", at = @At(type = At.Type.HEAD))
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