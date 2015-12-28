# Getted Started

## Unit Test

```bash
$ junit suite.TestSuite
```

`junit()` is defined below. Variable `eclipse` is eclipse's location.

```bash
junit() {
    local eclipse=$HOME/eclipse
    local plugin=$eclipse/plugins
    local junit=`find $plugin -name junit.jar`
    local hamcrest=`find $plugin -name org.hamcrest.*.jar`
    local classpath="$junit:$hamcrest:bin:lib/*"
    java -ea -cp $classpath org.junit.runner.JUnitCore $1 2>&1 | less -S
}
```
