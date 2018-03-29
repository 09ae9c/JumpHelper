pipeline {
  agent any
  stages {
    stage('first') {
      steps {
        echo 'this is first'
        build(job: 'init', propagate: true, quietPeriod: 1, wait: true)
      }
    }
    stage('second') {
      steps {
        echo 'sleep'
      }
    }
  }
}