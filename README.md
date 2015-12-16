# Getted Started

## Unit Test

    $ junit suite.TestSuite

`junit()` is defined below. Variable `eclipse` is eclipse's location.

    junit() {
      local eclipse=$HOME/eclipse
      local plugin=$eclipse/plugins
      local junit=`find $plugin -name junit.jar`
      local hamcrest=`find $plugin -name org.hamcrest.*.jar`
      java -ea -cp bin:$junit:$hamcrest org.junit.runner.JUnitCore $1 2>&1 | less -S
    }
